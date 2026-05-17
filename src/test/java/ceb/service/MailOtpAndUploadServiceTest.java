package ceb.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import ceb.TestData;
import ceb.domain.res.CloudinaryResponse;
import ceb.exception.BadRequestException;
import ceb.exception.FileEmptyException;
import ceb.repository.UsersRepository;
import ceb.service.implement.CloudinaryServiceImpl;
import ceb.service.implement.MailTemplateServiceImpl;
import ceb.service.implement.OtpMailServiceImpl;
import ceb.service.implement.PasswordResetMailServiceImpl;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailOtpAndUploadServiceTest {

    @Test
    void mailTemplateServiceBuildsOtpAndResetHtml() {
        MailTemplateServiceImpl service = new MailTemplateServiceImpl();

        assertThat(service.buildOtpEmail("123456", Instant.now().plusSeconds(300), 120))
                .contains("123456", "Verification Code");
        assertThat(service.buildResetPasswordEmail(null, "https://reset", "token"))
                .contains("Dat Lai Mat Khau", "https://reset", "token");
    }

    @Test
    void otpServiceSendsAndVerifiesGeneratedOtp() {
        JavaMailSender mailSender = mailSender();
        UsersRepository usersRepository = mock(UsersRepository.class);
        AtomicReference<String> generatedOtp = new AtomicReference<>();
        MailTemplateServiceImpl templateService = mock(MailTemplateServiceImpl.class);
        when(usersRepository.findByEmail("a@example.com")).thenReturn(Optional.of(TestData.user()));
        when(templateService.buildOtpEmail(any(String.class), any(Instant.class), anyLong()))
                .thenAnswer(invocation -> {
                    generatedOtp.set(invocation.getArgument(0));
                    return "<html>otp</html>";
                });
        OtpMailServiceImpl service = new OtpMailServiceImpl(
                mailSender,
                "noreply@example.com",
                300,
                120,
                5,
                templateService,
                usersRepository);

        assertThat(service.sendOtp(" A@Example.com ").getRemainingResends()).isEqualTo(5);
        service.verifyOtp("a@example.com", generatedOtp.get());

        assertThatThrownBy(() -> service.verifyOtp("a@example.com", generatedOtp.get()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void otpServiceEnforcesResendCooldown() {
        JavaMailSender mailSender = mailSender();
        UsersRepository usersRepository = mock(UsersRepository.class);
        when(usersRepository.findByEmail("a@example.com")).thenReturn(Optional.of(TestData.user()));
        OtpMailServiceImpl service = new OtpMailServiceImpl(
                mailSender,
                "noreply@example.com",
                300,
                120,
                5,
                new MailTemplateServiceImpl(),
                usersRepository);

        service.sendOtp("a@example.com");

        assertThatThrownBy(() -> service.sendOtp("a@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Chi duoc gui lai OTP");
    }

    @Test
    void passwordResetMailServiceBuildsAndSendsMessage() {
        JavaMailSender mailSender = mailSender();
        MailTemplateServiceImpl templateService = mock(MailTemplateServiceImpl.class);
        when(templateService.buildResetPasswordEmail("Nguyen Van A", "https://reset?token=token", "token"))
                .thenReturn("<html>reset</html>");
        PasswordResetMailServiceImpl service = new PasswordResetMailServiceImpl(
                mailSender,
                "noreply@example.com",
                "https://reset",
                templateService);

        service.sendResetPasswordMail(TestData.user(), "token");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void cloudinaryServiceUploadsNonEmptyFileAndRejectsEmptyFile() throws Exception {
        Cloudinary cloudinary = mock(Cloudinary.class);
        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("public_id", "plants/1", "secure_url", "https://cdn.example.test/1.jpg"));
        CloudinaryServiceImpl service = new CloudinaryServiceImpl(cloudinary);

        CloudinaryResponse response = service.uploadFile(new MockMultipartFile("file", "plant.jpg", "image/jpeg", new byte[] {1}));

        assertThat(response.getPublicId()).isEqualTo("plants/1");
        assertThat(response.getUrl()).isEqualTo("https://cdn.example.test/1.jpg");
        assertThatThrownBy(() -> service.uploadFile(new MockMultipartFile("file", new byte[0])))
                .isInstanceOf(FileEmptyException.class);
    }

    private JavaMailSender mailSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        return mailSender;
    }
}
