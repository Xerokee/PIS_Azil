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
    private String tipLjubimca;

    @SerializedName("Udomljen")
    private boolean udomljen;

    @SerializedName("IdUdomitelja")
    private String idUdomitelja;

    @SerializedName("IdLjubimca")
    private String idLjubimca;

    public ViewAllModel(String imeLjubimca, String opisLjubimca, String imgUrl, String tipLjubimca) {
        this.imeLjubimca = imeLjubimca;
        this.opisLjubimca = opisLjubimca;
        this.imgUrl = imgUrl;
        this.tipLjubimca = tipLjubimca;
    }

    // Getters and setters
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

    public String getTipLjubimca() {
        return tipLjubimca;
    }

    public void setTipLjubimca(String tipLjubimca) {
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
}
