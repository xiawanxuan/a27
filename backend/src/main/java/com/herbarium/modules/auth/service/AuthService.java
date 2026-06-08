package com.herbarium.modules.auth.service;

import com.herbarium.common.exception.BusinessException;
import com.herbarium.common.result.ResultCode;
import com.herbarium.modules.auth.dto.LoginRequest;
import com.herbarium.modules.auth.dto.LoginResponse;
import com.herbarium.modules.user.entity.Role;
import com.herbarium.modules.user.entity.User;
import com.herbarium.modules.user.repository.RoleRepository;
import com.herbarium.modules.user.repository.UserRepository;
import com.herbarium.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Role role = roleRepository.findById(user.getRoleId()).orElse(null);
        if (role != null) {
            user.setRoleName(role.getName());
            user.setRoleCode(role.getCode());
        }

        LoginResponse.UserInfoVO userInfo = new LoginResponse.UserInfoVO(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getAvatar(),
                user.getRoleId(),
                user.getRoleCode(),
                user.getRoleName(),
                user.getStatus()
        );

        return new LoginResponse(token, userInfo);
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }

    public LoginResponse.UserInfoVO getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Role role = roleRepository.findById(user.getRoleId()).orElse(null);
        if (role != null) {
            user.setRoleName(role.getName());
            user.setRoleCode(role.getCode());
        }

        return new LoginResponse.UserInfoVO(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getAvatar(),
                user.getRoleId(),
                user.getRoleCode(),
                user.getRoleName(),
                user.getStatus()
        );
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null ? user.getId() : null;
    }
}
