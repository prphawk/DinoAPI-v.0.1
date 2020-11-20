package br.ufrgs.inf.pet.dinoapi.model.auth.google;

import javax.validation.constraints.NotNull;

public class GoogleAuthRequestModel {
    @NotNull
    private String idToken;

    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    private String pictureUrl;

    public GoogleAuthRequestModel(){ }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
