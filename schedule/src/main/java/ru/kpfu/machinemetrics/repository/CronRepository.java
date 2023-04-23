package ru.kpfu.machinemetrics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.model.Cron;

@Repository
public interface CronRepository extends JpaRepository<Cron, String> {
}
