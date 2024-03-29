package com.amazonaws.wafv2.webaclassociation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CallbackContext {
    private int stabilizationRetriesRemaining;
}
