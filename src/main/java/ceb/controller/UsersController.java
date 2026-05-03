package ceb.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ceb.domain.entity.Users;
import ceb.domain.req.UserChangePasswordRequest;
import ceb.domain.req.UserUpdateProfileRequest;
import ceb.domain.res.MessageResponse;
import ceb.domain.res.UserProfileUpdateResponse;
import ceb.domain.res.UserResponse;
import ceb.service.service.CurrentUserService;
import ceb.service.service.UsersService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/user")
public class UsersController {

    private final UsersService usersService;
    private final CurrentUserService currentUserService;

    public UsersController(UsersService usersService, CurrentUserService currentUserService) {
        this.usersService = usersService;
        this.currentUserService = currentUserService;
    }

    @GetMapping(params = "!email")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAlls() {
        return usersService.findAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping(value = {"", "/email"}, params = "email")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getByEmail(
            @RequestParam
            @NotBlank(message = "Email khong duoc de trong")
            @Email(message = "Email khong dung dinh dang")
            @Size(max = 150, message = "Email khong duoc vuot qua 150 ky tu")
            String email) {
        return UserResponse.from(usersService.getUsersByEmail(email.trim()));
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        Users user = currentUserService.getCurrentUser(authentication);
        return UserResponse.from(user);
    }

    @PutMapping("/me")
    public UserProfileUpdateResponse updateMe(
            Authentication authentication,
            @Valid @RequestBody UserUpdateProfileRequest request) {
        Users updatedUser = usersService.updateCurrentUserProfile(authentication, request);
        return new UserProfileUpdateResponse("Cap nhat thong tin thanh cong", UserResponse.from(updatedUser));
    }

    @PutMapping("/me/password")
    public MessageResponse changePassword(
            Authentication authentication,
            @Valid @RequestBody UserChangePasswordRequest request) {
        usersService.changePassword(authentication, request.getOldPassword(), request.getNewPassword());
        return new MessageResponse("Doi mat khau thanh cong");
    }
}
