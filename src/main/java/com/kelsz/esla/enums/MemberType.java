package com.kelsz.esla.enums;

/**
 * Enum for Member and Ledger types.
 */
public enum MemberType {
    CHANNEL_3("channel 3"),
    RESORT("resort"),
    EXECUTIVE("executive"),
    CONSULTANT("consultant"),
    OTHER("other");

    private final String value;

    MemberType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
