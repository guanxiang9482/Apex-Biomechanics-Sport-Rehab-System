package com.apex.domain;

public class Facility {

    private int facilityId;
    private String facilityName;
    private String facilityType;
    private boolean isAvailable;
    private String notes;

    public Facility(String facilityName, String facilityType) {
        this.facilityName = facilityName;
        this.facilityType = facilityType;
        this.isAvailable  = true;
    }

    public Facility(int facilityId, String facilityName,
                    String facilityType, boolean isAvailable,
                    String notes) {
        this.facilityId   = facilityId;
        this.facilityName = facilityName;
        this.facilityType = facilityType;
        this.isAvailable  = isAvailable;
        this.notes        = notes;
    }

    public int getFacilityId()      { return facilityId; }
    public String getFacilityName() { return facilityName; }
    public String getFacilityType() { return facilityType; }
    public boolean isAvailable()    { return isAvailable; }
    public String getNotes()        { return notes; }

    public void setFacilityId(int facilityId)       { this.facilityId = facilityId; }
    public void setAvailable(boolean available)      { this.isAvailable = available; }
    public void setNotes(String notes)               { this.notes = notes; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }
    public void setFacilityType(String facilityType) { this.facilityType = facilityType; }
}
