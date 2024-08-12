package com.farhat.inventory_service;

import com.farhat.inventory_service.model.Inventory;
import com.farhat.inventory_service.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(InventoryRepository inventoryRepository){
		return args -> {
			Inventory inventory1 = new Inventory();
			inventory1.setSkuCode("Celestron_SX_Telescope");
			inventory1.setQuantity(6);

			Inventory inventory2 = new Inventory();
			inventory2.setSkuCode("Pogo_Water_Bottle");
			inventory2.setQuantity(6);

			inventoryRepository.save(inventory1);
			inventoryRepository.save(inventory2);

		};
	}

}
