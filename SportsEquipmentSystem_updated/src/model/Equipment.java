package model;

public class Equipment {
    private String equipmentId;
    private String name;
    private String category;       // e.g., "Cricket", "Football", "Tennis"
    private String status;         // "AVAILABLE", "BORROWED", "REPAIR"
    private int totalQuantity;
    private int availableQuantity;

    public Equipment(String equipmentId, String name, String category, int quantity) {
        this.equipmentId      = equipmentId;
        this.name             = name;
        this.category         = category;
        this.totalQuantity    = quantity;
        this.availableQuantity = quantity;
        this.status           = "AVAILABLE";
    }

    // ---- Getters ----
    public String getEquipmentId()      { return equipmentId; }
    public String getName()             { return name; }
    public String getCategory()         { return category; }
    public String getStatus()           { return status; }
    public int getTotalQuantity()       { return totalQuantity; }
    public int getAvailableQuantity()   { return availableQuantity; }

    // ---- Setters ----
    public void setStatus(String status) { this.status = status; }

    public boolean isAvailable() {
        return availableQuantity > 0 && !status.equals("REPAIR");
    }

    public void decreaseAvailable() {
        if (availableQuantity > 0) {
            availableQuantity--;
            if (availableQuantity == 0) status = "BORROWED";
        }
    }

    public void increaseAvailable() {
        if (availableQuantity < totalQuantity) {
            availableQuantity++;
            if (!status.equals("REPAIR")) status = "AVAILABLE";
        }
    }

    @Override
    public String toString() {
        return "[" + equipmentId + "] " + name + " (" + category + ") - Qty: "
                + availableQuantity + "/" + totalQuantity + " | " + status;
    }
}
