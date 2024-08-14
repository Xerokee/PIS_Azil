package com.activity.pis_azil.models;

import java.io.Serializable;

public class UserModel implements Serializable {
    public int id_korisnika;
    public String ime;
    public String email;
    private String lozinka;
    public boolean admin;
    public String profileImg;
    private UserRoleModel userRole;

    public UserModel(int id_korisnika, String ime, String email, String lozinka, boolean admin, String profileImg) {
        this.id_korisnika = id_korisnika;
        this.ime = ime;
        this.email = email;
        this.lozinka = lozinka;
        this.admin = admin;
        this.profileImg = profileImg;
    }

    public UserModel() {
    }

    // Getters and setters
    public int getIdKorisnika() {
        return id_korisnika;
    }

    public void setIdKorisnika(int idKorisnika) {
        this.id_korisnika = idKorisnika;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String Ime) {
        this.ime = Ime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String Email) {
        this.email = Email;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean Admin) {
        this.admin = Admin;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public UserRoleModel getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRoleModel userRole) {
        this.userRole = userRole;
        this.admin = userRole.isAdmin();
    }
}
