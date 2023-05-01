package ru.kpfu.machinemetrics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.model.DataService;

@Repository
public interface DataServiceRepository extends JpaRepository<DataService, Long> {
}
