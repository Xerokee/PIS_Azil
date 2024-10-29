package com.activity.pis_azil.models;

public class SlikaModel {
    public int id;
    public int id_ljubimca;
    public String slika_data;

    public SlikaModel(int i, int ilj, String d){
        id=i;
        id_ljubimca=ilj;
        slika_data=d;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdLjubimca() {
        return id_ljubimca;
    }

    public void setIdLjubimca(int id_ljubimca) {
        this.id_ljubimca = id_ljubimca;
    }
    public String getSlika_data() {return  slika_data;}
    public void setSlika_data(String slika_data) {this.slika_data = slika_data;}
}
