package com.farhat.order_service.service;

import com.farhat.order_service.dto.InventoryResponse;
import com.farhat.order_service.dto.OrderLineItemsDto;
import com.farhat.order_service.dto.OrderRequest;
import com.farhat.order_service.exception.ProductOutOfStockException;
import com.farhat.order_service.model.Order;
import com.farhat.order_service.model.OrderLineItems;
import com.farhat.order_service.repository.OrderRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;

    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        List<OrderLineItems> orderLineItems = getOrderLineItems(orderRequest);  //Get order line items from the orderRequest
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemsList(orderLineItems);
        List<String> skuCodes = getSkuCodes(order); //Get skuCodes for the order-line items to check whether they're in stock


        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");

        try(Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start())){
            InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                    .allMatch(inventoryResponse -> inventoryResponse.isInStock());

            if(allProductsInStock){
                orderRepository.save(order);
                log.info("Placed an order, order ID is {}",order.getId());
                return "Order placed successfully";
            } else {
                throw new ProductOutOfStockException("Product is out of stock");
            }
        } finally {
            inventoryServiceLookup.end();
        }
        //Call Inventory service, and place order only if it's in stock.



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
