package ru.kpfu.machinemetrics.task;

import lombok.AllArgsConstructor;
import ru.kpfu.machinemetrics.client.DataServiceClient;

@AllArgsConstructor
public class FetchDataServiceTask implements Runnable {

    private String url;
    private Long equipmentId;
    private DataServiceClient dataServiceClient;

    @Override
    public void run() {
        dataServiceClient.triggerService(url, equipmentId);
    }
}