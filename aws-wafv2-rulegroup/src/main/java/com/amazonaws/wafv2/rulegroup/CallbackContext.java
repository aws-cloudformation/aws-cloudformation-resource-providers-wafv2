package com.amazonaws.wafv2.rulegroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CallbackContext {
    private String id;
    private String name;
    private String lockToken;
    private int stabilizationRetriesRemaining;
}
