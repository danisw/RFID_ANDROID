package com.acs.btdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ActionBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserProfile extends Activity implements AdapterView.OnItemSelectedListener {
    TextView nama_profile, username, wh_name, company;
    Button btn_next;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    //An ArrayList for Spinner Items
    private ArrayList<String> whs;
    //JSON Array
    private JSONArray result;
    /* Spinner and Button */
    private Spinner spinner_whs;
    //tampungan  selected whs
    String whsd_selected;
    String whsd_selected_name;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        //nav
//        dl = (DrawerLayout)findViewById(R.id.activity_main);
//        t = new ActionBarDrawerToggle(this, dl,R.string.transmit_apdu, R.string.app_name);
//
//        dl.addDrawerListener(t);
//        t.syncState();

       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        nv = (NavigationView)findViewById(R.id.nv);
//        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                int id = item.getItemId();
//                switch(id)
//                {
//                    case R.id.accountz:
//                        Toast.makeText(UserProfile.this, "My Account",Toast.LENGTH_SHORT).show();break;
//                    case R.id.settingsz:
//                        Toast.makeText(UserProfile.this, "Settings",Toast.LENGTH_SHORT).show();break;
//                    case R.id.mycart:
//                        Toast.makeText(UserProfile.this, "My Cart",Toast.LENGTH_SHORT).show();break;
//                    default:
//                        return true;
//                }
//
//
//                return true;
//
//            }
//        });

        Users user = SharedPrefManager.getInstance(this).getUser();

        nama_profile = findViewById(R.id.up_nama_user);
        username = findViewById(R.id.up_username);
        wh_name = findViewById(R.id.up_wh_name);
        btn_next = findViewById(R.id.up_btn_next);
        company = findViewById(R.id.company);
        spinner_whs = findViewById(R.id.spinner_wh);

        //Initializing the ArrayList
        whs = new ArrayList<String>();

        //Adding an Item Selected Listener to our Spinner
        spinner_whs.setOnItemSelectedListener(this);

        //get data Sinner
        String companyy = user.getCompany_id();
//        Log.d("compannyy",companyy);
        get_data_spinner(companyy);


        nama_profile.setText(user.getName());
        username.setText(user.getUsername());
        wh_name.setText(user.getWh_name());
        company.setText(user.getCompany());

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfile.this, DeviceScanActivity.class);
                intent.putExtra("whs_name",whsd_selected_name);
                intent.putExtra("whs_id",whsd_selected);
                startActivity(intent);
            }
        });

    }

    private void get_data_spinner(String company) {
        //Creating a string request
        StringRequest stringRequest = new StringRequest(SpinnerWhsMilik.DATA_URL+""+company,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("whd", "onResponse: "+response.toString());
                        JSONObject j = null;
                        try {
                            //Parsing the fetched Json String to JSON Object
                            j = new JSONObject(response);

                            //Storing the Array of JSON String to our JSON Array
                            result = j.getJSONArray(SpinnerWhsMilik.JSON_ARRAY);
                            Log.d("whd", "onResponse: "+result);

                            //Calling method getStudents to get the students from the JSON Array
                            getWarehouse(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //Creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
    private void getWarehouse(JSONArray j) {
        //Traversing through all the items in the json array
        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                Log.d("whd", "getKeterangan: "+json.getString(SpinnerWhsMilik.TAG_NAMA_KETERANGAN));

                //Adding the name of the information to array list
                whs.add(json.getString(SpinnerWhsMilik.TAG_NAMA_KETERANGAN));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Setting adapter to show the items in the spinner
        spinner_whs.setAdapter(new ArrayAdapter<String>(UserProfile.this, R.layout.spinner_itemwhs_dest,R.id.WhsSpinner_dest,  whs));

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        whsd_selected = getValue(position);
        whsd_selected_name = getName(position);
    }

    //Method to get value keterangan of a particular position
    private String getValue(int position){
        String value="";
        try {
            //Getating object of given index
            JSONObject json = result.getJSONObject(position);

            //Fetching name from that object
            value = json.getString(SpinnerWhsMilik.TAG_VALUE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the value
        return value;
    }
    //Method to get Name of a particular position
    private String getName(int position){
        String name="";
        try {
            //Getating object of given index
            JSONObject json = result.getJSONObject(position);

            //Fetching name from that object
            name = json.getString(SpinnerWhsMilik.TAG_NAMA_KETERANGAN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name
        return name;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
