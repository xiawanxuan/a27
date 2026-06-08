package com.herbarium.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private UserInfoVO userInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoVO {
        private Long id;
        private String username;
        private String realName;
        private String email;
        private String avatar;
        private Long roleId;
        private String roleCode;
        private String roleName;
        private Integer status;
    }
}
