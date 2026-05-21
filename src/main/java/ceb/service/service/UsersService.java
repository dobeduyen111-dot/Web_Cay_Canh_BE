package ceb.service.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import ceb.domain.entity.Users;
import ceb.domain.res.AdminCustomerResponse;
import ceb.domain.res.PagedResponse;

public interface UsersService{
    List<Users> findAll();
 
    Users getUsersByEmail(String email);

    Users findById(int userId);

    int updatePassword(int userId, String password);

    void changePassword(Authentication authentication, String currentPassword, String newPassword);

    void deleteById(int userId);

    Users updateRole(Authentication authentication, int userId, String role);

    Users updateEnabled(Authentication authentication, int userId, boolean enabled);

    PagedResponse<AdminCustomerResponse> findAdminCustomersPage(int page, int size, String keyword);
}
