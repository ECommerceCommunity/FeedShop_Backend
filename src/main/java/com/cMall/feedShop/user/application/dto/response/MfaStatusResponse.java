package com.cMall.feedShop.user.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaStatusResponse {
    
    private boolean enabled;
    private boolean setupRequired;
    private String email;
    private boolean hasBackupCodes;
    private String mfaType;
}
