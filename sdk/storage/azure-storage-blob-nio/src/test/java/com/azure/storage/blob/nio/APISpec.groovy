// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.TestMode
import com.azure.core.util.CoreUtils
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobClientBuilder
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobContainerClientBuilder
import com.azure.storage.blob.BlobServiceAsyncClient
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.models.BlobContainerItem
import com.azure.storage.blob.models.ListBlobContainersOptions
import com.azure.storage.blob.specialized.BlobClientBase
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.test.shared.StorageSpec
import com.azure.storage.common.test.shared.TestAccount
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Timeout

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

@Timeout(value = 5, unit = TimeUnit.MINUTES)
class APISpec extends StorageSpec {
    @Shared
    ClientLogger logger = new ClientLogger(APISpec.class)

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    // both sync and async clients point to same container
    BlobContainerClient cc
    BlobContainerAsyncClient ccAsync

    // Fields used for conveniently creating blobs with data.
    static final String defaultText = "default"

    public static final ByteBuffer defaultData = ByteBuffer.wrap(defaultText.getBytes(StandardCharsets.UTF_8))

    static final Supplier<InputStream> defaultInputStream = new Supplier<InputStream>() {
        @Override
        InputStream get() {
            return new ByteArrayInputStream(defaultText.getBytes(StandardCharsets.UTF_8))
        }
    }

    static int defaultDataSize = defaultData.remaining()

    BlobServiceClient primaryBlobServiceClient
    BlobServiceAsyncClient primaryBlobServiceAsyncClient
    BlobServiceClient alternateBlobServiceClient

    boolean recordLiveMode
    String containerName


    // The values below are used to create data-driven tests for access conditions.
    static final OffsetDateTime oldDate = OffsetDateTime.now().minusDays(1)
    static final OffsetDateTime newDate = OffsetDateTime.now().plusDays(1)
    static final String garbageEtag = "garbage"
    /*
     Note that this value is only used to check if we are depending on the received etag. This value will not actually
     be used.
     */
    static final String receivedEtag = "received"

    static final int KB = 1024
    static final int MB = 1024 * KB

    def setupSpec() {
        // The property is to limit flapMap buffer size of concurrency
        // in case the upload or download open too many connections.
        System.setProperty("reactor.bufferSize.x", "16")
        System.setProperty("reactor.bufferSize.small", "100")
        System.out.println(String.format("--------%s---------", env.testMode))
    }

    def setup() {
        // If the test doesn't have the Requires tag record it in live mode.
        recordLiveMode = specificationContext.getCurrentFeature().getFeatureMethod().getAnnotation(Requires.class) != null

        primaryBlobServiceClient = setClient(env.primaryAccount)
        primaryBlobServiceAsyncClient = getServiceAsyncClient(env.primaryAccount)
        alternateBlobServiceClient = setClient(env.secondaryAccount)

        containerName = generateContainerName()
        cc = primaryBlobServiceClient.getBlobContainerClient(containerName)
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
    }

    def cleanup() {
        def options = new ListBlobContainersOptions().setPrefix(namer.getResourcePrefix())
        for (BlobContainerItem container : primaryBlobServiceClient.listBlobContainers(options, Duration.ofSeconds(120))) {
            BlobContainerClient containerClient = primaryBlobServiceClient.getBlobContainerClient(container.getName())

            containerClient.delete()
        }
    }

    static boolean liveMode() {
        return env.testMode == TestMode.LIVE
    }

    BlobServiceClient setClient(TestAccount account) {
        try {
            return getServiceClient(account)
        } catch (Exception ignore) {
            return null
        }
    }

    BlobServiceClient getServiceClient(String endpoint) {
        return getServiceClient(null, endpoint, null)
    }

    BlobServiceClient getServiceClient(TestAccount account) {
        return getServiceClient(account.credential, account.blobEndpoint, null)
    }

    BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint) {
        return getServiceClient(credential, endpoint, null)
    }

    BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient()
    }

    BlobServiceClient getServiceClient(String sasToken, String endpoint) {
        return getServiceClientBuilder(null, endpoint, null).sasToken(sasToken).buildClient()
    }

    BlobServiceAsyncClient getServiceAsyncClient(TestAccount account) {
        return getServiceClientBuilder(account.credential, account.blobEndpoint)
            .buildAsyncClient()
    }

    BlobServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        builder.addPolicy(getRecordPolicy())

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    BlobContainerClient getContainerClient(String sasToken, String endpoint) {
        getContainerClientBuilder(endpoint).sasToken(sasToken).buildClient()
    }

    BlobContainerClientBuilder getContainerClientBuilder(String endpoint) {
        BlobContainerClientBuilder builder = new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())

        builder.addPolicy(getRecordPolicy())

        return builder
    }

    BlobAsyncClient getBlobAsyncClient(StorageSharedKeyCredential credential, String endpoint, String blobName) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .httpClient(getHttpClient())

        builder.addPolicy(getRecordPolicy())

        builder.credential(credential).buildAsyncClient()
    }

    BlobClient getBlobClient(String sasToken, String endpoint, String blobName) {
        return getBlobClient(sasToken, endpoint, blobName, null)
    }

    BlobClient getBlobClient(String sasToken, String endpoint, String blobName, String snapshotId) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .snapshot(snapshotId)
            .httpClient(getHttpClient())

        builder.addPolicy(getRecordPolicy())

        return builder.sasToken(sasToken).buildClient()
    }

    BlobClient getBlobClient(StorageSharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        builder.addPolicy(getRecordPolicy())

        return builder.credential(credential).buildClient()
    }

    BlobClient getBlobClient(StorageSharedKeyCredential credential, String endpoint, String blobName) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .httpClient(getHttpClient())

        builder.addPolicy(getRecordPolicy())

        return builder.credential(credential).buildClient()
    }

    BlobClient getBlobClient(String endpoint, String sasToken) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())

        if (!CoreUtils.isNullOrEmpty(sasToken)) {
            builder.sasToken(sasToken)
        }

        builder.addPolicy(getRecordPolicy())

        return builder.buildClient()
    }

    Map<String, Object> initializeConfigMap(HttpPipelinePolicy... policies) {
        def config = [:]
        config[AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT] = getHttpClient()
        def policyList = []
        for (HttpPipelinePolicy policy : policies) {
            policyList.push(policy)
        }
        policyList.push(getRecordPolicy())
        config[AzureFileSystem.AZURE_STORAGE_HTTP_POLICIES] = policyList as HttpPipelinePolicy[]
        return config as Map<String, Object>
    }

    def getFileSystemUri() {
        return new URI("azb://?endpoint=" + env.primaryAccount.blobEndpoint)
    }

    def generateContainerName() {
        generateResourceName(entityNo++)
    }

    def generateBlobName() {
        generateResourceName(entityNo++)
    }

    private String generateResourceName(int entityNo) {
        return namer.getRandomName(namer.getResourcePrefix() + entityNo, 63)
    }

    def createFS(Map<String,Object> config) {
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = generateContainerName() + "," + generateContainerName()
        config[AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL] = env.primaryAccount.credential

        return new AzureFileSystem(new AzureFileSystemProvider(), env.primaryAccount.blobEndpoint, config)
    }

    byte[] getRandomByteArray(int size) {
        long seed = UUID.fromString(namer.getRandomUuid()).getMostSignificantBits() & Long.MAX_VALUE
        Random rand = new Random(seed)
        byte[] data = new byte[size]
        rand.nextBytes(data)
        return data
    }

    /*
     Size must be an int because ByteBuffer sizes can only be an int. Long is not supported.
     */
    ByteBuffer getRandomData(int size) {
        return ByteBuffer.wrap(getRandomByteArray(size))
    }

    /*
    We only allow int because anything larger than 2GB (which would require a long) is left to stress/perf.
     */
    File getRandomFile(int size) {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".txt")
        file.deleteOnExit()
        FileOutputStream fos = new FileOutputStream(file)

        if (size > MB) {
            for (def i = 0; i < size / MB; i++) {
                def dataSize = Math.min(MB, size - i * MB)
                fos.write(getRandomByteArray(dataSize))
            }
        } else {
            fos.write(getRandomByteArray(size))
        }

        fos.close()
        return file
    }

    /**
     * Compares two files for having equivalent content.
     *
     * @param file1 File used to upload data to the service
     * @param file2 File used to download data from the service
     * @param offset Write offset from the upload file
     * @param count Size of the download from the service
     * @return Whether the files have equivalent content based on offset and read count
     */
    def compareFiles(File file1, File file2, long offset, long count) {
        def stream1 = new FileInputStream(file1)
        stream1.skip(offset)
        def stream2 = new FileInputStream(file2)

        return compareInputStreams(stream1, stream2, count)
    }

    def compareInputStreams(InputStream stream1, InputStream stream2, long count) {
        def pos = 0L
        def defaultReadBuffer = 128 * KB
        try {
            // If the amount we are going to read is smaller than the default buffer size use that instead.
            def bufferSize = (int) Math.min(defaultReadBuffer, count)

            while (pos < count) {
                // Number of bytes we expect to read.
                def expectedReadCount = (int) Math.min(bufferSize, count - pos)
                def buffer1 = new byte[expectedReadCount]
                def buffer2 = new byte[expectedReadCount]

                def readCount1 = stream1.read(buffer1)
                def readCount2 = stream2.read(buffer2)

                // Use Arrays.equals as it is more optimized than Groovy/Spock's '==' for arrays.
                assert readCount1 == readCount2 && Arrays.equals(buffer1, buffer2)

                pos += expectedReadCount
            }

            def verificationRead = stream2.read()
            return pos == count && verificationRead == -1
        } finally {
            stream1.close()
            stream2.close()
        }
    }

    // Only sleep if test is running in live mode
    def sleepIfRecord(long milliseconds) {
        if (env.testMode != TestMode.PLAYBACK) {
            sleep(milliseconds)
        }
    }

    def rootNameToContainerName(String root) {
        return root.substring(0, root.length() - 1)
    }

    def rootNameToContainerClient(String root) {
        return primaryBlobServiceClient.getBlobContainerClient(rootNameToContainerName(root))
    }

    def getNonDefaultRootDir(FileSystem fs) {
        for (Path dir : fs.getRootDirectories()) {
            if (!dir.equals(((AzureFileSystem) fs).getDefaultDirectory())) {
                return dir.toString()
            }
        }
        throw new Exception("File system only contains the default directory")
    }

    def getDefaultDir(FileSystem fs) {
        return ((AzureFileSystem) fs).getDefaultDirectory().toString()
    }

    def getPathWithDepth(int depth) {
        def pathStr = ""
        for (int i = 0; i < depth; i++) {
            pathStr += generateBlobName() + AzureFileSystem.PATH_SEPARATOR
        }
        return pathStr
    }

    def putDirectoryBlob(BlockBlobClient blobClient) {
        blobClient.commitBlockListWithResponse(Collections.emptyList(), null,
            [(AzureResource.DIR_METADATA_MARKER): "true"], null, null, null, null)
    }

    /**
     * This will retrieve the etag to be used in testing match conditions. The result will typically be assigned to
     * the ifMatch condition when testing success and the ifNoneMatch condition when testing failure.
     *
     * @param bc
     *      The URL to the blob to get the etag on.
     * @param match
     *      The ETag value for this test. If {@code receivedEtag} is passed, that will signal that the test is expecting
     *      the blob's actual etag for this test, so it is retrieved.
     * @return
     * The appropriate etag value to run the current test.
     */
    def setupBlobMatchCondition(BlobClientBase bc, String match) {
        if (match == receivedEtag) {
            return bc.getProperties().getETag()
        } else {
            return match
        }
    }

    def checkBlobIsDir(BlobClient blobClient) {
         String isDir = blobClient.getPropertiesWithResponse(null, null, null)
             .getValue().getMetadata().get(AzureResource.DIR_METADATA_MARKER)
        return isDir != null && isDir == "true"
    }

    static class TestFileAttribute<T> implements  FileAttribute<T> {
        String name
        T value

        TestFileAttribute(String name, T value) {
            this.name = name
            this.value = value
        }

        @Override
        String name() {
            return this.name
        }

        @Override
        T value() {
            return this.value
        }
    }
}
