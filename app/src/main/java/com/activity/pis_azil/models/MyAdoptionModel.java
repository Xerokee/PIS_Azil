package com.activity.pis_azil.models;
import java.io.Serializable;

public class MyAdoptionModel implements Serializable {
    private int id;
    private int idLjubimca;
    private String ime_ljubimca;
    private String tip_ljubimca;
    private String opisLjubimca;
    private String datum;
    private String vrijeme;
    private String imgUrl;
    private boolean udomljen;
    private boolean zahtjev_udomljen;
    private boolean stanje_zivotinje;
    private int id_korisnika;
    private boolean status_udomljavanja;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdLjubimca() {
        return idLjubimca;
    }

    public void setIdLjubimca(int idLjubimca) {
        this.idLjubimca = idLjubimca;
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
        return stanje_zivotinje;
    }

    public void setStanjeZivotinje(boolean stanje_zivotinje) {
        this.stanje_zivotinje = stanje_zivotinje;
    }

    public int getIdKorisnika() {
        return id_korisnika;
    }

    public void setIdKorisnika(int id_korisnika) {
        this.id_korisnika = id_korisnika;
    }

    public boolean isStatusUdomljavanja() {
        return status_udomljavanja;
    }

    public void setStatusUdomljavanja(boolean status_udomljavanja) {
        this.status_udomljavanja = status_udomljavanja;
    }

    public boolean isZahtjevUdomljavanja() {
        return zahtjev_udomljen;
    }

    public void setZahtjevUdomljavanja(boolean zahtjev_udomljavanja) {
        this.zahtjev_udomljen = zahtjev_udomljavanja;
    }

    @Override
    public String toString() {
        return "MyAdoptionModel{" +
                "idLjubimca=" + idLjubimca +
                ", ime_ljubimca='" + ime_ljubimca + '\'' +
                ", tip_tjubimca='" + tip_ljubimca + '\'' +
                ", opisLjubimca='" + opisLjubimca + '\'' +
                ", datum='" + datum + '\'' +
                ", vrijeme='" + vrijeme + '\'' +
                '}' +
                ", imgUrl='" + imgUrl + '\'' +
                ", udomljen=" + udomljen +
                ", stanje_zivotinje=" + stanje_zivotinje +
                ", id_korisnika=" + id_korisnika +
                ", status_udomljavanja=" + status_udomljavanja +
                '}';
    }
}
