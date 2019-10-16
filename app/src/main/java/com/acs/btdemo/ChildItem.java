package com.acs.btdemo;
import com.google.gson.annotations.SerializedName;


class ChildItem {
//    @SerializedName("kode_barang")
//    private String title;
    @SerializedName("no_WTR")
    private String no_WTR;
    @SerializedName("kode_barang")
    private String kode_barang;
    @SerializedName("nama_barang")
    private String nama_barang;
    @SerializedName("qty")
    private String qty;
//    @SerializedName("uid")
 //   private String uid;
    @SerializedName("picked_qty")
    private String picked_qty;
    @SerializedName("sisa_qty")
    private String sisa_qty;


    public ChildItem(String no_WTR, String kode_barang, String nama_barang, String qty, String picked_qty , String sisa_qty) {
     //   this.title = title;
        this.no_WTR=no_WTR;
        this.kode_barang=kode_barang;
        this.nama_barang=nama_barang;
        this.qty=qty;
        //this.uid=uid;
        this.picked_qty=picked_qty;
        this.sisa_qty=sisa_qty;
    }

//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }


    public String getNo_WTR() {
        return no_WTR;
    }

    public void setNo_WTR(String no_WTR) {
        this.no_WTR = no_WTR;
    }

    public String getKode_barang() {
        return kode_barang;
    }

    public void setKode_barang(String kode_barang) {
        this.kode_barang = kode_barang;
    }

    public String getNama_barang() {
        return nama_barang;
    }

    public void setNama_barang(String nama_barang) {
        this.nama_barang = nama_barang;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

//    public String getUid() {
//        return uid;
//    }
//
//    public void setUid(String uid) {
//        this.uid = uid;
//    }

    public String getPicked_qty() {
        return picked_qty;
    }

    public void setPicked_qty(String picked_qty) {
        this.picked_qty = picked_qty;
    }

    public String getSisa_qty() {
        return sisa_qty;
    }

    public void setSisa_qty(String sisa_qty) {
        this.sisa_qty = sisa_qty;
    }
}
