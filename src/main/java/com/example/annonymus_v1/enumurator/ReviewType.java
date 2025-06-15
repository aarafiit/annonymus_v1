package com.example.annonymus_v1.enumurator;

public enum ReviewType {

    POSITIVE(1L),
    NEGATIVE(0L),
    MIXED(2L);

    private final Long value;

    ReviewType(Long value) {
        this.value = value;
    }

    public Long getDescription() {
        return value;
    }

    // Optional: Override toString to return the description directly
    @Override
    public String toString() {
        return value.toString();
    }
}
