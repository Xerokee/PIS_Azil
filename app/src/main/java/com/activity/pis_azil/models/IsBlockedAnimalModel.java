package com.activity.pis_azil.models;

import java.io.Serializable;
import java.util.List;

public class IsBlockedAnimalModel implements Serializable {
    private int id_ljubimca;
    private int id_udomitelja;
    private String ime_ljubimca;
    private String tip_ljubimca;
    private String opis_ljubimca;
    private boolean udomljen;
    private String datum;
    private String vrijeme;
    private String imgUrl;
    List<String> galerijaZivotinja;
    private boolean stanje_zivotinje;
    private boolean isBlocked;
    private boolean statusUdomljavanja;

    // Constructors, getters, and setters
    public IsBlockedAnimalModel() {}

    public IsBlockedAnimalModel(int id_ljubimca, int id_udomitelja, String ime_ljubimca, String tip_ljubimca, String opis_ljubimca, boolean udomljen, String datum, String vrijeme, String imgUrl, boolean stanje_zivotinje, boolean isBlocked) {
        this.id_ljubimca = id_ljubimca;
        this.id_udomitelja = id_udomitelja;
        this.ime_ljubimca = ime_ljubimca;
        this.tip_ljubimca = tip_ljubimca;
        this.opis_ljubimca = opis_ljubimca;
        this.udomljen = udomljen;
        this.datum = datum;
        this.vrijeme = vrijeme;
        this.imgUrl = imgUrl;
        this.stanje_zivotinje = stanje_zivotinje;
        this.isBlocked = isBlocked;
    }

    // Getters and Setters for all fields
    public int getIdLjubimca() {
        return id_ljubimca;
    }

    public void setIdLjubimca(int id_ljubimca) {
        this.id_ljubimca = id_ljubimca;
    }

    public int getIdUdomitelja() {
        return id_udomitelja;
    }

    public void setIdUdomitelja(int id_udomitelja) {
        this.id_udomitelja = id_udomitelja;
    }

    public String getImeLjubimca() {
        return ime_ljubimca;
    }

    public void setImeLjubimca(String ime_ljubimca) {
        this.ime_ljubimca = ime_ljubimca;
    }

    public String getTipLjubimca() {
        return tip_ljubimca;
    }

    public void setTipLjubimca(String tip_ljubimca) {
        this.tip_ljubimca = tip_ljubimca;
    }

    public String getOpisLjubimca() {
        return opis_ljubimca;
    }

    public void setOpisLjubimca(String opis_ljubimca) {
        this.opis_ljubimca = opis_ljubimca;
    }

    public boolean isUdomljen() {
        return udomljen;
    }

    public void setUdomljen(boolean udomljen) {
        this.udomljen = udomljen;
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

    public List<String> getGalleryImgUrls() {
        return galerijaZivotinja;
    }

    public void setGalleryImgUrls(List<String> galerijaZivotinja) {
        this.galerijaZivotinja = galerijaZivotinja;
    }

    public boolean isStanjeZivotinje() {
        return stanje_zivotinje;
    }

    public void setStanjeZivotinje(boolean stanje_zivotinje) {
        this.stanje_zivotinje = stanje_zivotinje;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public boolean isStatusUdomljavanja() {
        return statusUdomljavanja;
    }

    public void setStatusUdomljavanja(boolean statusUdomljavanja) {
        this.statusUdomljavanja = statusUdomljavanja;
    }

    @Override
    public String toString() {
        return "IsBlockedAnimalModel{" +
                "id_ljubimca=" + id_ljubimca +
                ", id_udomitelja=" + id_udomitelja +
                ", ime_ljubimca='" + ime_ljubimca + '\'' +
                ", tip_ljubimca='" + tip_ljubimca + '\'' +
                ", opis_ljubimca='" + opis_ljubimca + '\'' +
                ", udomljen=" + udomljen +
                ", datum='" + datum + '\'' +
                ", vrijeme='" + vrijeme + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", stanje_zivotinje=" + stanje_zivotinje +
                ", isBlocked=" + isBlocked +  // Dodano novo polje u toString metodu
                '}';
    }
}

