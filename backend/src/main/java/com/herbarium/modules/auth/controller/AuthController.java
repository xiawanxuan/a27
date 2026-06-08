package com.herbarium.modules.auth.controller;

import com.herbarium.common.result.Result;
import com.herbarium.modules.auth.dto.LoginRequest;
import com.herbarium.modules.auth.dto.LoginResponse;
import com.herbarium.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userinfo")
    public Result<LoginResponse.UserInfoVO> getUserInfo() {
        return Result.success(authService.getCurrentUserInfo());
    }
}
