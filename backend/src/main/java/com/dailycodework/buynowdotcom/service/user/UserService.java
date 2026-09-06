package com.dailycodework.buynowdotcom.service.user;

import com.dailycodework.buynowdotcom.dto.UserDto;
import com.dailycodework.buynowdotcom.model.Cart;
import com.dailycodework.buynowdotcom.model.User;
import com.dailycodework.buynowdotcom.config.RoleSeeder;
import com.dailycodework.buynowdotcom.model.Role;
import com.dailycodework.buynowdotcom.repository.CartRepository;
import com.dailycodework.buynowdotcom.repository.RoleRepository;
import com.dailycodework.buynowdotcom.repository.UserRepository;
import com.dailycodework.buynowdotcom.request.CreateUserRequest;
import com.dailycodework.buynowdotcom.request.UpdateUserRequest;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Long userId) {
        assertSelfOrAdmin(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public User createUser(CreateUserRequest request) {
        return Optional.of(request)
                .filter(r -> !userRepository.existsByEmail(r.getEmail()))
                .map(r -> {
                    User user = new User();
                    user.setFirstName(r.getFirstName());
                    user.setLastName(r.getLastName());
                    user.setEmail(r.getEmail());
                    user.setPassword(passwordEncoder.encode(r.getPassword()));
                    Role defaultRole = roleRepository.findByName(RoleSeeder.ROLE_USER)
                            .orElseThrow(() -> new IllegalStateException(
                                    "ROLE_USER is missing — RoleSeeder should have created it at startup"));
                    user.setRoles(new java.util.HashSet<>(java.util.List.of(defaultRole)));
                    User savedUser = userRepository.save(user);
                    Cart cart = new Cart();
                    cart.setUser(savedUser);
                    cartRepository.save(cart);
                    return savedUser;
                })
                .orElseThrow(() -> new EntityExistsException("User with email " + request.getEmail() + " already exists"));
    }

    @Override
    public User updateUser(UpdateUserRequest request, Long userId) {
        assertSelfOrAdmin(userId);
        return userRepository.findById(userId).map(existingUser -> {
            existingUser.setFirstName(request.getFirstName());
            existingUser.setLastName(request.getLastName());
            existingUser.setEmail(request.getEmail());
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public void deleteUser(Long userId) {
        assertSelfOrAdmin(userId);
        userRepository.findById(userId).ifPresentOrElse(userRepository::delete,
                () -> { throw new EntityNotFoundException("User not found"); });
    }

    @Override
    public UserDto convertUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    /**
     * A user id in the URL identifies the account but proves nothing about who
     * is asking. Access is granted only to the account holder, or to an admin.
     */
    private void assertSelfOrAdmin(Long userId) {
        User authenticated = getAuthenticatedUser();
        if (authenticated.getId().equals(userId)) {
            return;
        }
        boolean isAdmin = authenticated.getRoles() != null && authenticated.getRoles().stream()
                .anyMatch(role -> RoleSeeder.ROLE_ADMIN.equals(role.getName()));
        if (!isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You may only access your own account");
        }
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}