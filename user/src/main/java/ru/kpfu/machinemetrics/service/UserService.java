package ru.kpfu.machinemetrics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.User;
import ru.kpfu.machinemetrics.repository.UserRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.UserConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public List<User> getAllNotDeleted() {
        return userRepository.findAllByDeletedFalse();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User getById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            USER_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(Long id) {
        User user = getById(id);
        user.setDeleted(true);
        userRepository.save(user);
    }

    public User edit(Long id, User updatedUser) {
        User user = getById(id);

        user.setName(updatedUser.getName());
        user.setSurname(updatedUser.getSurname());

        return userRepository.save(user);
    }
}
