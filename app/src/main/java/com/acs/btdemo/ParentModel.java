package com.acs.btdemo;
//import com.google.gson.annotations.SerializedName;

class ParentModel {
   // @SerializedName("no_WTR")
    private String title;

    public ParentModel(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
