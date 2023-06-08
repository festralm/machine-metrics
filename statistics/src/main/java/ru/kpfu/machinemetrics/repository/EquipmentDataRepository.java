package ru.kpfu.machinemetrics.repository;

import jakarta.validation.constraints.NotNull;
import ru.kpfu.machinemetrics.model.EquipmentData;

import java.util.ArrayList;
import java.util.List;

public interface EquipmentDataRepository {

    ArrayList<EquipmentData> getData(@NotNull String start, @NotNull String stop, Long equipmentId);

    void delete(@NotNull Long equipmentId);
}
