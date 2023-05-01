package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.DataService;
import ru.kpfu.machinemetrics.repository.DataServiceRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.DataServiceConstants.DATA_SERVICE_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class DataServiceService {

    private final DataServiceRepository dataServiceRepository;

    private final MessageSource messageSource;

    public List<DataService> getAll() {
        return dataServiceRepository.findAll();
    }

    public DataService save(@NotNull DataService dataService) {
        return dataServiceRepository.save(dataService);
    }

    public DataService getById(@NotNull Long id) {
        return dataServiceRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            DATA_SERVICE_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(@NotNull Long id) {
        DataService dataService = getById(id);
        // todo what if there are equipments
        dataServiceRepository.delete(dataService);
    }

    public DataService edit(@NotNull DataService updatedDataService) {
        getById(updatedDataService.getId());
        return dataServiceRepository.save(updatedDataService);
    }
}
