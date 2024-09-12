package com.activity.pis_azil.models;

public class RejectAdoptionModel {

    private Integer IdKorisnika;
    private String ImeLjubimca;

    public RejectAdoptionModel() {

    }

    public String getImeLjubimca() {
        return ImeLjubimca;
    }

    public void setImeLjubimca(String imeLjubimca) {
        ImeLjubimca = imeLjubimca;
    }

    public Integer getIdKorisnika() {
        return IdKorisnika;
    }

    public void setIdKorisnika(Integer idKorisnika) {
        IdKorisnika = idKorisnika;
    }
}
