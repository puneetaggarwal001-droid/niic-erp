package com.niic.erp.user;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.user.dto.CreateUserRequest;
import com.niic.erp.user.dto.UpdateUserRequest;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already exists.");
        }
        User user = new User(request.username(), passwordEncoder.encode(request.password()), request.role());
        // Admins implicitly have every right; only store explicit rights for other roles.
        user.setRights(request.role() == Role.ADMIN ? Set.of() : request.rights());
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found."));

        // Guard against locking everyone out: the last active admin can't be
        // demoted to a lesser role or deactivated.
        boolean losingAdmin = user.getRole() == Role.ADMIN
                && (request.role() != Role.ADMIN || !request.active());
        if (losingAdmin && isLastActiveAdmin(user)) {
            throw new BadRequestException("Cannot demote or deactivate the last active admin.");
        }

        user.setRole(request.role());
        user.setActive(request.active());
        user.setRights(request.role() == Role.ADMIN || request.rights() == null ? Set.of() : request.rights());

        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters.");
            }
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return user;
    }

    private boolean isLastActiveAdmin(User candidate) {
        return userRepository.findAll().stream()
                .noneMatch(u -> !u.getId().equals(candidate.getId())
                        && u.getRole() == Role.ADMIN && u.isActive());
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
