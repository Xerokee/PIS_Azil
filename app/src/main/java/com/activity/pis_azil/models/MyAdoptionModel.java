package com.activity.pis_azil.models;

public class MyAdoptionModel {
    private int idLjubimca;
    private String imeLjubimca;
    private String tipLjubimca;
    private String opisLjubimca;
    private String datum;
    private String vrijeme;
    private String imgUrl;
    private boolean udomljen;
    private boolean stanjeZivotinje;
    private int idKorisnika;

    // Getteri i setteri

    public int getIdLjubimca() {
        return idLjubimca;
    }

    public void setIdLjubimca(int idLjubimca) {
        this.idLjubimca = idLjubimca;
    }

    public String getImeLjubimca() {
        return imeLjubimca;
    }

    public void setImeLjubimca(String imeLjubimca) {
        this.imeLjubimca = imeLjubimca;
    }

    public String getTipLjubimca() {
        return tipLjubimca;
    }

    public void setTipLjubimca(String tipLjubimca) {
        this.tipLjubimca = tipLjubimca;
    }

    public String getOpisLjubimca() {
        return opisLjubimca;
    }

    public void setOpisLjubimca(String opisLjubimca) {
        this.opisLjubimca = opisLjubimca;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(String vrijeme) {
        this.vrijeme = vrijeme;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public boolean isUdomljen() {
        return udomljen;
    }

    public void setUdomljen(boolean udomljen) {
        this.udomljen = udomljen;
    }

    public boolean isStanjeZivotinje() {
        return stanjeZivotinje;
    }

    public void setStanjeZivotinje(boolean stanjeZivotinje) {
        this.stanjeZivotinje = stanjeZivotinje;
    }

    public int getIdKorisnika() {
        return idKorisnika;
    }

    public void setIdKorisnika(int idKorisnika) {
        this.idKorisnika = idKorisnika;
    }

    @Override
    public String toString() {
        return "MyAdoptionModel{" +
                "idLjubimca=" + idLjubimca +
                ", imeLjubimca='" + imeLjubimca + '\'' +
                ", tipLjubimca='" + tipLjubimca + '\'' +
                ", opisLjubimca='" + opisLjubimca + '\'' +
                ", datum='" + datum + '\'' +
                ", vrijeme='" + vrijeme + '\'' +
                '}' +
                ", imgUrl='" + imgUrl + '\'' +
                ", udomljen=" + udomljen +
                ", stanjeZivotinje=" + stanjeZivotinje +
                ", idKorisnika=" + idKorisnika +
                '}';
    }
}
