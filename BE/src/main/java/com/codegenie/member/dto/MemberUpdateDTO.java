package com.codegenie.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateDTO {
    private String username; 
    private String currentPassword;
    private String newPassword;
}
