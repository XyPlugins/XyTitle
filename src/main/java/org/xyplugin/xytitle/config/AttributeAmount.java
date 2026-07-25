package org.xyplugin.xytitle.config;

public final class AttributeAmount {

    private final String name;
    private final double value;
    private final boolean percentage;

    public AttributeAmount(String name, double value, boolean percentage) {
        this.name = name;
        this.value = value;
        this.percentage = percentage;
    }

    public String name() {
        return name;
    }

    public double value() {
        return value;
    }

    public boolean percentage() {
        return percentage;
    }

    public AttributeAmount add(AttributeAmount other) {
        return new AttributeAmount(name, value + other.value, percentage);
    }

    public String key() {
        return name + "|" + percentage;
    }

    public String toAttributeLine() {
        return name + ": +" + trim(value) + (percentage ? "%" : "");
    }

    private static String trim(double value) {
        if (Math.floor(value) == value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
