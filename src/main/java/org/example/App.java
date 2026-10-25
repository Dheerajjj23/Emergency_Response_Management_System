package org.example;

import org.example.Model.EmergencyDetails;
import org.example.Model.ResponseUnit;
import org.example.Model.UnitType;
import org.example.Service.EmergencyService;
import org.example.Service.impl.EmergencyServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.*;

public class App 
{
    public static void main( String[] args ) throws InterruptedException
    {
        List<ResponseUnit> units = new ArrayList<>();
        units.add(new ResponseUnit("Ambulance-1", UnitType.AMBULANCE));
        units.add(new ResponseUnit("Ambulance-2", UnitType.AMBULANCE));
        units.add(new ResponseUnit("Ambulance-3", UnitType.AMBULANCE));
        units.add(new ResponseUnit("FireTruck-1", UnitType.FIRE_TRUCK));
        units.add(new ResponseUnit("FireTruck-2", UnitType.FIRE_TRUCK));
        units.add(new ResponseUnit("Police-1", UnitType.POLICE_UNIT));
        units.add(new ResponseUnit("Police-2", UnitType.POLICE_UNIT));
        units.add(new ResponseUnit("Disaster-Team-1", UnitType.DISASTER_RESPONSE_TEAM));
        units.add(new ResponseUnit("Disaster-Team-2", UnitType.DISASTER_RESPONSE_TEAM));

        EmergencyService service = new EmergencyServiceImpl(units);

        ExecutorService pool = Executors.newFixedThreadPool(3);

        pool.submit(() -> {
            Scanner scanner = new Scanner(System.in);
            try {
                EmergencyDetails emergency;
                while ((emergency = service.recordEmergencyDetails(scanner)) != null) {
                    service.addRecorded(emergency);
                }
            } catch (NoSuchElementException ex) {
            } finally {
                service.finishRecording();
            }
        });

        pool.submit(() -> {
            try {
                EmergencyDetails emergency;
                while ((emergency = service.nextRecorded()) != null) {
                    service.analyseEmergency(emergency);
                    service.addAnalysed(emergency);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                service.finishAnalysis();
            }
        });

        pool.submit(() -> {
            try {
                EmergencyDetails emergency;
                while ((emergency = service.nextAnalysed()) != null) {
                    service.assignUnit(emergency);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.MINUTES);
        service.shutdown();

        System.out.println("\n--- All handled emergencies ---");
        for (EmergencyDetails e : service.getHandled()) {
            System.out.printf("%-10s %-17s %-9s -> %s%n",
                    e.getName(), e.getEmergencyType(), e.getPriority(), e.getAssignedUnit().name());
        }
    }

}
