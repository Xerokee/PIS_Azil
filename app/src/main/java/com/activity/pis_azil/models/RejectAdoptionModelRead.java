package com.activity.pis_azil.models;

public class RejectAdoptionModelRead {

    private Integer id;
    private Integer id_korisnika;
    private Integer id_ljubimca;
    private String ime_ljubimca;
    private String tip_ljubimca;
    private String datum;
    private String vrijeme;
    private String imgUrl;
    private boolean stanje_zivotinje;

    public RejectAdoptionModelRead() {}

    public Integer getId_ljubimca() {
        return id_ljubimca;
    }

    public void setId_ljubimca(Integer id_ljubimca) {
        this.id_ljubimca = id_ljubimca;
    }

    public String getTip_ljubimca() {
        return tip_ljubimca;
    }

    public void setTip_ljubimca(String tip_ljubimca) {
        this.tip_ljubimca = tip_ljubimca;
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

    // Postojeći getteri i setteri
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
