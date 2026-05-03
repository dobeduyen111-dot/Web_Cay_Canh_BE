package ceb.service.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import ceb.domain.entity.Users;
import ceb.domain.req.UserUpdateProfileRequest;

public interface UsersService{
    List<Users> findAll();
 
    Users getUsersByEmail(String email);

    Users updateCurrentUserProfile(Authentication authentication, UserUpdateProfileRequest request);

    int updatePassword(int userId, String password);

    void changePassword(Authentication authentication, String currentPassword, String newPassword);

    void deleteById(int userId);
    
}

