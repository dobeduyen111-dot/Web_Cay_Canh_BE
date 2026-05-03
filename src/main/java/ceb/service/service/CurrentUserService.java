package ceb.service.service;

import org.springframework.security.core.Authentication;

import ceb.domain.entity.Users;

public interface CurrentUserService {

    Users getCurrentUser(Authentication authentication);

    int getCurrentUserId(Authentication authentication);
}
