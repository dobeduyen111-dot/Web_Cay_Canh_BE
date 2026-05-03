package ceb.service.implement;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import ceb.domain.res.SendOtpResponse;
import ceb.exception.BadRequestException;
import ceb.exception.EmailSendFailedException;
import ceb.exception.UserNotFoundException;
import ceb.repository.UsersRepository;
import ceb.service.service.MailTemplateService;
import ceb.service.service.OtpMailService;
import jakarta.mail.internet.MimeMessage;

@Service
public class OtpMailServiceImpl implements OtpMailService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final long otpTtlSeconds;
    private final long resendCooldownSeconds;
    private final int maxResends;
    private final MailTemplateService mailTemplateService;
    private final UsersRepository usersRepository;
    private final Map<String, OtpState> otpStates = new ConcurrentHashMap<>();

    public OtpMailServiceImpl(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromEmail,
            @Value("${app.auth.otp.expiration-seconds:300}") long otpTtlSeconds,
            @Value("${app.auth.otp.resend-cooldown-seconds:120}") long resendCooldownSeconds,
            @Value("${app.auth.otp.max-resends:5}") int maxResends,
            MailTemplateService mailTemplateService,
            UsersRepository usersRepository) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.otpTtlSeconds = otpTtlSeconds;
        this.resendCooldownSeconds = resendCooldownSeconds;
        this.maxResends = maxResends;
        this.mailTemplateService = mailTemplateService;
        this.usersRepository = usersRepository;
    }

    @Override
    public SendOtpResponse sendOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        usersRepository.findByEmail(normalizedEmail)
                .orElseThrow(UserNotFoundException::new);

        synchronized (normalizedEmail.intern()) {
            Instant now = Instant.now();
            OtpState previousState = otpStates.get(normalizedEmail);

            OtpState state = createNextState(previousState, now);

            sendMail(normalizedEmail, state.code(), state.expiresAt());
            otpStates.put(normalizedEmail, state);

            return new SendOtpResponse(
                    "OTP da duoc gui den email",
                    Math.max(0, state.expiresAt().getEpochSecond() - now.getEpochSecond()),
                    resendCooldownSeconds,
                    Math.max(0, maxResends - state.resendCount()));
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        String normalizedEmail = email.trim().toLowerCase();
        synchronized (normalizedEmail.intern()) {
            Instant now = Instant.now();
            OtpState state = otpStates.get(normalizedEmail);

            if (state == null) {
                throw new BadRequestException("Khong tim thay OTP hoac OTP chua duoc gui");
            }

            if (now.isAfter(state.expiresAt())) {
                otpStates.remove(normalizedEmail);
                throw new BadRequestException("OTP da het han");
            }

            if (!state.code().equals(otp.trim())) {
                throw new BadRequestException("OTP khong chinh xac");
            }

            otpStates.remove(normalizedEmail);
        }
    }

    private OtpState createNextState(OtpState current, Instant now) {
        if (current == null || now.isAfter(current.expiresAt())) {
            return new OtpState(generateOtp(), now.plusSeconds(otpTtlSeconds), now.plusSeconds(resendCooldownSeconds), 0);
        }

        if (now.isBefore(current.nextAllowedResendAt())) {
            long secondsLeft = Math.max(1, current.nextAllowedResendAt().getEpochSecond() - now.getEpochSecond());
            throw new BadRequestException("Chi duoc gui lai OTP sau %s giay".formatted(secondsLeft));
        }

        if (current.resendCount() >= maxResends) {
            throw new BadRequestException("Da vuot qua so lan gui lai OTP toi da");
        }

        return new OtpState(
                generateOtp(),
                now.plusSeconds(otpTtlSeconds),
                now.plusSeconds(resendCooldownSeconds),
                current.resendCount() + 1);
    }

    private void sendMail(String email, String otp, Instant expiresAt) {
        String html = mailTemplateService.buildOtpEmail(otp, expiresAt, resendCooldownSeconds);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Ma OTP xac thuc");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new EmailSendFailedException(ex);
        }
    }

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private record OtpState(
            String code,
            Instant expiresAt,
            Instant nextAllowedResendAt,
            int resendCount) {
    }
}
