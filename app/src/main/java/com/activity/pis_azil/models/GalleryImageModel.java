package com.activity.pis_azil.models;

import java.io.Serializable;

public class GalleryImageModel implements Serializable {
    private int id;
    private String imgUrl;

    public GalleryImageModel() {}

    public GalleryImageModel(int id, String imgUrl) {
        this.id = id;
        this.imgUrl = imgUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
