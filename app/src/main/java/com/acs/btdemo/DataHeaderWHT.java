package com.acs.btdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataHeaderWHT extends AppCompatActivity implements Spinner.OnItemSelectedListener{
    private String id_wtr;
    private String kode_WTR;
    private String dest_wh;
    private String sc_wh;

    TextView id_wtr_txt;
    TextView no_wtr_txt;
    TextView sc_wh_txt;
    TextView dest_wh_txt;

    Spinner sp_ket_retur;

   private String selected_name;
    private String selected_value;

    Button btn_konfirm;

    String whs;
    String nik;
    String epoch;
    String cat;
    String USER_NFC;
    String USER_WTR;
    String wtr_id;
    String PB;
    String SUPIR;
    String KON;
    String NOPOL;
    String NOSEAL;
    String MEMO;


    EditText kontainer, driver, plat, noseal, memo;

    //An ArrayList for Spinner Items
    private ArrayList<String> keterangan;

    //JSON Array
    private JSONArray result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wht_header);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        if(b!=null)
        {
            String j =(String) b.get("id_wtr");
            String i =(String) b.get("kode_WTR");
            String k = (String) b.get("dest_wh");
            String l = (String) b.get("sc_wh");
            String m = (String) b.get("cat_selected");
            String n = (String) b.get("no_uid");
            String o = (String) b.get("epoch");

            id_wtr=j;
            kode_WTR=i;
            dest_wh=k;
            sc_wh=l;
            cat=m;
            epoch=o;

        }else{
            finish();
        }

        id_wtr_txt=findViewById(R.id.id_WTR);
        no_wtr_txt=findViewById(R.id.no_WTR);
        sc_wh_txt = findViewById(R.id.sc_whs);
        dest_wh_txt = findViewById(R.id.dest_wh);
        sp_ket_retur= findViewById(R.id.ket_retur);
        btn_konfirm=findViewById(R.id.btn_konfirm);
        kontainer=findViewById(R.id.kontainer);
        driver = findViewById(R.id.driver);
        plat= findViewById(R.id.nopol);
        noseal=findViewById(R.id.noseal);
        memo=findViewById(R.id.memo);


        id_wtr_txt.setText(id_wtr);
        no_wtr_txt.setText(kode_WTR);
        dest_wh_txt.setText(dest_wh);
        sc_wh_txt.setText(sc_wh);

        //Initializing the ArrayList
        keterangan = new ArrayList<String>();
        //Adding an Item Selected Listener to our Spinner
        //As we have implemented the class Spinner.OnItemSelectedListener to this class iteself we are passing this to setOnItemSelectedListener
        sp_ket_retur.setOnItemSelectedListener(this);

        //get data Sinner
        get_data_spinner();

        //get user session
        Users user = SharedPrefManager.getInstance(this).getUser();
        final String nik2=user.getUsername();

        btn_konfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String whs=dest_wh;
                String nik=nik2;
                String epoch2=epoch;
                String cat2=cat;
                String USER_NFC=nik;
                String wtr_id=id_wtr_txt.getText().toString();
                String PB=selected_value;
                String SUPIR = driver.getText().toString();
                String KON=kontainer.getText().toString();
                String NOPOL=plat.getText().toString();
                String NOSEAL=noseal.getText().toString();
                String MEMO = memo.getText().toString();
                Log.d("is_berisi", "onClick: "+whs+" "+nik+" "+epoch2+" "+cat2+" "+USER_NFC+" "+wtr_id+" "+PB+" "+SUPIR+" "+KON+" "+NOPOL+" "+NOSEAL+" "+MEMO);

                postJsonObject(whs,nik,epoch2,cat2,USER_NFC,wtr_id,PB,SUPIR,KON,NOPOL,NOSEAL,MEMO);



            }
        });

    }

    private void postJsonObject(final String whs, final String nik, final String epoch, final String cat,
                                final String USER_NFC, final String wtr_id, final String PB,final String SUPIR,final String KON,final String NOPOL,
                                final String NOSEAL, final String MEMO ) {
        RequestQueue queue2 = Volley.newRequestQueue(getApplicationContext());
        final String url2 ="http://10.1.250.116/rest-api/index.php/api/Final_input/index_post/";
        // final String url2 ="http://192.168.0.4/rest-api/index.php/api/Item_2/index_post/";

        StringRequest stringRequest2 = new StringRequest(Request.Method.POST, url2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(context, "Input Item Successed !", Toast.LENGTH_SHORT).show();
                Log.d("my_suc", "onResponse: "+url2);
                alert2("berhasil", response.toString());
                btn_konfirm.setEnabled(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                alert2("Gagal",error.getMessage()+" | "+whs+" "+nik+" "+epoch+" "+cat+" "+USER_NFC+" "+wtr_id+ " "+ PB+" "+SUPIR+" "+KON+" "+NOPOL+" "+NOSEAL+" "+MEMO);
                Log.d("response_put", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> map = new HashMap<String, String>();
                map.put("whs",whs);
                map.put("nik",nik);
                map.put("epoch",epoch);
                map.put("cat",cat);
                map.put("USER_NFC",USER_NFC);
                map.put("wtr_id",wtr_id);
                map.put("PB", PB);
                map.put("SUPIR", SUPIR);
                map.put("KON", KON);
                map.put("NOPOL", NOPOL);
                map.put("NOSEAL", NOSEAL);
                map.put("MEMO", MEMO);

                return map;
            }
        };

        queue2.add(stringRequest2);
    }
    public void alert2(String Judul, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(DataHeaderWHT.this)
                //set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
                //set title
                .setTitle(Judul)
                //set message
                .setMessage(message)
                .show();
    }
    private void get_data_spinner() {
        //Creating a string request
        StringRequest stringRequest = new StringRequest(SpinnerRetur.DATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("retur", "onResponse: "+response.toString());
                        JSONObject j = null;
                        try {
                            //Parsing the fetched Json String to JSON Object
                            j = new JSONObject(response);

                            //Storing the Array of JSON String to our JSON Array
                            result = j.getJSONArray(SpinnerRetur.JSON_ARRAY);
                            Log.d("retur", "onResponse: "+result);

                            //Calling method getStudents to get the students from the JSON Array
                            getKeterangan(result);
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

    private void getKeterangan(JSONArray j){
        //Traversing through all the items in the json array
        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                Log.d("retur", "getKeterangan: "+json.getString(SpinnerRetur.TAG_NAMA_KETERANGAN));

                //Adding the name of the information to array list
                keterangan.add(json.getString(SpinnerRetur.TAG_NAMA_KETERANGAN));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Setting adapter to show the items in the spinner
        sp_ket_retur.setAdapter(new ArrayAdapter<String>(DataHeaderWHT.this, R.layout.item_retur, R.id.returSpinnerText,  keterangan));
    }

    //Method to get value keterangan of a particular position
    private String getValue(int position){
        String value="";
        try {
            //Getating object of given index
            JSONObject json = result.getJSONObject(position);

            //Fetching name from that object
            value = json.getString(SpinnerRetur.TAG_VALUE);
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
            name = json.getString(SpinnerRetur.TAG_NAMA_KETERANGAN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name
        return name;
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        selected_name=getName(position).toString();
        selected_value=getValue(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        selected_name="";
        selected_value="";

    }
}
