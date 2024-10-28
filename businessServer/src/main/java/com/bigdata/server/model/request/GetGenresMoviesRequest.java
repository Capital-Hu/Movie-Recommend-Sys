package com.bigdata.server.model.request;

public class GetGenresMoviesRequest {

    private String genres;

    private int num;

    private int skip;

    public GetGenresMoviesRequest(String genres, int num, int skip) {
        this.genres = genres;
        this.num = num;
        this.skip = skip;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }
}
