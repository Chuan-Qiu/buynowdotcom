package com.dailycodework.buynowdotcom.service.user;

import com.dailycodework.buynowdotcom.dto.UserDto;
import com.dailycodework.buynowdotcom.model.Cart;
import com.dailycodework.buynowdotcom.model.User;
import com.dailycodework.buynowdotcom.repository.CartRepository;
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
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Long userId) {
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
        return userRepository.findById(userId).map(existingUser -> {
            existingUser.setFirstName(request.getFirstName());
            existingUser.setLastName(request.getLastName());
            existingUser.setEmail(request.getEmail());
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId).ifPresentOrElse(userRepository::delete,
                () -> { throw new EntityNotFoundException("User not found"); });
    }

    @Override
    public UserDto convertUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}