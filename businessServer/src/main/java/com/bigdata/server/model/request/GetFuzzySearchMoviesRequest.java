package com.bigdata.server.model.request;

public class GetFuzzySearchMoviesRequest {

    private String query;

    private int num;

    private int skip;

    public GetFuzzySearchMoviesRequest(String query, int num, int skip) {
        this.query = query;
        this.num = num;
        this.skip = skip;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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
