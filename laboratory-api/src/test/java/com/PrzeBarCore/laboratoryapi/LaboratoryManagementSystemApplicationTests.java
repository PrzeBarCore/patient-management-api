package com.przebarcore.laboratoryapi;

import com.przebarcore.laboratoryapi.outbox.EventSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class LaboratoryManagementSystemApplicationTests {
	@MockitoBean
	private EventSender eventSender;

	@Test
	void contextLoads() {
	}

}
