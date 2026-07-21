package com.niic.erp.user;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.user.dto.CreateUserRequest;
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

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
