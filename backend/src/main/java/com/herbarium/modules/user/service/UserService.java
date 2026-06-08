package com.herbarium.modules.user.service;

import com.herbarium.common.exception.BusinessException;
import com.herbarium.common.result.PageResult;
import com.herbarium.common.result.ResultCode;
import com.herbarium.modules.user.dto.PasswordUpdateDTO;
import com.herbarium.modules.user.dto.UserCreateDTO;
import com.herbarium.modules.user.dto.UserUpdateDTO;
import com.herbarium.modules.user.entity.Role;
import com.herbarium.modules.user.entity.User;
import com.herbarium.modules.user.repository.RoleRepository;
import com.herbarium.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public PageResult<User> getUserList(Integer page, Integer pageSize, String keyword, Integer status) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;

        if (StringUtils.hasText(keyword)) {
            userPage = userRepository.findByUsernameContaining(keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<User> users = userPage.getContent();
        users.forEach(this::loadRoleInfo);

        return PageResult.of(users, userPage.getTotalElements(), page, pageSize);
    }

    public User getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        loadRoleInfo(user);
        return user;
    }

    @Transactional
    public User createUser(UserCreateDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setRoleId(dto.getRoleId());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getRoleId() != null) {
            user.setRoleId(dto.getRoleId());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void updatePassword(Long id, PasswordUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public List<Role> getRoleList() {
        return roleRepository.findAll();
    }

    private void loadRoleInfo(User user) {
        if (user.getRoleId() != null) {
            roleRepository.findById(user.getRoleId()).ifPresent(role -> {
                user.setRoleName(role.getName());
                user.setRoleCode(role.getCode());
            });
        }
    }
}
