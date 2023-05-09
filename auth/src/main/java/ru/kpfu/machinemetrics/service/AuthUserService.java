package ru.kpfu.machinemetrics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.AuthUser;
import ru.kpfu.machinemetrics.repository.AuthUserRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.AuthUserConstants.AUTH_USER_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class AuthUserService implements UserDetailsManager {

    private final AuthUserRepository authUserRepository;
    private final MessageSource messageSource;

    public List<AuthUser> getAll() {
        return authUserRepository.findAll();
    }

    public AuthUser save(AuthUser user) {
        return authUserRepository.save(user);
    }

    public AuthUser getById(Long id) {
        return authUserRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            AUTH_USER_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(Long id) {
        AuthUser user = getById(id);
        authUserRepository.delete(user);
    }

    @Override
    public void createUser(UserDetails user) {
        authUserRepository.save((AuthUser) user);
    }

    @Override
    public void updateUser(UserDetails user) {
        authUserRepository.save((AuthUser) user);
    }

    @Override
    public void deleteUser(String username) {
        AuthUser userDetails = authUserRepository.findByEmail(username)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            AUTH_USER_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{username},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
        authUserRepository.delete(userDetails);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        AuthUser userDetails = authUserRepository.findByPassword(oldPassword)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            AUTH_USER_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{oldPassword},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
        userDetails.setPassword(newPassword);
        authUserRepository.save(userDetails);
    }

    @Override
    public boolean userExists(String username) {
        return authUserRepository.findByEmail(username).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return authUserRepository.findByEmail(username)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            AUTH_USER_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{username},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }
}
