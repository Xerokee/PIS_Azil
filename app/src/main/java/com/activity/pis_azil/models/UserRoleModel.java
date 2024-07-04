package com.activity.pis_azil.models;

public class UserRoleModel {
    private int id_uloge;
    private String naziv_uloge;

    // Getters and setters
    public int getIdUloge() {
        return id_uloge;
    }

    public void setIdUloge(int id_uloge) {
        this.id_uloge = id_uloge;
    }

    public String getNazivUloge() {
        return naziv_uloge;
    }

    public void setNazivUloge(String nazivUloge) {
        this.naziv_uloge = naziv_uloge;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(naziv_uloge);
    }
}
