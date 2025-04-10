package io.github.brainage04.brainagehud.config.hud.custom.keystrokes;

public enum ClicksPerSecondFormat {
    NONE("None"),
    LEFT_CLICK("Left Click"),
    RIGHT_CLICK("Right Click"),
    BOTH("Both");

    private final String name;

    ClicksPerSecondFormat(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
