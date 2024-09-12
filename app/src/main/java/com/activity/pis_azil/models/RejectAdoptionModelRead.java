package com.activity.pis_azil.models;

public class RejectAdoptionModelRead {

    private Integer id_korisnika;
    private String ime_ljubimca;

    public RejectAdoptionModelRead() {

    }


    public Integer getId_korisnika() {
        return id_korisnika;
    }

    public void setId_korisnika(Integer id_korisnika) {
        this.id_korisnika = id_korisnika;
    }

    public String getIme_ljubimca() {
        return ime_ljubimca;
    }

    public void setIme_ljubimca(String ime_ljubimca) {
        this.ime_ljubimca = ime_ljubimca;
    }
}
