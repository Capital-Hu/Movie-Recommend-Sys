package com.bigdata.server.model.request;

public class GetHybridRecommendationRequest {

    private double cfShare;

    private int mid;

    private int num;

    public GetHybridRecommendationRequest(double cfShare, int mid, int num) {
        this.cfShare = cfShare;
        this.mid = mid;
        this.num = num;
    }

    public double getCfShare() {
        return cfShare;
    }

    public void setCfShare(double cfShare) {
        this.cfShare = cfShare;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
