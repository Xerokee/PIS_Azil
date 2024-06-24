package com.activity.pis_azil.models;

import java.io.Serializable;

public class UserRoleModel implements Serializable {
    private int idUloge;
    private String nazivUloge;

    // Getters and setters
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
}
