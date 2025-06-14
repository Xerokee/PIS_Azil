package com.activity.pis_azil.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UpdateDnevnikModel implements Serializable {

    private int id;
    private int id_ljubimca;
    private int id_korisnika;
    private String ime_ljubimca;
    private String tip_ljubimca;
    private boolean udomljen;
    private String datum;
    private String vrijeme;
    private String imgUrl;
    private boolean stanje_zivotinje;
    private boolean status_udomljavanja;
    private boolean zahtjev_udomljen;
    private String imeUdomitelja;
    private String prezimeUdomitelja;
    private String opisLjubimca;
    private List<String> galleryImgUrls;
    private List<String> activities;
    private boolean isNotificationShown = false;

    public UpdateDnevnikModel() {
        galleryImgUrls = new ArrayList<>();
        activities = new ArrayList<>();
    }

    public UpdateDnevnikModel(int id, int id_ljubimca, int id_korisnika, String ime_ljubimca, String tip_ljubimca, boolean udomljen, String datum, String vrijeme, String imgUrl, boolean stanje_zivotinje, boolean status_udomljavanja, boolean zahtjev_udomljen) {
        this.id = id;
        this.id_ljubimca = id_ljubimca;
        this.id_korisnika = id_korisnika;
        this.ime_ljubimca = ime_ljubimca;
        this.tip_ljubimca = tip_ljubimca;
        this.udomljen = udomljen;
        this.datum = datum;
        this.vrijeme = vrijeme;
        this.imgUrl = imgUrl;
        this.stanje_zivotinje = stanje_zivotinje;
        this.status_udomljavanja = status_udomljavanja;
        this.zahtjev_udomljen = zahtjev_udomljen;
        galleryImgUrls = new ArrayList<>();
        activities = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIme_ljubimca() {
        return ime_ljubimca;
    }

    public void setIme_ljubimca(String ime_ljubimca) {
        this.ime_ljubimca = ime_ljubimca;
    }

    public String getTip_ljubimca() {
        return tip_ljubimca;
    }

    public void setTip_ljubimca(String tip_ljubimca) {
        this.tip_ljubimca = tip_ljubimca;
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

    public boolean isStanje_zivotinje() {
        return stanje_zivotinje;
    }

    public void setStanje_zivotinje(boolean stanje_zivotinje) {
        this.stanje_zivotinje = stanje_zivotinje;
    }

    public boolean isStatus_udomljavanja() {
        return status_udomljavanja;
    }

    public void setStatus_udomljavanja(boolean status_udomljavanja) {
        this.status_udomljavanja = status_udomljavanja;
    }

    public boolean isZahtjev_udomljen() {
        return zahtjev_udomljen;
    }

    public void setZahtjev_udomljen(boolean zahtjev_udomljen) {
        this.zahtjev_udomljen = zahtjev_udomljen;
    }

    public int getId_korisnika() {
        return id_korisnika;
    }

    public void setId_korisnika(int id_korisnika) {
        this.id_korisnika = id_korisnika;
    }

    public int getId_ljubimca() {
        return id_ljubimca;
    }
    public void setId_ljubimca(int id_ljubimca) {
        this.id_ljubimca = id_ljubimca;
    }

    public String getImeUdomitelja() {
        return imeUdomitelja;
    }

    public void setImeUdomitelja(String imeUdomitelja) {
        this.imeUdomitelja = imeUdomitelja;
    }

    public String getPrezimeUdomitelja() {
        return prezimeUdomitelja;
    }

    public void setPrezimeUdomitelja(String prezimeUdomitelja) {
        this.prezimeUdomitelja = prezimeUdomitelja;
    }

    public String getOpisLjubimca() {
        return opisLjubimca;
    }

    public void setOpisLjubimca(String opisLjubimca) {
        this.opisLjubimca = opisLjubimca;
    }

    public List<String> getGalleryImgUrls() {
        return galleryImgUrls;
    }

    public void setGalleryImgUrls(List<String> galleryImgUrls) {
        this.galleryImgUrls = galleryImgUrls;
    }

    public void addGalleryImage(String imageUrl) {
        galleryImgUrls.add(imageUrl);
    }

    public List<String> getActivities() {
        return activities;
    }

    public void addActivity(String activity) {
        activities.add(activity);
    }

    public boolean isNotificationShown() {
        return isNotificationShown;
    }

    public void setNotificationShown(boolean notificationShown) {
        isNotificationShown = notificationShown;
    }
}
