package ceb.service.implement;

import java.util.Optional;
import java.util.UUID;

import ceb.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import ceb.domain.entity.Users;
import ceb.exception.AccountCreationFailedException;
import ceb.exception.EmailAlreadyExistsException;
import ceb.exception.EmailRequiredException;
import ceb.exception.InvalidCredentialsException;
import ceb.exception.InvalidRegistrationException;
import ceb.exception.BadRequestException;
import ceb.exception.PasswordRequiredException;
import ceb.exception.PhoneAlreadyExistsException;
import ceb.exception.UserDisabledException;
import ceb.exception.UserNotFoundException;
import ceb.repository.UsersRepository;
import ceb.security.TokenBlacklistService;
import ceb.service.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UsersRepository usersRepository,
            PasswordEncoder passwordEncoder,
            TokenBlacklistService tokenBlacklistService,
            JwtService jwtService) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtService = jwtService;
    }

    @Override
    public Users register(Users user) {
        if (user == null) {
            throw new InvalidRegistrationException();
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new EmailRequiredException();
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new PasswordRequiredException();
        }

        String normalizedEmail = user.getEmail().trim();
        String normalizedPhone = user.getPhone() == null ? null : user.getPhone().trim().replaceAll("\\s+", "");
        if (normalizedPhone != null && normalizedPhone.isBlank()) {
            normalizedPhone = null;
        }

        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);

        Optional<Users> existingUser = usersRepository.findByEmail(normalizedEmail);
        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        if (normalizedPhone != null && usersRepository.findByPhone(normalizedPhone).isPresent()) {
            throw new PhoneAlreadyExistsException();
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }

        usersRepository.save(user);
        return usersRepository.findByEmail(normalizedEmail)
                .orElseThrow(AccountCreationFailedException::new);
    }

    @Override
    public Users login(String email, String password) {
        Users dbUser = usersRepository.findByEmail(email.trim())
                .orElseThrow(UserNotFoundException::new);

        if (!dbUser.isEnabled()) {
            throw new UserDisabledException();
        }

        if (!passwordEncoder.matches(password, dbUser.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return dbUser;
    }

    @Override
    public Users loginWithGoogle(String email, String fullName) {
        if (!StringUtils.hasText(email)) {
            throw new UserNotFoundException();
        }

        String normalizedEmail = email.trim();
        String normalizedFullName = StringUtils.hasText(fullName) ? fullName.trim() : normalizedEmail;

        Optional<Users> existingUser = usersRepository.findByEmail(normalizedEmail);
        if (existingUser.isPresent()) {
            Users user = existingUser.get();
            if (!user.isEnabled()) {
                throw new UserDisabledException();
            }
            return user;
        }

        Users user = new Users();
        user.setFullName(normalizedFullName);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole("USER");
        user.setEnabled(true);
        usersRepository.save(user);

        return usersRepository.findByEmail(normalizedEmail)
                .orElseThrow(AccountCreationFailedException::new);
    }

    @Override
    public void requestPasswordReset(String email, String newPassword) {
        if (email == null || email.isBlank()) {
            throw new EmailRequiredException();
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new PasswordRequiredException();
        }

        Users user = usersRepository.findByEmail(email.trim())
                .filter(Users::isEnabled)
                .orElseThrow(UserNotFoundException::new);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("Mat khau moi khong duoc trung mat khau cu");
        }

        usersRepository.updatePassword(user.getUserId(), passwordEncoder.encode(newPassword));
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        requestPasswordReset(email, newPassword);
    }

    @Override
    public void logout(String token) {
        // Lấy expiration time từ token và thêm vào blacklist
        long expirationTime = jwtService.getTokenExpiration(token);
        tokenBlacklistService.blacklistToken(token, expirationTime);
    }
}
