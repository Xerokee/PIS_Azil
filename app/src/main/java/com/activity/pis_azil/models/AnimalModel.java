package com.activity.pis_azil.models;

public class AnimalModel {
    private int idLjubimca;
    private int idUdomitelja;
    private String imeLjubimca;
    private String tipLjubimca;
    private String opisLjubimca;
    private boolean udomljen;
    private String imgUrl; // Promijenjeno iz int u String za URL

    // Constructors, getters, and setters
    public AnimalModel() {}

    public AnimalModel(int idLjubimca, int idUdomitelja, String imeLjubimca, String tipLjubimca, String opisLjubimca, boolean udomljen, String imgUrl) {
        this.idLjubimca = idLjubimca;
        this.idUdomitelja = idUdomitelja;
        this.imeLjubimca = imeLjubimca;
        this.tipLjubimca = tipLjubimca;
        this.opisLjubimca = opisLjubimca;
        this.udomljen = udomljen;
        this.imgUrl = imgUrl; // Inicijalizacija imgUrl
    }

    // Getteri i setteri
    public int getIdLjubimca() {
        return idLjubimca;
    }

    public void setIdLjubimca(int idLjubimca) {
        this.idLjubimca = idLjubimca;
    }

    public int getIdUdomitelja() {
        return idUdomitelja;
    }

    public void setIdUdomitelja(int idUdomitelja) {
        this.idUdomitelja = idUdomitelja;
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

    public boolean isUdomljen() {
        return udomljen;
    }

    public void setUdomljen(boolean udomljen) {
        this.udomljen = udomljen;
    }

    public String getImgUrl() {
        return imgUrl; // Getter za imgUrl
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl; // Setter za imgUrl
    }
}
