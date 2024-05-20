package com.sportsapp.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.sportsapp.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.sportsapp.demo.model.User;
import com.sportsapp.demo.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // Service to get all users
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    // Get a specific user by userId
    public User getUserById(String id){
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }

    // Service to add/register a new user
    public User createUser(User user){
        return userRepository.save(user);
    }

    // Service to get user by username
    public User getUserByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }

    // Service to get user by email
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }

    // Service to update user details
    public User updateUser(String id, User userDetails){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setPassword(userDetails.getPassword());
        user.setFavoriteTeams(userDetails.getFavoriteTeams());
        return userRepository.save(user);
    }

    // Service to delete user
    public void deleteUser(String id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }
}
