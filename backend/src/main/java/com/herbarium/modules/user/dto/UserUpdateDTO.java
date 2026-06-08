package com.herbarium.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @Size(max = 50, message = "真实姓名长度不超过50")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    private Long roleId;

    private Integer status;
}
