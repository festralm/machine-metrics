package ru.kpfu.machinemetrics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.model.EquipmentData;

@Repository
public interface EquipmentDataRepository extends JpaRepository<EquipmentData, Long> {

}
