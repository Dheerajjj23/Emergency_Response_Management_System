package org.example.EmergencyServiceTest;

import org.example.Model.*;
import org.example.Service.impl.EmergencyServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 5, unit = TimeUnit.SECONDS)
class EmergencyServiceImplTest {

    private EmergencyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = newService(10,
                new ResponseUnit("Ambulance-1", UnitType.AMBULANCE),
                new ResponseUnit("FireTruck-1", UnitType.FIRE_TRUCK),
                new ResponseUnit("Police-1", UnitType.POLICE_UNIT),
                new ResponseUnit("Disaster-Team-1", UnitType.DISASTER_RESPONSE_TEAM));
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        service.shutdown();
    }


    @Test
    void recordEmergencyDetails_validInput_buildsDetails() {
        Scanner scanner = new Scanner("Ravi\nPerson unconscious\nShahdara\n1\n");

        EmergencyDetails result = service.recordEmergencyDetails(scanner);

        assertNotNull(result);
        assertEquals("Ravi", result.getName());
        assertEquals("Person unconscious", result.getDescription());
        assertEquals("Shahdara", result.getLocation());
        assertEquals(EmergencyType.MEDICAL, result.getEmergencyType());
        assertEquals(EmergencyStatus.REPORTED, result.getStatus());
        assertNotNull(result.getDate());
        assertNotNull(result.getTime());
    }

    @Test
    void recordEmergencyDetails_typingDone_returnsNull() {
        assertNull(service.recordEmergencyDetails(new Scanner("done\n")));
    }

    @Test
    void recordEmergencyDetails_blankValues_areRejectedUntilValid() {
        Scanner scanner = new Scanner("\nRavi\n\nPerson unconscious\n\nShahdara\n1\n");

        EmergencyDetails result = service.recordEmergencyDetails(scanner);

        assertNotNull(result);
        assertEquals("Ravi", result.getName());
        assertEquals("Person unconscious", result.getDescription());
        assertEquals("Shahdara", result.getLocation());
    }

    @Test
    void recordEmergencyDetails_invalidTypeChoices_asksAgainUntilValid() {
        Scanner scanner = new Scanner("Ravi\nBurning shop\nKarol Bagh\nabc\n9\n0\n2\n");

        EmergencyDetails result = service.recordEmergencyDetails(scanner);

        assertEquals(EmergencyType.FIRE, result.getEmergencyType());
    }


    @ParameterizedTest
    @CsvSource({
            "MEDICAL,          Chest pain,   HIGH",
            "NATURAL_DISASTER, Flooding,     HIGH",
            "FIRE,             Shop fire,    MEDIUM",
            "ROAD_ACCIDENT,    Collision,    MEDIUM",
            "SECURITY,         Break-in,     LOW"
    })
    void analyseEmergency_assignsPriorityByType(EmergencyType type, String description,
                                                EmergencyPriority expected) {
        EmergencyDetails e = emergency("Test", type, description, null);

        service.analyseEmergency(e);

        assertEquals(expected, e.getPriority());
        assertEquals(EmergencyStatus.ANALYSED, e.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Person unconscious", "Man TRAPPED inside", "Heavy bleeding", "Explosion heard"})
    void analyseEmergency_criticalKeywords_overrideTypePriority(String description) {
        EmergencyDetails e = emergency("Test", EmergencyType.SECURITY, description, null);

        service.analyseEmergency(e);

        assertEquals(EmergencyPriority.CRITICAL, e.getPriority());
    }


    @Test
    void nextRecorded_returnsEmergenciesInArrivalOrder() throws InterruptedException {
        service.addRecorded(emergency("first", EmergencyType.FIRE, "a", null));
        service.addRecorded(emergency("second", EmergencyType.FIRE, "b", null));

        assertEquals("first", service.nextRecorded().getName());
        assertEquals("second", service.nextRecorded().getName());
    }

    @Test
    void nextRecorded_whenRecordingFinishedAndEmpty_returnsNull() throws InterruptedException {
        service.finishRecording();

        assertNull(service.nextRecorded());
    }

    @Test
    void nextRecorded_whenFinishedButItemsRemain_stillDrainsThem() throws InterruptedException {
        EmergencyDetails e = emergency("left-over", EmergencyType.FIRE, "a", null);
        service.addRecorded(e);
        service.finishRecording();

        assertSame(e, service.nextRecorded());
        assertNull(service.nextRecorded());
    }

    @Test
    void nextRecorded_blocksUntilSomethingIsAdded() throws Exception {
        CompletableFuture<EmergencyDetails> future = async(service::nextRecorded);

        assertThrows(TimeoutException.class, () -> future.get(150, TimeUnit.MILLISECONDS));

        EmergencyDetails e = emergency("late", EmergencyType.MEDICAL, "a", null);
        service.addRecorded(e);

        assertSame(e, future.get(2, TimeUnit.SECONDS));
    }


    @Test
    void nextAnalysed_returnsMostUrgentFirst() throws InterruptedException {
        service.addAnalysed(emergency("low", EmergencyType.SECURITY, "a", EmergencyPriority.LOW));
        service.addAnalysed(emergency("critical", EmergencyType.MEDICAL, "b", EmergencyPriority.CRITICAL));
        service.addAnalysed(emergency("medium", EmergencyType.FIRE, "c", EmergencyPriority.MEDIUM));

        assertEquals("critical", service.nextAnalysed().getName());
        assertEquals("medium", service.nextAnalysed().getName());
        assertEquals("low", service.nextAnalysed().getName());
    }

    @Test
    void nextAnalysed_samePriority_keepsArrivalOrder() throws InterruptedException {
        service.addAnalysed(emergency("earlier", EmergencyType.MEDICAL, "a", EmergencyPriority.HIGH));
        service.addAnalysed(emergency("later", EmergencyType.MEDICAL, "b", EmergencyPriority.HIGH));

        assertEquals("earlier", service.nextAnalysed().getName());
        assertEquals("later", service.nextAnalysed().getName());
    }

    @Test
    void nextAnalysed_whenAnalysisFinishedAndEmpty_returnsNull() throws InterruptedException {
        service.finishAnalysis();

        assertNull(service.nextAnalysed());
    }


    @ParameterizedTest
    @CsvSource({
            "MEDICAL,          AMBULANCE",
            "ROAD_ACCIDENT,    AMBULANCE",
            "FIRE,             FIRE_TRUCK",
            "SECURITY,         POLICE_UNIT",
            "NATURAL_DISASTER, DISASTER_RESPONSE_TEAM"
    })
    void assignUnit_picksUnitOfTheRequiredType(EmergencyType type, UnitType expectedUnitType)
            throws InterruptedException {
        EmergencyDetails e = emergency("Test", type, "a", EmergencyPriority.LOW);

        service.assignUnit(e);

        assertNotNull(e.getAssignedUnit());
        assertEquals(expectedUnitType, e.getAssignedUnit().type());
        assertEquals(EmergencyStatus.ASSIGNED, e.getStatus());
    }

    @Test
    void assignUnit_addsEmergencyToHandledList() throws InterruptedException {
        EmergencyDetails e = emergency("Test", EmergencyType.FIRE, "a", EmergencyPriority.LOW);

        service.assignUnit(e);

        assertEquals(1, service.getHandled().size());
        assertSame(e, service.getHandled().get(0));
    }

    @Test
    void assignUnit_whenAllUnitsBusy_waitsUntilOneIsReleased() throws Exception {
        EmergencyServiceImpl oneAmbulance =
                newService(200, new ResponseUnit("Ambulance-1", UnitType.AMBULANCE));
        EmergencyDetails first = emergency("A", EmergencyType.MEDICAL, "a", EmergencyPriority.LOW);
        EmergencyDetails second = emergency("B", EmergencyType.ROAD_ACCIDENT, "b", EmergencyPriority.LOW);

        oneAmbulance.assignUnit(first);

        CompletableFuture<Void> secondAssignment = CompletableFuture.runAsync(() -> {
            try {
                oneAmbulance.assignUnit(second);
            } catch (InterruptedException ex) {
                throw new CompletionException(ex);
            }
        });

        assertThrows(TimeoutException.class, () -> secondAssignment.get(100, TimeUnit.MILLISECONDS));

        secondAssignment.get(3, TimeUnit.SECONDS);
        assertEquals("Ambulance-1", second.getAssignedUnit().name());

        oneAmbulance.shutdown();
    }


    @Test
    void getHandled_returnsACopy_soCallersCannotModifyInternalState() throws InterruptedException {
        service.assignUnit(emergency("Test", EmergencyType.FIRE, "a", EmergencyPriority.LOW));

        List<EmergencyDetails> copy = service.getHandled();
        copy.clear();

        assertEquals(1, service.getHandled().size());
    }


    @Test
    void pipeline_oneEmergencyFlowsThroughAllStages() throws InterruptedException {
        EmergencyDetails e = emergency("Meena", EmergencyType.FIRE, "Person trapped in fire", null);

        service.addRecorded(e);
        EmergencyDetails fromRecorded = service.nextRecorded();
        service.analyseEmergency(fromRecorded);
        service.addAnalysed(fromRecorded);
        EmergencyDetails fromAnalysed = service.nextAnalysed();
        service.assignUnit(fromAnalysed);

        assertEquals(EmergencyPriority.CRITICAL, e.getPriority());     // "trapped"
        assertEquals("FireTruck-1", e.getAssignedUnit().name());
        assertEquals(EmergencyStatus.ASSIGNED, e.getStatus());
    }


    private EmergencyServiceImpl newService(long responseStepMillis, ResponseUnit... units) {
        List<ResponseUnit> list = new ArrayList<>();
        for (ResponseUnit unit : units) {
            list.add(unit);
        }
        return new EmergencyServiceImpl(list, responseStepMillis);
    }

    private EmergencyDetails emergency(String name, EmergencyType type, String description,
                                       EmergencyPriority priority) {
        EmergencyDetails e = new EmergencyDetails();
        e.setName(name);
        e.setEmergencyType(type);
        e.setDescription(description);
        e.setLocation("Delhi");
        e.setPriority(priority);
        return e;
    }

    private <T> CompletableFuture<T> async(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        });
    }
}
