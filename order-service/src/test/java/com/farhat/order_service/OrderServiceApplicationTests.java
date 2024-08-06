package com.farhat.order_service;

import com.farhat.order_service.dto.OrderLineItemsDto;
import com.farhat.order_service.dto.OrderRequest;
import com.farhat.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceApplicationTests {

	@Container
	static MySQLContainer mySQLContainer = new MySQLContainer("mysql:5.7.34");
	@Autowired
	private  OrderRepository orderRepository;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@DynamicPropertySource
	static void setDynamicProperties(DynamicPropertyRegistry dynamicPropertyRegistry){
		dynamicPropertyRegistry.add("spring.datasource.url",mySQLContainer::getJdbcUrl);
		dynamicPropertyRegistry.add("spring.datasource.username", mySQLContainer::getUsername);
		dynamicPropertyRegistry.add("spring.datasource.password", mySQLContainer::getPassword);
	}

	@Test
	void shouldCreateOrder() throws Exception {
		OrderRequest orderRequest = createOrderRequest();

		String orderRequestString = objectMapper.writeValueAsString(orderRequest);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(orderRequestString))
			.andExpect(status().isCreated());

		Assertions.assertEquals(1,orderRepository.findAll().size());

	}

	public OrderRequest createOrderRequest(){
		OrderRequest orderRequest = new OrderRequest();
		OrderLineItemsDto orderLineItemsDto = new OrderLineItemsDto();
		orderLineItemsDto.setSkuCode("123");
		orderLineItemsDto.setPrice(BigDecimal.valueOf(1000));
		orderLineItemsDto.setQuantity(1);
		orderRequest.setOrderLineItemsDtosList(List.of(orderLineItemsDto));

		return orderRequest;
	}

}
//So we'll be using MockMvc to perform the request itself (also side note, this is an integration test)
//At the same time we'll be creating a test container for our mysql db/server. So we have to initialize that
//We also have to configure it to run, so we should be using the class which I can't recall. But the test itself
//Is fairly simple, we'll arrange the pieces (TestContainer,Repo,MockMvc, and then we'll send act/request)
//Lastly we'll assert to see whether our db has one item at least.