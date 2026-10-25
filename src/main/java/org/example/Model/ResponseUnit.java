package org.example.Model;

public record ResponseUnit(String name, UnitType type) {

    @Override
    public String toString() {
        return name;
    }
}
