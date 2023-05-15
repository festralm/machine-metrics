package ru.kpfu.machinemetrics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.model.Purpose;

@Repository
public interface PurposeRepository extends JpaRepository<Purpose, Long> {
}
