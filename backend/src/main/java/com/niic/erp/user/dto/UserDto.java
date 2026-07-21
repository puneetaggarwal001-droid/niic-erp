package com.niic.erp.user.dto;

import com.niic.erp.user.User;
import java.util.Set;

public record UserDto(
        Long id,
        String username,
        String role,
        boolean active,
        Set<String> rights) {

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.isActive(),
                user.getRights());
    }
}
