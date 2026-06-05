package main.java.Services.Facade;

public interface AdmissionFacade {
    boolean admitNewAthlete(int athleteId);
    String getAdmissionStatus();
    void rollBackAdmission();
}
