package ceb.service.service;

import java.time.Instant;

public interface MailTemplateService {

    String buildOtpEmail(String otp, Instant expiresAt, long resendCooldownSeconds);

    String buildResetPasswordEmail(String fullName, String resetLink, String resetToken);
}
