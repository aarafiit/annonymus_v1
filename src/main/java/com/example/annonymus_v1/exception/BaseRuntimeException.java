package com.example.annonymus_v1.exception;


public class BaseRuntimeException extends RuntimeException {

    public BaseRuntimeException() {}

    public BaseRuntimeException(String s) {
        super(s);
    }

    public BaseRuntimeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public BaseRuntimeException(Throwable throwable) {
        super(throwable);
    }

    public BaseRuntimeException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
