package com.activity.pis_azil.models;

import com.google.gson.annotations.SerializedName;

public class ViewAllModel {
    @SerializedName("ImeLjubimca")
    private String imeLjubimca;

    @SerializedName("OpisLjubimca")
    private String opisLjubimca;

    @SerializedName("ImgUrl")
    private String imgUrl;

    @SerializedName("TipLjubimca")
    private int tipLjubimca;

    @SerializedName("Udomljen")
    private boolean udomljen;

    @SerializedName("IdUdomitelja")
    private String idUdomitelja;

    @SerializedName("IdLjubimca")
    private String idLjubimca;

    @SerializedName("Dob")
    private int dob;
    @SerializedName("Boja")
    private int boja;

    public ViewAllModel(String imeLjubimca, String opisLjubimca, String imgUrl, int tipLjubimca, int dob, int boja) {
        this.imeLjubimca = imeLjubimca;
        this.opisLjubimca = opisLjubimca;
        this.imgUrl = imgUrl;
        this.tipLjubimca = tipLjubimca;
        this.dob = dob;
        this.boja = boja;
    }

    public String getImeLjubimca() {
        return imeLjubimca;
    }

    public void setImeLjubimca(String imeLjubimca) {
        this.imeLjubimca = imeLjubimca;
    }

    public String getOpisLjubimca() {
        return opisLjubimca;
    }

    public void setOpisLjubimca(String opisLjubimca) {
        this.opisLjubimca = opisLjubimca;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getTipLjubimca() {
        return tipLjubimca;
    }

    public void setTipLjubimca(int tipLjubimca) {
        this.tipLjubimca = tipLjubimca;
    }

    public boolean isUdomljen() {
        return udomljen;
    }

    public void setUdomljen(boolean udomljen) {
        this.udomljen = udomljen;
    }

    public String getIdUdomitelja() {
        return idUdomitelja;
    }

    public void setIdUdomitelja(String idUdomitelja) {
        this.idUdomitelja = idUdomitelja;
    }

    public String getIdLjubimca() {
        return idLjubimca;
    }

    public void setIdLjubimca(String idLjubimca) {
        this.idLjubimca = idLjubimca;
    }

    public int getDob() { return dob; }
    public void setDob(int dob) { this.dob = dob; }

    public int getBoja() { return boja; }
    public void setBoja(int boja) { this.boja = boja; }
}
