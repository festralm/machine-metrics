package ru.kpfu.machinemetrics.repository;

import jakarta.validation.constraints.NotNull;
import ru.kpfu.machinemetrics.model.EquipmentData;

import java.util.List;

public interface EquipmentDataRepository {

    List<EquipmentData> getData(@NotNull String start, @NotNull String stop, Long equipmentId);
}
