package ceb.service.service;

import ceb.domain.entity.Users;

public interface PasswordResetMailService {

    void sendResetPasswordMail(Users user, String resetToken);
}
