package com.activity.pis_azil.models;

public class MyAdoptionModel {
    private int idLjubimca;
    private String imeLjubimca;
    private String tipLjubimca;
    private String opisLjubimca;
    private String datum;
    private String imgUrl;
    private boolean udomljen;

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
}
