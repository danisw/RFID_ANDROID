package com.acs.btdemo;

import com.google.gson.annotations.SerializedName;

public class ItemWhtModel {
    @SerializedName("kode_barang")
    private String title;
    @SerializedName("uid_picked")
    private String uid;
    @SerializedName("qty")
    private String qty;


    public ItemWhtModel(String judul, String uid, String qty) {
        this.title=judul;
        this.uid=uid;
        this.qty=qty;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public ItemWhtModel(String title) {
        this.title = title;
    }

}
