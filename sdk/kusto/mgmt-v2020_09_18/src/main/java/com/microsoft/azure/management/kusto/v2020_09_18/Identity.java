/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.kusto.v2020_09_18;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Identity for the resource.
 */
public class Identity {
    /**
     * The principal ID of resource identity.
     */
    @JsonProperty(value = "principalId", access = JsonProperty.Access.WRITE_ONLY)
    private String principalId;

    /**
     * The tenant ID of resource.
     */
    @JsonProperty(value = "tenantId", access = JsonProperty.Access.WRITE_ONLY)
    private String tenantId;

    /**
     * The type of managed identity used. The type 'SystemAssigned,
     * UserAssigned' includes both an implicitly created identity and a set of
     * user-assigned identities. The type 'None' will remove all identities.
     * Possible values include: 'None', 'SystemAssigned', 'UserAssigned',
     * 'SystemAssigned, UserAssigned'.
     */
    @JsonProperty(value = "type", required = true)
    private IdentityType type;

    /**
     * The list of user identities associated with the Kusto cluster. The user
     * identity dictionary key references will be ARM resource ids in the form:
     * '/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{identityName}'.
     */
    @JsonProperty(value = "userAssignedIdentities")
    private Map<String, IdentityUserAssignedIdentitiesValue> userAssignedIdentities;

    /**
     * Get the principal ID of resource identity.
     *
     * @return the principalId value
     */
    public String principalId() {
        return this.principalId;
    }

    /**
     * Get the tenant ID of resource.
     *
     * @return the tenantId value
     */
    public String tenantId() {
        return this.tenantId;
    }

    /**
     * Get the type of managed identity used. The type 'SystemAssigned, UserAssigned' includes both an implicitly created identity and a set of user-assigned identities. The type 'None' will remove all identities. Possible values include: 'None', 'SystemAssigned', 'UserAssigned', 'SystemAssigned, UserAssigned'.
     *
     * @return the type value
     */
    public IdentityType type() {
        return this.type;
    }

    /**
     * Set the type of managed identity used. The type 'SystemAssigned, UserAssigned' includes both an implicitly created identity and a set of user-assigned identities. The type 'None' will remove all identities. Possible values include: 'None', 'SystemAssigned', 'UserAssigned', 'SystemAssigned, UserAssigned'.
     *
     * @param type the type value to set
     * @return the Identity object itself.
     */
    public Identity withType(IdentityType type) {
        this.type = type;
        return this;
    }

    /**
     * Get the list of user identities associated with the Kusto cluster. The user identity dictionary key references will be ARM resource ids in the form: '/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{identityName}'.
     *
     * @return the userAssignedIdentities value
     */
    public Map<String, IdentityUserAssignedIdentitiesValue> userAssignedIdentities() {
        return this.userAssignedIdentities;
    }

    /**
     * Set the list of user identities associated with the Kusto cluster. The user identity dictionary key references will be ARM resource ids in the form: '/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{identityName}'.
     *
     * @param userAssignedIdentities the userAssignedIdentities value to set
     * @return the Identity object itself.
     */
    public Identity withUserAssignedIdentities(Map<String, IdentityUserAssignedIdentitiesValue> userAssignedIdentities) {
        this.userAssignedIdentities = userAssignedIdentities;
        return this;
    }

}