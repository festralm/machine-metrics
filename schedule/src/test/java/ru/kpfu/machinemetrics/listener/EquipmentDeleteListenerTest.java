package ru.kpfu.machinemetrics.listener;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.service.EquipmentInfoService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@SpringBootTest
public class EquipmentDeleteListenerTest {

    @Autowired
    private EquipmentDeleteListener equipmentDeleteListener;

    @MockBean
    private EquipmentInfoService equipmentInfoService;

    @Test
    public void testListen() {
        // given
        String message = "123";

        // when
        equipmentDeleteListener.listen(message);

        // then
        verify(equipmentInfoService).delete(123L);
    }

    @Test
    public void testListenEquipmentDoesntExist() {
        // given
        String message = "123";
        doThrow(new ResourceNotFoundException("not found")).when(equipmentInfoService).delete(123L);

        // when
        try {
            equipmentDeleteListener.listen(message);
        } catch (Exception e) {
            Assertions.fail("Exception should not have been thrown");
        }
    }
}
