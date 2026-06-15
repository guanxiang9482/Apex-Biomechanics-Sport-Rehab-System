package com.apex.domain;

public class Equipment {

    private int itemId;
    private int facilityId;
    private String itemName;
    private EquipmentStatus itemStatus;
    private int itemQuantity;

    public Equipment(int facilityId, String itemName,
                     int itemQuantity) {
        this.facilityId   = facilityId;
        this.itemName     = itemName;
        this.itemQuantity = itemQuantity;
        this.itemStatus   = EquipmentStatus.AVAILABLE;
    }

    public Equipment(int itemId, int facilityId, String itemName,
                     EquipmentStatus itemStatus, int itemQuantity) {
        this.itemId       = itemId;
        this.facilityId   = facilityId;
        this.itemName     = itemName;
        this.itemStatus   = itemStatus;
        this.itemQuantity = itemQuantity;
    }

    public int getItemId()              { return itemId; }
    public int getFacilityId()          { return facilityId; }
    public String getItemName()         { return itemName; }
    public EquipmentStatus getItemStatus(){ return itemStatus; }
    public int getItemQuantity()        { return itemQuantity; }

    public void setItemId(int id)       { this.itemId = id; }
    public void setItemStatus(EquipmentStatus s){ this.itemStatus = s; }
    public void setItemQuantity(int q)  { this.itemQuantity = q; }
    public void setItemName(String n)   { this.itemName = n; }
}
