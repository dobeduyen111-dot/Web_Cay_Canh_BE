package ceb.service.implement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import ceb.domain.entity.Users;
import ceb.exception.EmailSendFailedException;
import ceb.service.service.MailTemplateService;
import ceb.service.service.PasswordResetMailService;
import jakarta.mail.internet.MimeMessage;

@Service
public class PasswordResetMailServiceImpl implements PasswordResetMailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String resetPasswordUrl;
    private final MailTemplateService mailTemplateService;

    public PasswordResetMailServiceImpl(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromEmail,
            @Value("${app.auth.reset-password.url:http://localhost:3000/reset-password}") String resetPasswordUrl,
            MailTemplateService mailTemplateService) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.resetPasswordUrl = resetPasswordUrl;
        this.mailTemplateService = mailTemplateService;
    }

    @Override
    public void sendResetPasswordMail(Users user, String resetToken) {
        String resetLink = resetPasswordUrl + "?token=" + resetToken;
        String html = mailTemplateService.buildResetPasswordEmail(user.getFullName(), resetLink, resetToken);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Yeu cau dat lai mat khau");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new EmailSendFailedException(ex);
        }
    }
}
