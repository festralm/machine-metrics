package ru.kpfu.machinemetrics.repository;

import com.influxdb.client.write.Point;

public interface EquipmentDataRepository {

    void save(Point point);
}
