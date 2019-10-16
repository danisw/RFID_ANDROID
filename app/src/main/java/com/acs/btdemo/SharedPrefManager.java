package com.acs.btdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String shared_pref_name="mySmarcard";
    private static final String key_username="keyusername";
    private static final String key_name="keyname";
    private static final String key_id="keyid";
    private static final String key_wh_id="keywh_id";
    private static final String key_wh_name="keywh_name";
    private static final String key_company="keycompany";
    private static final String key_company_id="keycompanyid";

    private static SharedPrefManager mInstance;
    private static Context mContex;

    public SharedPrefManager(Context context) {
        mContex=context;
    }
    public static synchronized SharedPrefManager getInstance(Context context){
        if(mInstance== null){
            mInstance= new SharedPrefManager(context);
        }
        return mInstance;
    }
    public void userLogin(Users user){
        SharedPreferences sharedPreferences = mContex.getSharedPreferences(shared_pref_name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key_id,user.getId());
        editor.putString(key_username,user.getUsername());
        editor.putString(key_name,user.getName());
        editor.putString(key_wh_id,user.getWh_id());
        editor.putString(key_wh_name,user.getWh_name());
        editor.putString(key_company,user.getCompany());
        editor.putString(key_company_id,user.getCompany_id());
        editor.apply();
    }
    public boolean isLoggedIn(){
        SharedPreferences sharedPreferences = mContex.getSharedPreferences(shared_pref_name,Context.MODE_PRIVATE);
        return sharedPreferences.getString(key_username,null) != null;
    }

    //make user logged in
    public Users getUser(){
        SharedPreferences sharedPreferences = mContex.getSharedPreferences(shared_pref_name,Context.MODE_PRIVATE);
        return new Users(
                sharedPreferences.getString(key_id,null),
                sharedPreferences.getString(key_username,null),
                sharedPreferences.getString(key_name,null),
                sharedPreferences.getString(key_wh_id,null),
                sharedPreferences.getString(key_wh_name,null),
                sharedPreferences.getString(key_company,null),
                sharedPreferences.getString(key_company_id,null)
        );
    }

    //make user logout
    public void logout(){
        SharedPreferences sharedPreferences = mContex.getSharedPreferences(shared_pref_name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        final Intent intent = new Intent(mContex,LoginActivity.class);
        mContex.startActivity(intent);

    }
}
