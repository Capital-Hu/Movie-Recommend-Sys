package com.bigdata.server.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Movie {
    @JsonIgnore
    private int _id;

    private int mid;

    private String name;

    private String descri;

    private String timelong;

    private String issue;

    private String shoot;

    private String language;

    private String directors;

    private String actors;

    private String genres;

    private double score;

    public Movie() {
    }

    public Movie(int mid, String name, String descri, String timelong, String issue, String shoot, String language, String directors, String actors, String genres, double score) {
        this.mid = mid;
        this.name = name;
        this.descri = descri;
        this.timelong = timelong;
        this.issue = issue;
        this.shoot = shoot;
        this.language = language;
        this.directors = directors;
        this.actors = actors;
        this.genres = genres;
        this.score = score;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescri() {
        return descri;
    }

    public void setDescri(String descri) {
        this.descri = descri;
    }

    public String getTimelong() {
        return timelong;
    }

    public void setTimelong(String timelong) {
        this.timelong = timelong;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getShoot() {
        return shoot;
    }

    public void setShoot(String shoot) {
        this.shoot = shoot;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDirectors() {
        return directors;
    }

    public void setDirectors(String directors) {
        this.directors = directors;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
