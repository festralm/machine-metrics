package ru.kpfu.machinemetrics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.model.DataService;
import ru.kpfu.machinemetrics.model.EquipmentInfo;

import java.util.List;

@Repository
public interface EquipmentInfoRepository extends JpaRepository<EquipmentInfo, Long> {

    List<EquipmentInfo> findAllByEnabledIsTrue();
}
