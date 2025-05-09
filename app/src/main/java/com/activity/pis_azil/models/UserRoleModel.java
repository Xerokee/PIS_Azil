package com.activity.pis_azil.models;

public class UserRoleModel {
    public int idUloge;
    public String nazivUloge;

    public int getIdUloge() {
        return idUloge;
    }

    public void setIdUloge(int idUloge) {
        this.idUloge = idUloge;
    }

    public String getNazivUloge() {
        return nazivUloge;
    }

    public void setNazivUloge(String nazivUloge) {
        this.nazivUloge = nazivUloge;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(nazivUloge);
    }
}
