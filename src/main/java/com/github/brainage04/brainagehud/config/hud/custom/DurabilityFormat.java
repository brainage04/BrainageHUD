package com.github.brainage04.brainagehud.config.hud.custom;

public enum DurabilityFormat {
    NONE("None"),
    ONE_NUMBER("One Number"),
    BOTH_NUMBERS("Both Numbers"),
    PERCENTAGE("Percentage");

    private final String name;

    DurabilityFormat(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
