package ru.kpfu.machinemetrics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.model.UsageType;

@Repository
public interface UsageTypeRepository extends JpaRepository<UsageType, Long> {
}
