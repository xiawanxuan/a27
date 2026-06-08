package com.herbarium.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    TOKEN_EXPIRED(401, "Token已过期"),
    TOKEN_INVALID(401, "Token无效"),
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已禁用"),
    PASSWORD_ERROR(1003, "密码错误"),
    USERNAME_EXIST(1004, "用户名已存在"),
    SPECIMEN_NOT_FOUND(2001, "标本不存在"),
    SPECIMEN_NO_EXIST(2002, "标本编号已存在"),
    TAXONOMY_NOT_FOUND(3001, "分类不存在"),
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(4002, "文件下载失败"),
    RECOGNITION_ERROR(5001, "图像识别失败"),
    FEATURE_EXTRACT_ERROR(5002, "特征提取失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
