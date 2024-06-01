package com.activity.pis_azil.models;

public class AnimalModel {
    private String animalId;
    private String animalName;
    private String animalType;
    private String currentDate;
    private String currentTime;
    private String imgUrl;
    private boolean isAdopted;
    private String adopterId;
    private String adopterName;
    private String documentId;

    // Constructors, getters and setters

    public AnimalModel() {}

    public AnimalModel(String animalId, String animalName, String animalType, String imgUrl, boolean isAdopted) {
        this.animalId = animalId;
        this.animalName = animalName;
        this.animalType = animalType;
        this.imgUrl = imgUrl;
        this.isAdopted = isAdopted;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }

    public String getAnimalName() {
        return animalName;
    }

    public void setAnimalName(String animalName) {
        this.animalName = animalName;
    }

    public String getAnimalType() {
        return animalType;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getImg_url() {
        return imgUrl;
    }

    public void setImg_url(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public boolean isAdopted() {
        return isAdopted;
    }

    public void setAdopted(boolean adopted) {
        isAdopted = adopted;
    }

    public String getAdopterId() {
        return adopterId;
    }

    public void setAdopterId(String adopterId) {
        this.adopterId = adopterId;
    }

    public String getAdopterName() {
        return adopterName;
    }

    public void setAdopterName(String adopterName) {
        this.adopterName = adopterName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
