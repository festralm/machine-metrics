package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Unit;
import ru.kpfu.machinemetrics.repository.UnitRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.UnitConstants.UNIT_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;

    private final MessageSource messageSource;

    public List<Unit> getAll() {
        return unitRepository.findAll();
    }

    public Unit save(@NotNull Unit unit) {
        if (unit.getParent() != null) {
            unit.setParent(getById(unit.getParent().getId()));
        }
        return unitRepository.save(unit);
    }

    private Unit getById(@NotNull Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            UNIT_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(@NotNull Long id) {
        Unit unit = getById(id);
        // todo what if there are equipments
        unitRepository.delete(unit);
    }
}
