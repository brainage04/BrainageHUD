package io.github.brainage04.brainagehud.config.hud.custom.armour_info;

public enum DurabilityFormat {
    NONE("None"),
    FIRST_NUMBER("First Number"),
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
