package com.niic.erp.user.dto;

import com.niic.erp.user.Role;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

/**
 * Admin edit of an existing account. Password is optional — a blank/absent
 * value leaves the current password untouched; a non-blank value resets it
 * (length is validated in the service so blank can safely mean "no change").
 */
public record UpdateUserRequest(
        @NotNull Role role,
        Set<String> rights,
        @NotNull Boolean active,
        String password) {
}
