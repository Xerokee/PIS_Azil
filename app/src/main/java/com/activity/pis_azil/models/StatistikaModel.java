package com.activity.pis_azil.models;

public class StatistikaModel {
    public int raspolozive_zivotinje;
    public int udomljene_zivotinje;
    public int broj_zivotinja;
    public int broj_odbijenih_zahtjeva;
    public int broj_zahtjeva;

    public StatistikaModel(int rz, int uz, int bz, int boz, int br){
        raspolozive_zivotinje = rz;
        udomljene_zivotinje = uz;
        broj_zivotinja = bz;
        broj_odbijenih_zahtjeva = boz;
        broj_zahtjeva = br;
    }

    public int getRaspolozive_zivotinje() {
        return raspolozive_zivotinje;
    }
    public int getUdomljene_zivotinje() {
        return udomljene_zivotinje;
    }
    public int getBroj_zivotinja() {
        return broj_zivotinja;
    }
    public int getBroj_odbijenih_zahtjeva() {
        return broj_odbijenih_zahtjeva;
    }
    public int getBroj_zahtjeva() {
        return broj_zahtjeva;
    }

}
