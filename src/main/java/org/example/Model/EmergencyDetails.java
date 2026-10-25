package org.example.Model;


import java.time.LocalDate;
import java.time.LocalTime;

public class EmergencyDetails {

    private String name;
    private String description;
    private EmergencyType emergencyType;
    private String location;
    private LocalDate date;
    private LocalTime time;
    private EmergencyPriority priority;
    private ResponseUnit assignedUnit;
    private EmergencyStatus status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EmergencyType getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(EmergencyType emergencyType) {
        this.emergencyType = emergencyType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public EmergencyPriority getPriority() {
        return priority;
    }

    public void setPriority(EmergencyPriority priority) {
        this.priority = priority;
    }

    public ResponseUnit getAssignedUnit() {
        return assignedUnit;
    }

    public void setAssignedUnit(ResponseUnit assignedUnit) {
        this.assignedUnit = assignedUnit;
    }

    public EmergencyStatus getStatus() {
        return status;
    }

    public void setStatus(EmergencyStatus status) {
        this.status = status;
    }

    public EmergencyDetails(String name, String description, EmergencyType emergencyType, String location, LocalDate date, LocalTime time, EmergencyPriority priority, ResponseUnit assignedUnit, EmergencyStatus status) {
        this.name = name;
        this.description = description;
        this.emergencyType = emergencyType;
        this.location = location;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.assignedUnit = assignedUnit;
        this.status = status;
    }

    public EmergencyDetails() {
    }

}
