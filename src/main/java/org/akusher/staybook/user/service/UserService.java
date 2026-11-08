package org.akusher.staybook.user.service;

import lombok.RequiredArgsConstructor;
import org.akusher.staybook.dto.UserRequestDto;
import org.akusher.staybook.dto.UserResponseDto;
import org.akusher.staybook.exception.DuplicateResourceException;
import org.akusher.staybook.exception.ResourceNotFoundException;
import org.akusher.staybook.user.entity.User;
import org.akusher.staybook.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(transactionManager = "userTransactionManager", readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(transactionManager = "userTransactionManager", readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional(transactionManager = "userTransactionManager")
    public UserResponseDto createUser(UserRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User with email '" + dto.getEmail() + "' already exists");
        }

        User user = User.builder()
                .firstName(dto.getFirstName().trim())
                .lastName(dto.getLastName().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .role(dto.getRole() != null && !dto.getRole().isBlank() ? dto.getRole().trim().toUpperCase() : "GUEST")
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Transactional(transactionManager = "userTransactionManager")
    public UserResponseDto updateUser(Long id, UserRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String normalizedEmail = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new DuplicateResourceException("User with email '" + dto.getEmail() + "' already exists");
        }

        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            user.setRole(dto.getRole().trim().toUpperCase());
        }

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Transactional(transactionManager = "userTransactionManager")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    private UserResponseDto mapToResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }
}
