package ceb.service.implement;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ceb.domain.entity.Users;
import ceb.exception.BadRequestException;
import ceb.exception.CurrentPasswordIncorrectException;
import ceb.exception.PasswordRequiredException;
import ceb.exception.UserNotFoundException;
import ceb.repository.UsersRepository;
import ceb.service.service.CurrentUserService;
import ceb.service.service.UsersService;

@Service
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public UsersServiceImpl(
            UsersRepository usersRepository,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @Override
    public List<Users> findAll() {
        return usersRepository.findAlls();
    }

    @Override
    public Users getUsersByEmail(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public int updatePassword(int userId, String password) {
        if (password == null || password.isBlank()) {
            throw new PasswordRequiredException();
        }

        getExistingUser(userId);
        return usersRepository.updatePassword(userId, passwordEncoder.encode(password));
    }

    @Override
    public void changePassword(Authentication authentication, String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new PasswordRequiredException();
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new PasswordRequiredException();
        }

        Users currentUser = currentUserService.getCurrentUser(authentication);
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new CurrentPasswordIncorrectException();
        }
        if (passwordEncoder.matches(newPassword, currentUser.getPassword())) {
            throw new BadRequestException("Mat khau moi khong duoc trung mat khau cu");
        }

        usersRepository.updatePassword(currentUser.getUserId(), passwordEncoder.encode(newPassword));
    }

    @Override
    public void deleteById(int userId) {
        getExistingUser(userId);
        usersRepository.deleteById(userId);
    }

    private Users getExistingUser(int userId) {
        return usersRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }
}
