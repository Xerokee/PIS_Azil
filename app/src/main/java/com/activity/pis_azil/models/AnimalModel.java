package com.activity.pis_azil.models;

import java.io.Serializable;
import java.util.List;

public class AnimalModel implements Serializable {
    private int id_ljubimca;
    private int id_udomitelja;
    private String ime_ljubimca;
    private String tip_ljubimca;
    private String opis_ljubimca;
    private boolean udomljen;
    private boolean inAdoptionProcess;
    private String datum;
    private String vrijeme;
    private String imgUrl;
    private List<GalleryImageModel> galerijaZivotinja;
    private boolean stanje_zivotinje;
    private boolean statusUdomljavanja;
    private boolean zahtjev_udomljen;
    private int dob;
    private String boja;

    // Constructors, getters, and setters
    public AnimalModel() {}

    public AnimalModel(int id_ljubimca, int id_udomitelja, String ime_ljubimca, String tip_ljubimca, String opis_ljubimca, boolean udomljen, boolean zahtjev_udomljen, String datum, String vrijeme, String imgUrl, List<GalleryImageModel> galerijaZivotinja, boolean stanje_zivotinje) {
        this.id_ljubimca = id_ljubimca;
        this.id_udomitelja = id_udomitelja;
        this.ime_ljubimca = ime_ljubimca;
        this.tip_ljubimca = tip_ljubimca;
        this.opis_ljubimca = opis_ljubimca;
        this.udomljen = udomljen;
        this.zahtjev_udomljen = zahtjev_udomljen;
        this.datum = datum;
        this.vrijeme = vrijeme;
        this.imgUrl = imgUrl;
        this.galerijaZivotinja = galerijaZivotinja;
        this.stanje_zivotinje = stanje_zivotinje;
    }

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

    public List<GalleryImageModel> getGalerijaZivotinja() {
        return galerijaZivotinja;
    }

    public void setGalerijaZivotinja(List<GalleryImageModel> galerijaZivotinja) {
        this.galerijaZivotinja = galerijaZivotinja;
    }

    public boolean StanjeZivotinje() {
        return stanje_zivotinje;
    }

    public boolean setStanjeZivotinje(boolean stanje_zivotinje) {
        this.stanje_zivotinje = stanje_zivotinje;
        return stanje_zivotinje;
    }

    public boolean isInAdoptionProcess() {
        return inAdoptionProcess;
    }

    public void setInAdoptionProcess(boolean inAdoptionProcess) {
        this.inAdoptionProcess = inAdoptionProcess;
    }

    public boolean isStatusUdomljavanja() {
        return statusUdomljavanja;
    }

    public void setStatusUdomljavanja(boolean statusUdomljavanja) {
        this.statusUdomljavanja = statusUdomljavanja;
    }

    public boolean isZahtjevUdomljavanja() {
        return zahtjev_udomljen;
    }

    public void setZahtjevUdomljavanja(boolean zahtjev_udomljavanja) {
        this.zahtjev_udomljen = zahtjev_udomljavanja;
    }

    public int getDob() {
        return dob;
    }

    public void setDob(int dob) {
        this.dob = dob;
    }

    public String getBoja() {
        return boja;
    }

    public void setBoja(String boja) {
        this.boja = boja;
    }

    public String getDobCategory() {
        if (dob >= 0 && dob <= 1) {
            return "0 do 1 godina";
        } else if (dob >= 2 && dob <= 5) {
            return "2 do 5 godina";
        } else if (dob >= 6) {
            return "5+ godina";
        } else {
            return "Nepoznato";
        }
    }

    @Override
    public String toString() {
        return "AnimalModel{" +
                "id_ljubimca=" + id_ljubimca +
                ", id_udomitelja=" + id_udomitelja +
                ", ime_ljubimca='" + ime_ljubimca + '\'' +
                ", tip_ljubimca='" + tip_ljubimca + '\'' +
                ", opis_ljubimca='" + opis_ljubimca + '\'' +
                ", udomljen=" + udomljen +
                ", datum='" + datum + '\'' +
                ", vrijeme='" + vrijeme + '\'' +
                '}' +
                ", imgUrl='" + imgUrl + '\'' +
                ", galerijaZivotinja='" + galerijaZivotinja + '\'' +
                ", stanje_zivotinje=" + stanje_zivotinje +
                '}';
    }
}
