package org.example.Service.impl;

import org.example.Model.*;
import org.example.Service.EmergencyService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EmergencyServiceImpl implements EmergencyService {

    private final Object lock = new Object();

    private final List<EmergencyDetails> recorded = new ArrayList<>();
    private final List<EmergencyDetails> analysed = new ArrayList<>();
    private final List<EmergencyDetails> handled = new ArrayList<>();
    private final Map<UnitType, List<ResponseUnit>> freeUnits = new HashMap<>();

    private boolean recordingDone = false;
    private boolean analysisDone = false;

    private final long responseStepMillis;
    private final ExecutorService responders = Executors.newCachedThreadPool();

    public EmergencyServiceImpl(List<ResponseUnit> units) {
        this(units, 1000L);
    }

    public EmergencyServiceImpl(List<ResponseUnit> units, long responseStepMillis) {
        this.responseStepMillis = responseStepMillis;

        for (UnitType type : UnitType.values()) {
            freeUnits.put(type, new ArrayList<>());
        }
        for (ResponseUnit unit : units) {
            freeUnits.get(unit.type()).add(unit);
        }
    }


    @Override
    public EmergencyDetails recordEmergencyDetails(Scanner scanner) {
        String name;
        while (true) {
            System.out.print("\nName of affected person (or 'done' to stop): ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty. Please enter a valid value.");
                continue;
            }
            if (name.equalsIgnoreCase("done")) {
                return null;
            }
            break;
        }

        String description = readRequiredInput(scanner, "Description: ", "Description");
        String location = readRequiredInput(scanner, "Location: ", "Location");

        EmergencyType type = null;
        while (type == null) {
            System.out.println("Type: 1-MEDICAL 2-FIRE 3-SECURITY 4-NATURAL_DISASTER 5-ROAD_ACCIDENT");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                type = EmergencyType.values()[choice - 1];
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                System.out.println("Invalid choice, try again.");
            }
        }

        EmergencyDetails details = new EmergencyDetails();
        details.setName(name);
        details.setDescription(description);
        details.setLocation(location);
        details.setEmergencyType(type);
        details.setDate(LocalDate.now());
        details.setTime(LocalTime.now());
        details.setStatus(EmergencyStatus.REPORTED);
        return details;
    }

    private String readRequiredInput(Scanner scanner, String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println(fieldName + " cannot be empty. Please enter a valid value.");
        }
    }

    @Override
    public void addRecorded(EmergencyDetails emergency) {
        synchronized (lock) {
            recorded.add(emergency);
            lock.notifyAll();
        }
    }

    @Override
    public void finishRecording() {
        synchronized (lock) {
            recordingDone = true;
            lock.notifyAll();
        }
    }



    @Override
    public EmergencyDetails nextRecorded() throws InterruptedException {
        synchronized (lock) {
            while (recorded.isEmpty() && !recordingDone) {
                lock.wait();
            }
            if (recorded.isEmpty()) {
                return null;
            }
            return recorded.remove(0);
        }
    }

    @Override
    public void analyseEmergency(EmergencyDetails emergency) {
        String text = emergency.getDescription().toLowerCase();

        EmergencyPriority priority = switch (emergency.getEmergencyType()) {
            case MEDICAL, NATURAL_DISASTER -> EmergencyPriority.HIGH;
            case FIRE, ROAD_ACCIDENT -> EmergencyPriority.MEDIUM;
            case SECURITY -> EmergencyPriority.LOW;
        };

        if (text.contains("unconscious") || text.contains("trapped")
                || text.contains("bleeding") || text.contains("explosion")) {
            priority = EmergencyPriority.CRITICAL;
        }

        emergency.setPriority(priority);
        emergency.setStatus(EmergencyStatus.ANALYSED);
        log("ANALYSED  " + summary(emergency));
    }

    @Override
    public void addAnalysed(EmergencyDetails emergency) {
        synchronized (lock) {
            analysed.add(emergency);
            lock.notifyAll();
        }
    }

    @Override
    public void finishAnalysis() {
        synchronized (lock) {
            analysisDone = true;
            lock.notifyAll();
        }
    }


    @Override
    public EmergencyDetails nextAnalysed() throws InterruptedException {
        synchronized (lock) {
            while (analysed.isEmpty() && !analysisDone) {
                lock.wait();
            }
            if (analysed.isEmpty()) {
                return null;
            }
            analysed.sort(Comparator.comparing(EmergencyDetails::getPriority).reversed());
            return analysed.remove(0);
        }
    }

    @Override
    public void assignUnit(EmergencyDetails emergency) throws InterruptedException {
        UnitType needed = emergency.getEmergencyType().getRequiredUnit();
        ResponseUnit unit;

        synchronized (lock) {
            List<ResponseUnit> free = freeUnits.get(needed);
            while (free.isEmpty()) {
                lock.wait();
            }
            unit = free.remove(0);
            emergency.setAssignedUnit(unit);
            emergency.setStatus(EmergencyStatus.ASSIGNED);
            handled.add(emergency);
        }
        log("ASSIGNED  " + summary(emergency));

        long millis = (3 + emergency.getPriority().ordinal()) * responseStepMillis;
        responders.submit(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            synchronized (lock) {
                freeUnits.get(needed).add(unit);
                lock.notifyAll();
            }
            log("RELEASED  " + unit.name());
        });
    }


    @Override
    public List<EmergencyDetails> getHandled() {
        synchronized (lock) {
            return new ArrayList<>(handled);
        }
    }

    @Override
    public void shutdown() throws InterruptedException {
        responders.shutdown();
        responders.awaitTermination(30, TimeUnit.SECONDS);
    }


    private String summary(EmergencyDetails e) {
        String unit = (e.getAssignedUnit() == null) ? "-" : e.getAssignedUnit().name();
        return e.getName() + " | " + e.getEmergencyType() + " | " + e.getPriority() + " | " + unit;
    }

    private static synchronized void log(String message) {
        System.out.println("\n[" + Thread.currentThread().getName() + "] " + message);
    }
}
