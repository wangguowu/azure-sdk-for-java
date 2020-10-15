/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.compute.v2019_07_01;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a scale-in policy for a virtual machine scale set.
 */
public class ScaleInPolicy {
    /**
     * The rules to be followed when scaling-in a virtual machine scale set.
     * &lt;br&gt;&lt;br&gt; Possible values are: &lt;br&gt;&lt;br&gt;
     * **Default** When a virtual machine scale set is scaled in, the scale set
     * will first be balanced across zones if it is a zonal scale set. Then, it
     * will be balanced across Fault Domains as far as possible. Within each
     * Fault Domain, the virtual machines chosen for removal will be the newest
     * ones that are not protected from scale-in. &lt;br&gt;&lt;br&gt;
     * **OldestVM** When a virtual machine scale set is being scaled-in, the
     * oldest virtual machines that are not protected from scale-in will be
     * chosen for removal. For zonal virtual machine scale sets, the scale set
     * will first be balanced across zones. Within each zone, the oldest
     * virtual machines that are not protected will be chosen for removal.
     * &lt;br&gt;&lt;br&gt; **NewestVM** When a virtual machine scale set is
     * being scaled-in, the newest virtual machines that are not protected from
     * scale-in will be chosen for removal. For zonal virtual machine scale
     * sets, the scale set will first be balanced across zones. Within each
     * zone, the newest virtual machines that are not protected will be chosen
     * for removal. &lt;br&gt;&lt;br&gt;.
     */
    @JsonProperty(value = "rules")
    private List<VirtualMachineScaleSetScaleInRules> rules;

    /**
     * Get the rules to be followed when scaling-in a virtual machine scale set. &lt;br&gt;&lt;br&gt; Possible values are: &lt;br&gt;&lt;br&gt; **Default** When a virtual machine scale set is scaled in, the scale set will first be balanced across zones if it is a zonal scale set. Then, it will be balanced across Fault Domains as far as possible. Within each Fault Domain, the virtual machines chosen for removal will be the newest ones that are not protected from scale-in. &lt;br&gt;&lt;br&gt; **OldestVM** When a virtual machine scale set is being scaled-in, the oldest virtual machines that are not protected from scale-in will be chosen for removal. For zonal virtual machine scale sets, the scale set will first be balanced across zones. Within each zone, the oldest virtual machines that are not protected will be chosen for removal. &lt;br&gt;&lt;br&gt; **NewestVM** When a virtual machine scale set is being scaled-in, the newest virtual machines that are not protected from scale-in will be chosen for removal. For zonal virtual machine scale sets, the scale set will first be balanced across zones. Within each zone, the newest virtual machines that are not protected will be chosen for removal. &lt;br&gt;&lt;br&gt;.
     *
     * @return the rules value
     */
    public List<VirtualMachineScaleSetScaleInRules> rules() {
        return this.rules;
    }

    /**
     * Set the rules to be followed when scaling-in a virtual machine scale set. &lt;br&gt;&lt;br&gt; Possible values are: &lt;br&gt;&lt;br&gt; **Default** When a virtual machine scale set is scaled in, the scale set will first be balanced across zones if it is a zonal scale set. Then, it will be balanced across Fault Domains as far as possible. Within each Fault Domain, the virtual machines chosen for removal will be the newest ones that are not protected from scale-in. &lt;br&gt;&lt;br&gt; **OldestVM** When a virtual machine scale set is being scaled-in, the oldest virtual machines that are not protected from scale-in will be chosen for removal. For zonal virtual machine scale sets, the scale set will first be balanced across zones. Within each zone, the oldest virtual machines that are not protected will be chosen for removal. &lt;br&gt;&lt;br&gt; **NewestVM** When a virtual machine scale set is being scaled-in, the newest virtual machines that are not protected from scale-in will be chosen for removal. For zonal virtual machine scale sets, the scale set will first be balanced across zones. Within each zone, the newest virtual machines that are not protected will be chosen for removal. &lt;br&gt;&lt;br&gt;.
     *
     * @param rules the rules value to set
     * @return the ScaleInPolicy object itself.
     */
    public ScaleInPolicy withRules(List<VirtualMachineScaleSetScaleInRules> rules) {
        this.rules = rules;
        return this;
    }

}