package com.lwohvye.reactive.reactor;

public class BusinessException extends Exception {

    public BusinessException(String s, Throwable original) {
        super(s, original);
    }
}
