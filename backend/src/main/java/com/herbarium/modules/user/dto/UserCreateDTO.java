package com.herbarium.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度在3-50之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度在6-50之间")
    private String password;

    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    private Integer status = 1;
}
