package com.acs.btdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ItemWhtList extends AppCompatActivity {
    private TextView stickyView;
    private RecyclerView listView;
    private View heroImageView;
    private CoordinatorLayout mCLayout;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCToolbarLayout;
    private Button btn_konfirm;
    private String kode_wtr;
    private String kode_uid;
    private  String id_wtr;
    private String cat_selected;
    private CoordinatorLayout coordinatorLayout;
    String epoch,sc_wh,dest_wh;

    /** set up adapter **/
    ArrayList<ItemWhtModel> dataItem = new ArrayList<>();
    Adapter<ItemWhtModel, ItemViewHolder> adapter = new Adapter<ItemWhtModel, ItemViewHolder>
            (R.layout.list_wtr, ItemViewHolder.class, dataItem) {

        @Override
        protected void bindView(ItemViewHolder holder, final ItemWhtModel model, final int position) {
            holder.onBind(ItemWhtList.this, model);

            coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coor_layout);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar snackbar = (Snackbar) Snackbar.make(view,"This is Simple Snackbar", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    //Toast.makeText(ItemWhtList.this, "Click on " + model.getTitle()+" position :"+position, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };


    Adapter.Callback adapterListeners = new Adapter.Callback() {

        @Override
        public void onImageClick(int position) {
            //code here setOnClickListener()
        }

        @Override
        public void onRemoveItem(int position) {
            //code here setOnClickListener()
        }
    };


    /** start activity **/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item_wht);

        //get Epoch time untuk inisialisasi insert data ke database
        //long currentTime = Calendar.getInstance().getTimeInMillis();
        //epoch = Long.toString(currentTime);

        //Kode WTR
        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        if(b!=null)
        {
            String j =(String) b.get("no_wtr");
            String i =(String) b.get("no_uid");
            String k = (String) b.get("id_wtr");
            String l = (String) b.get("sc_wh");
            String m = (String) b.get("cat_selected");
            String n = (String) b.get("no_uid");
            String o = (String) b.get("epoch");
            String p = (String) b.get("wh_desc_id");

            kode_wtr=j;
            id_wtr=k;
            kode_uid=i.replace(" ", "").substring(0,8);
            cat_selected=m;
            sc_wh=l;
            dest_wh=p;
            epoch=o;
        }else{
            finish();
        }

        /* Initialise list view, hero image, and sticky view */
        listView = (RecyclerView) findViewById(R.id.listView);
        heroImageView = findViewById(R.id.heroImageView);
       // stickyView = (TextView) findViewById(R.id.stickyView);

        // Get the widget reference from XML layout
        mCLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);

        // Set a title for collapsing toolbar layout
        mCToolbarLayout.setTitleEnabled(true);
        mCToolbarLayout.setTitle("Barang Terpilih");

        // Define the collapsing toolbar title text color
        mCToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        mCToolbarLayout.setExpandedTitleColor(Color.WHITE);

        //Button konfirm
        btn_konfirm= findViewById(R.id.btn_konfirm);
        btn_konfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemWhtList.this, DataHeaderWHT.class);
                intent.putExtra("id_wtr",id_wtr);
                intent.putExtra("kode_WTR",kode_wtr);
                intent.putExtra("dest_wh",dest_wh);
                intent.putExtra("sc_wh",sc_wh);
                intent.putExtra("epoch",epoch);
                intent.putExtra("cat_selected",cat_selected);
                startActivity(intent);

            }
        });

        /** Call data **/
        requestJsonObjectA(new ReaderActivity.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                for(int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject jsonObject = result.getJSONObject(i);
                        String judul= jsonObject.getString("kode_barang");
                        String uid= jsonObject.getString("uid_picked");
                        String qty= jsonObject.getString("qty");
                        Log.d("kode_item_rcv", judul);
                        dataItem.add(new ItemWhtModel(judul,uid,qty));
                    }
                    catch(JSONException e) {
                        dataItem.add(new ItemWhtModel("Error: " + e.getLocalizedMessage(),"error uid","error qty"));
                    }
                }
                listView = (RecyclerView) findViewById(R.id.listView);

                /** set up layout View **/
                setUpView();
            }
        }, kode_wtr,epoch);

        setUpView();
    }

    private void alert(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(ItemWhtList.this)
                //set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
                //set title
                .setTitle("Berhasil")
                //set message
                .setMessage(message)
                //set positive button
                .setPositiveButton("Kembali", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                           finish();
                    }
                })
                //set negative button
//                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            //set what should happen when negative button is clicked
//                            btn_scan_id.setEnabled(true);
//                            btn_scan_id.setVisibility(View.VISIBLE);
//                            btn_pick.setEnabled(false);
//                        }
//                    })
                .show();
    }

    /** Calling Data Function **/
    private void requestJsonObjectA(final ReaderActivity.VolleyCallback callback, String No_WTR, String epoch) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://10.1.250.116/rest-api/index.php/api/Item_picked/"+No_WTR+"/"+epoch;
        //String url ="http://192.168.0.4/rest-api/index.php/api/Item_picked/"+No_WTR;

        Log.d("ItemPickedwtr", url);
        JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray jsonArray) {
                callback.onSuccess(jsonArray);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Eroor", "Error " + error.getMessage());
            }
        });
        queue.add(request);
    }

    private void setUpView() {
        listView.setLayoutManager(new LinearLayoutManager(ItemWhtList.this, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setCallback(adapterListeners);
    }


    private void setOnClickListener() {
    }

}
