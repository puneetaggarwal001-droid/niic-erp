package com.niic.erp.user;

import com.niic.erp.security.AppUserPrincipal;
import com.niic.erp.security.JwtService;
import com.niic.erp.user.dto.CurrentUserResponse;
import com.niic.erp.user.dto.LoginRequest;
import com.niic.erp.user.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponse(token, user.getUsername(), user.getRole().name(), user.getRights());
    }

    /**
     * Returns the currently authenticated user so the SPA can rehydrate its auth
     * state (username/role/rights) after a hard refresh — the JWT only carries
     * username and role, not the per-user rights set. This path is under the
     * permitAll {@code /api/auth/**} matcher, so an absent/invalid token yields a
     * null principal here rather than a filter-level 401; we return 401 ourselves.
     */
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = principal.getUser();
        return ResponseEntity.ok(
                new CurrentUserResponse(user.getUsername(), user.getRole().name(), user.getRights()));
    }
}
