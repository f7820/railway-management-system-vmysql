package com.railway.railway_management_system.enums;

public enum SeatClass {
    BUSINESS("B"),
    ECONOMY("E"),
    SLEEPER("S");

    private final String prefix;

    SeatClass(String prefix) { this.prefix = prefix; }

    public String getPrefix() { return prefix; }
}
