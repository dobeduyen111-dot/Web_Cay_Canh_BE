package ceb.service.service;

import ceb.domain.entity.Users;

public interface AuthService {

    Users register(Users user);

    Users login(String email, String password);

    Users loginWithGoogle(String email, String fullName);

    void requestPasswordReset(String email, String newPassword);

    void resetPassword(String email, String newPassword);

    void logout(String token);
}
