package org.example.Service;

import org.example.Model.EmergencyDetails;

import java.util.List;
import java.util.Scanner;

public interface EmergencyService {


    EmergencyDetails recordEmergencyDetails(Scanner scanner);

    void addRecorded(EmergencyDetails emergency);

    void finishRecording();

    EmergencyDetails nextRecorded() throws InterruptedException;

    void analyseEmergency(EmergencyDetails emergency);

    void addAnalysed(EmergencyDetails emergency);

    void finishAnalysis();

    EmergencyDetails nextAnalysed() throws InterruptedException;

    void assignUnit(EmergencyDetails emergency) throws InterruptedException;

    List<EmergencyDetails> getHandled();

    void shutdown() throws InterruptedException;
}
