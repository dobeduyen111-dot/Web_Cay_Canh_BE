package ceb.service.implement;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import ceb.domain.entity.Users;
import ceb.exception.AuthenticationRequiredException;
import ceb.exception.CurrentUserUnavailableException;
import ceb.security.CustomUserDetails;
import ceb.service.service.CurrentUserService;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    @Override
    public Users getCurrentUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationRequiredException();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }

        if (!(principal instanceof Users user)) {
            throw new CurrentUserUnavailableException();
        }

        return user;
    }

    @Override
    public int getCurrentUserId(Authentication authentication) {
        return getCurrentUser(authentication).getUserId();
    }
}
