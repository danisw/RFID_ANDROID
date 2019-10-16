package com.acs.btdemo;

public class Users {
    private String id;
    private String username, name, wh_id, wh_name,company,company_id;



    public Users(String id, String username, String name, String wh_id, String wh_name, String company, String company_id) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.wh_id=wh_id;
        this.wh_name=wh_name;
        this.company=company;
        this.company_id=company_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getWh_id() {
        return wh_id;
    }

    public void setWh_id(String wh_id) {
        this.wh_id = wh_id;
    }

    public String getWh_name() {
        return wh_name;
    }

    public void setWh_name(String wh_name) {
        this.wh_name = wh_name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }
}
