package ru.kpfu.machinemetrics.listener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.model.EquipmentSchedule;
import ru.kpfu.machinemetrics.service.EquipmentScheduleService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = {EquipmentListener.class})
public class EquipmentListenerTest {

    @Autowired
    private EquipmentListener equipmentListener;

    @MockBean
    private EquipmentScheduleService equipmentScheduleService;

    @Test
    public void testListen() {
        // given
        EquipmentSchedule message = EquipmentSchedule.builder()
                .id(1L)
                .cron("* * * * * *")
                .enabled(true)
                .build();
        when(equipmentScheduleService.save(message)).thenReturn(message);

        // when
        equipmentListener.listen(message);

        // then
        verify(equipmentScheduleService, times(1)).save(message);
    }

    @Test
    public void testListenDelete() {
        // given
        String message = "1";
        doNothing().when(equipmentScheduleService).delete(1L);

        // when
        equipmentListener.listenDelete(message);

        // then
        verify(equipmentScheduleService, times(1)).delete(1L);
    }
}
