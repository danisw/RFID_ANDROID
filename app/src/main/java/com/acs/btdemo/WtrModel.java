package com.acs.btdemo;

import com.google.gson.annotations.SerializedName;

public class WtrModel {
    @SerializedName("id_wtr")
    private String id_wtr;
    @SerializedName("no_WTR")
    private String title;
    @SerializedName("waktu")
    private String tanggal_wtr;
    @SerializedName("epoch")
    private String epoch;
    @SerializedName("presentase")
    private String presentase;
    @SerializedName("request_qty")
    private String request_qty;
    @SerializedName("Approved_qty")
    private String Approved_qty;

    public String getEpoch() {
        return epoch;
    }

    public void setEpoch(String epoch) {
        this.epoch = epoch;
    }

    public WtrModel(String title, String tanggal_wtr, String epoch, String id_wtr, String presentase,
                    String request_qty, String Approved_qty) {
        this.title = title;
        this.tanggal_wtr=tanggal_wtr;
        this.epoch=epoch;
        this.id_wtr=id_wtr;
        this.presentase=presentase;
        this.request_qty=request_qty;
        this.Approved_qty=Approved_qty;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTanggal_wtr() {
        return tanggal_wtr;
    }

    public void setTanggal_wtr(String tanggal_wtr) {
        this.tanggal_wtr = tanggal_wtr;
    }

    public String getId_wtr() {
        return id_wtr;
    }

    public void setId_wtr(String id_wtr) {
        this.id_wtr = id_wtr;
    }

    public String getPresentase() {
        return presentase;
    }

    public void setPresentase(String presentase) {
        this.presentase = presentase;
    }

    public String getRequest_qty() {
        return request_qty;
    }

    public void setRequest_qty(String request_qty) {
        this.request_qty = request_qty;
    }

    public String getApproved_qty() {
        return Approved_qty;
    }

    public void setApproved_qty(String approved_qty) {
        Approved_qty = approved_qty;
    }
}
