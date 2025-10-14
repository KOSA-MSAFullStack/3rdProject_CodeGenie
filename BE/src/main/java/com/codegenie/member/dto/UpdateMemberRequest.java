// com.codegenie.member.dto.UpdateMemberRequest.java
package com.codegenie.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRequest {
    private String username;
    private String currentPassword;
    private String newPassword;
}
