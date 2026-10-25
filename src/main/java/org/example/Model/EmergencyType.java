package org.example.Model;

public enum EmergencyType {
    MEDICAL(UnitType.AMBULANCE),
    FIRE(UnitType.FIRE_TRUCK),
    SECURITY(UnitType.POLICE_UNIT),
    NATURAL_DISASTER(UnitType.DISASTER_RESPONSE_TEAM),
    ROAD_ACCIDENT(UnitType.AMBULANCE);

    private final UnitType requiredUnit;

    EmergencyType(UnitType requiredUnit) {
        this.requiredUnit = requiredUnit;
    }

    public UnitType getRequiredUnit() {
        return requiredUnit;
    }
}
