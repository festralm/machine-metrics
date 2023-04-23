package ru.kpfu.machinemetrics.client;

public interface DataServiceClient {

    void triggerService(String url, Long equipmentId);
}
