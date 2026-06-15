package com.apex.domain;

import java.time.LocalDateTime;

public class Administrator extends User {

    private int adminId;
    private String department;

    // Constructor for new admin creation
    public Administrator(String username, String password,
                         String email, String fullname, String contact,
                         String department) {
        super(username, password, email, Role.ADMIN, fullname, contact);
        this.department = department;
    }

    // Constructor for loading from database
    public Administrator(int userId, String username, String password,
                         String email, boolean isActive,
                         LocalDateTime lastActive, LocalDateTime createdAt,
                         String fullname, String contact, String department, int adminId) {
        super(userId, username, password, email, Role.ADMIN,
              fullname, contact, lastActive, createdAt, isActive);
        this.department = department;
        this.adminId = adminId;
    }

    // LSP contract fulfillment
    @Override
    public boolean login(String password) {
        return this.isActive() &&
               this.getPassword().equals(password);
    }

    @Override
    public void resetPassword(String newpassword) {
        setPassword(newpassword);
    }

    // Getters
    public String getDepartment() { return department; }
    public int getAdminId()    { return adminId; }

    // Setters
    public void setDepartment(String department) { this.department = department; }
    public void setAdminId(int adminId) {
        if (adminId <= 0) throw new IllegalArgumentException(
            "Admin ID must be positive.");
        this.adminId = adminId;
    }
}
