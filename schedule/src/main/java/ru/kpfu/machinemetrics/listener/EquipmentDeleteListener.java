package ru.kpfu.machinemetrics.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.service.EquipmentInfoService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EquipmentDeleteListener {

    private final EquipmentInfoService equipmentInfoService;

    @RabbitListener(queues = "rk-equipment")
    public void listen(String in) {
        System.out.println("Message read from rk-equipment : " + in);
        try {
            equipmentInfoService.delete(Long.parseLong(in));
        } catch (ResourceNotFoundException ignored) {
            log.info("exception is ignored", ignored);
        }
    }
}
