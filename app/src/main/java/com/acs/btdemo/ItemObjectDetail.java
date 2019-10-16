package com.acs.btdemo;

import com.google.gson.annotations.SerializedName;

public class ItemObjectDetail {
    @SerializedName("kode_barang")
//    private String songTitle;
    private String kode_barang;
    @SerializedName("nama_barang")
//    private String songYear;
    private String nama_barang;
    @SerializedName("qty")
//    private String songAuthor;
    private String qty;

    public ItemObjectDetail(String kode_barang, String nama_barang, String qty) {
        this.kode_barang = kode_barang;
        this.nama_barang = nama_barang;
        this.qty = qty;
    }
    public String getKodeBarang() {
        return kode_barang;
    }
    public String getNamaBarang() {
        return nama_barang;
    }
    public String getQtyBarang() {
        return qty;
    }
}
