package com.indothai.order_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "order.runner.enabled=false")
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
