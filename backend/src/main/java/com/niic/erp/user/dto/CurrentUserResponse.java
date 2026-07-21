package com.niic.erp.user.dto;

import java.util.Set;

public record CurrentUserResponse(
        String username,
        String role,
        Set<String> rights) {
}
