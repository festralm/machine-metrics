package ru.kpfu.machinemetrics.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.kpfu.machinemetrics.service.EquipmentDataService;

@Component
@RequiredArgsConstructor
public class DataFetchListener {

    private final EquipmentDataService equipmentDataService;

    @RabbitListener(queues = "rk-equipment")
    public void listen(String in) {
        System.out.println("Message read from rk-equipment : " + in);
        equipmentDataService.delete(Long.parseLong(in));
    }
}
