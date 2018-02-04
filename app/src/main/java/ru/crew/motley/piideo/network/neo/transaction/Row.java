package ru.crew.motley.piideo.network.neo.transaction;

public class Row {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value == null ? "Value is empty" : value;
    }
}
