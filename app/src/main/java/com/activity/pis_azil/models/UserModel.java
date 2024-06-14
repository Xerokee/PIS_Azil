package com.activity.pis_azil.models;

import java.io.Serializable;

public class UserModel implements Serializable {
    public int id_korisnika;
    public String ime;
    public String email;
    private String lozinka;
    public boolean admin;
    public String profileImg;

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
}
