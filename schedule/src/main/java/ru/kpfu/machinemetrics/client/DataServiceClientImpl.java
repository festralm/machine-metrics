package ru.kpfu.machinemetrics.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataServiceClientImpl implements DataServiceClient {
    @Override
    public void triggerService(String url, Long equipmentId) {
        log.info(url);
        log.info(equipmentId.toString());
    }
}
