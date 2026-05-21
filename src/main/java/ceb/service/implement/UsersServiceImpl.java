package ceb.service.implement;

import java.util.List;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ceb.domain.entity.Users;
import ceb.domain.res.AdminCustomerResponse;
import ceb.domain.res.PagedResponse;
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
    public Users findById(int userId) {
        return getExistingUser(userId);
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
        protectLastAdmin(userId, null, null);
        usersRepository.deleteById(userId);
    }

    @Override
    public Users updateRole(Authentication authentication, int userId, String role) {
        Users currentUser = currentUserService.getCurrentUser(authentication);
        Users targetUser = getExistingUser(userId);
        String normalizedRole = normalizeRole(role);

        if (targetUser.getUserId() == currentUser.getUserId() && !"ADMIN".equals(normalizedRole)) {
            throw new BadRequestException("Khong the tu ha quyen tai khoan admin dang dang nhap");
        }

        protectLastAdmin(userId, normalizedRole, null);
        usersRepository.updateRole(userId, normalizedRole);
        return getExistingUser(userId);
    }

    @Override
    public Users updateEnabled(Authentication authentication, int userId, boolean enabled) {
        Users currentUser = currentUserService.getCurrentUser(authentication);
        if (currentUser.getUserId() == userId && !enabled) {
            throw new BadRequestException("Khong the tu khoa tai khoan admin dang dang nhap");
        }

        protectLastAdmin(userId, null, enabled);
        usersRepository.updateEnabled(userId, enabled);
        return getExistingUser(userId);
    }

    @Override
    public PagedResponse<AdminCustomerResponse> findAdminCustomersPage(int page, int size, String keyword) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        long totalItems = usersRepository.countAdminCustomers(keyword);
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / safeSize);

        return new PagedResponse<>(
                usersRepository.findAdminCustomersPage(offset, safeSize, keyword),
                safePage,
                safeSize,
                totalItems,
                totalPages,
                safePage < totalPages,
                safePage > 1);
    }

    private Users getExistingUser(int userId) {
        return usersRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new BadRequestException("Role khong duoc de trong");
        }

        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        if (!"ADMIN".equals(normalizedRole) && !"USER".equals(normalizedRole)) {
            throw new BadRequestException("Role khong hop le");
        }
        return normalizedRole;
    }

    private void protectLastAdmin(int userId, String nextRole, Boolean nextEnabled) {
        Users targetUser = getExistingUser(userId);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(targetUser.getRole());
        if (!isAdmin) {
            return;
        }

        boolean willRemainAdmin = nextRole == null || "ADMIN".equalsIgnoreCase(nextRole);
        boolean willRemainEnabled = nextEnabled == null ? targetUser.isEnabled() : nextEnabled;
        if (willRemainAdmin && willRemainEnabled) {
            return;
        }

        if (usersRepository.countEnabledAdmins() <= 1) {
            throw new BadRequestException("Khong the thay doi admin cuoi cung dang hoat dong");
        }
    }
}
