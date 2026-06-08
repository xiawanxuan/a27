package com.herbarium.modules.user.controller;

import com.herbarium.common.result.PageResult;
import com.herbarium.common.result.Result;
import com.herbarium.modules.user.dto.PasswordUpdateDTO;
import com.herbarium.modules.user.dto.UserCreateDTO;
import com.herbarium.modules.user.dto.UserUpdateDTO;
import com.herbarium.modules.user.entity.Role;
import com.herbarium.modules.user.entity.User;
import com.herbarium.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(userService.getUserList(page, pageSize, keyword, status));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> createUser(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success(userService.createUser(dto));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        return Result.success(userService.updateUser(id, dto));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updatePassword(@PathVariable Long id, @Valid @RequestBody PasswordUpdateDTO dto) {
        userService.updatePassword(id, dto);
        return Result.success();
    }

    @Operation(summary = "获取角色列表")
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Role>> getRoleList() {
        return Result.success(userService.getRoleList());
    }
}
