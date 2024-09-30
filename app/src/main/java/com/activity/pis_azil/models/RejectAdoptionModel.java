package com.activity.pis_azil.models;

public class RejectAdoptionModel {

    private Integer IdKorisnika;
    private String ImeLjubimca;
    private Integer IdLjubimca;
    // private boolean zahtjev_udomljen;

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

    public Integer getIdLjubimca() {
        return IdLjubimca;
    }

    public void setIdLjubimca(Integer idLjubimca) {
        IdLjubimca = idLjubimca;
    }

    /*
    public boolean isZahtjev_udomljen() {
        return zahtjev_udomljen;
    }

    public void setZahtjev_udomljen(boolean zahtjev_udomljen) {
        this.zahtjev_udomljen = zahtjev_udomljen;
    }
    */
}
