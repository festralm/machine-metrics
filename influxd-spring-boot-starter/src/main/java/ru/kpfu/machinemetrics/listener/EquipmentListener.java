package ru.kpfu.machinemetrics.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.kpfu.machinemetrics.model.EquipmentSchedule;
import ru.kpfu.machinemetrics.service.EquipmentScheduleService;

@Component
@RequiredArgsConstructor
public class EquipmentListener {

    private final EquipmentScheduleService equipmentScheduleService;

    @RabbitListener(queues = "rk-${app.name}")
    public void listen(EquipmentSchedule in) {
        System.out.println("Message read from myQueue : " + in);
        equipmentScheduleService.save(in);
    }

    @RabbitListener(queues = "rk-${app.name}-delete")
    public void listenDelete(String in) {
        System.out.println("Delete message read from myQueue : " + in);
        equipmentScheduleService.delete(Long.parseLong(in));
    }
}
