package ru.kpfu.machinemetrics.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import ru.kpfu.machinemetrics.dto.EquipmentInfoCreateDto;
import ru.kpfu.machinemetrics.validation.annotation.EquipmentInfoConstraint;

public class EquipmentInfoValidator implements
        ConstraintValidator<EquipmentInfoConstraint, EquipmentInfoCreateDto> {
    @Override
    public void initialize(EquipmentInfoConstraint equipmentInfoConstraint) {
    }

    @Override
    public boolean isValid(EquipmentInfoCreateDto equipmentInfoCreateDto, ConstraintValidatorContext context) {
        if (equipmentInfoCreateDto == null) {
            return false;
        }

        boolean valid = true;
        if (equipmentInfoCreateDto.isEnabled()) {
            if (equipmentInfoCreateDto.getDataServiceId() == null || equipmentInfoCreateDto.getDataServiceId() < 1L) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{validation.equipment-info.data-service.empty}")
                        .addConstraintViolation();
                valid = false;
            }
            if (equipmentInfoCreateDto.getCronId() == null || equipmentInfoCreateDto.getCronId() == 0) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{validation.equipment-info.cron.empty}")
                        .addConstraintViolation();
                valid = false;
            }
        }
        return valid;
    }
}
