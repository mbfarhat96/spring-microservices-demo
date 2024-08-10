package com.farhat.order_service.exception;


public class ProductOutOfStockException extends RuntimeException{

    public ProductOutOfStockException(String message){
        super(message);
    }

}
