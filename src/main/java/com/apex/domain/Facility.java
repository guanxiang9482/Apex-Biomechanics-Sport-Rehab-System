package com.apex.domain;

public class Facility {

    private int facilityId;
    private Integer lastUsedByTherapist;
    private String name;
    private String type;
    private FacilityStatus status;
    private String location;

    // Constructor for new facility creation
    public Facility(String name, String type, String location) {
        this.name     = name;
        this.type     = type;
        this.location = location;
        this.status   = FacilityStatus.AVAILABLE;
    }

    // Constructor for loading from database
    public Facility(int facilityId, Integer lastUsedByTherapist,
                    String name, String type,
                    FacilityStatus status, String location) {
        this.facilityId            = facilityId;
        this.lastUsedByTherapist   = lastUsedByTherapist;
        this.name                  = name;
        this.type                  = type;
        this.status                = status;
        this.location              = location;
    }

    // Getters
    public int getFacilityId()              { return facilityId; }
    public Integer getLastUsedByTherapist() { return lastUsedByTherapist; }
    public String getName()                 { return name; }
    public String getType()                 { return type; }
    public FacilityStatus getStatus()       { return status; }
    public String getLocation()             { return location; }

    // Setters
    public void setFacilityId(int id)       { this.facilityId = id; }
    public void setStatus(FacilityStatus s) { this.status = s; }
    public void setLastUsedByTherapist(Integer id) {
        this.lastUsedByTherapist = id;
    }
    public void setLocation(String location){ this.location = location; }
    public void setName(String name)        { this.name = name; }
    public void setType(String type)        { this.type = type; }
}
