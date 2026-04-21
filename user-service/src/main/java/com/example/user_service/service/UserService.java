package com.example.user_service.service;



import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // Create user
    public User saveUser(User user) {
        return repository.save(user);
    }

    // Get all users
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return repository.findById(id);
    }

    // Delete user
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}
