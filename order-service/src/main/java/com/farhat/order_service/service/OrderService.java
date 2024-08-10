package com.farhat.order_service.service;

import com.farhat.order_service.dto.InventoryResponse;
import com.farhat.order_service.dto.OrderLineItemsDto;
import com.farhat.order_service.dto.OrderRequest;
import com.farhat.order_service.exception.ProductOutOfStockException;
import com.farhat.order_service.model.Order;
import com.farhat.order_service.model.OrderLineItems;
import com.farhat.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        List<OrderLineItems> orderLineItems = getOrderLineItems(orderRequest);  //Get order line items from the orderRequest
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemsList(orderLineItems);
        List<String> skuCodes = getSkuCodes(order); //Get skuCodes for the order-line items to check whether they're in stock

        //Call Inventory service, and place order only if it's in stock.
        InventoryResponse[] inventoryResponseArray = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                        .retrieve()
                                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                .allMatch(inventoryResponse -> inventoryResponse.isInStock());

        if(allProductsInStock){
            orderRepository.save(order);
            log.info("Placed an order, order ID is {}",order.getId());
        } else {
            throw new ProductOutOfStockException("Product is out of stock");
        }
    }

    private List<String> getSkuCodes(Order order) {
        return order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();
    }

    private OrderLineItems mapToEntity(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }

    private List<OrderLineItems> getOrderLineItems(OrderRequest orderRequest){
        return orderRequest.getOrderLineItemsDtosList()
                .stream()
                .map(this::mapToEntity)
                .toList();
    }

}
