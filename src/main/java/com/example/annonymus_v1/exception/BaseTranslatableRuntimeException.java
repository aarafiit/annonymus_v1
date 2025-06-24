package com.example.annonymus_v1.exception;

import lombok.Getter;

@Getter
public class BaseTranslatableRuntimeException extends BaseRuntimeException {
    private final String messageCode;
    private final String fallbackMessage;
    private final Object[] arguments;

    public BaseTranslatableRuntimeException(
            String messageCode,
            String fallbackMessage,
            Object[] arguments) {
        this.messageCode = messageCode;
        this.fallbackMessage = fallbackMessage;
        this.arguments = arguments;
    }

    public BaseTranslatableRuntimeException(String messageCode) {
        this.messageCode = messageCode;
        this.fallbackMessage = messageCode;
        this.arguments = null;
    }

    @Override
    public String getMessage() {
        return fallbackMessage;
    }
}
