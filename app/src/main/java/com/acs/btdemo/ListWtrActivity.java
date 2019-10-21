package com.acs.btdemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.acs.btdemo.ReaderActivity.no_WTR;

public class ListWtrActivity extends AppCompatActivity implements Spinner.OnItemSelectedListener {

    private View heroImageView;
    private CoordinatorLayout mCLayout;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCToolbarLayout;
    private Button btn_konfirm;
    private RecyclerView listView;
    private SearchView searchView;
    private EditText editTextSearch;

    /* Reader to be connected. */
    private String mDeviceName;
    private String mDeviceAddress;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    TextView txt_whdest;
    /* Spinner and Button */
    private Spinner sCategory;
  //  private Spinner sWhDest;
    private Spinner sWhSource;
    private Button mCari;

    //An ArrayList for Spinner Items
    private ArrayList<String> whs;
    //JSON Array
    private JSONArray result;
    //tampungan  selected whs
    String whsc_selected;
    String cat_selected;
    String whdest_selected, whs_id, whs_name;
    String cat;

    /** set up adapter **/
    ArrayList<WtrModel> dataItem = new ArrayList<>();
    Adapter<WtrModel, WtrViewHolder> adapter = new Adapter<WtrModel, WtrViewHolder>
            (R.layout.list_wtr_child, WtrViewHolder.class, dataItem) {
        private List<WtrModel> wtrList;
        private List<WtrModel> wtrListFiltered;

        @Override
        protected void bindView(WtrViewHolder holder, final WtrModel model, int position) {
            holder.onBind(ListWtrActivity.this,model);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String no_WTR2 = model.getTitle();
                    String epoch = model.getEpoch();
                    String id_wtr = model.getId_wtr();
                    String wh_desc_id=whdest_selected;
                    String cat = cat_selected ;
                   // String wh_desc_name=whdest_selected_name;
                   // String wh_sc_id=whsc_selected;
                    //String wh_sc_name=;

                    Log.d("URL_re", "onClick: gget title"+no_WTR);
                    final Intent intent = new Intent(ListWtrActivity.this, ReaderActivity.class);
                    intent.putExtra(ListWtrActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                    intent.putExtra(ListWtrActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress );
                    intent.putExtra("no_WTR", no_WTR2 );
                    intent.putExtra("epoch", epoch );
                    intent.putExtra("id_wtr",id_wtr);
                    intent.putExtra("wh_desc_id",wh_desc_id);
                    intent.putExtra("cat_selected",cat_selected);
                    startActivity(intent);
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

    /** set up Spinner value **/
    String [] sCategory_value ={"RM","SP","WIP","FG"};
    String [] sWhDest_value={"Gudang Packing"};
    String [] items_value_dest = new String[]{ "30"};
//    String [] sWhSource_value={"Gudang Biskuit","Gudang Produksi Biskuit","Gudang Wafer","Gudang Produksi Wafer",
//            "Gudang Formulasi", "Gudang Produksi Formulasi","Gudang GA","Gudang Retur"};
    //String [] items_value_source = new String[]{ "42", "25","43","40","37","45","28","33"};


    /** start activity **/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_wtr_main);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        if(b!=null) {
            String j = (String) b.get("whs_name");
            String i = (String) b.get("whs_id");

            whs_id=i;
            whs_name=j;

        }else{
            finish();
        }
        /*intitialize search edit text*/
        editTextSearch=findViewById(R.id.editTextSearch);

        /* Initialise list view, hero image, and sticky view */
        listView = (RecyclerView) findViewById(R.id.rv_wtr);
        heroImageView = findViewById(R.id.heroImageView_wtr);

        // Get the widget reference from XML layout
        mCLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout_wtr);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_wtr);
        mCToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout_wtr);

        // Set a title for collapsing toolbar layout
        mCToolbarLayout.setTitleEnabled(true);
        mCToolbarLayout.setTitle("List WTR");

        // Define the collapsing toolbar title text color
        mCToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        mCToolbarLayout.setExpandedTitleColor(Color.WHITE);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //initialize spinner adapter
        sCategory= findViewById(R.id.spinner_cat);
        txt_whdest=findViewById(R.id.txt_wh_dest);
        txt_whdest.setText(whs_name);
       // sWhDest=findViewById(R.id.spinner_wh_dest);
        sWhSource=findViewById(R.id.spinner_wh_asal);

        //Initializing the ArrayList
        whs = new ArrayList<String>();

        //Adding an Item Selected Listener to our Spinner
        sWhSource.setOnItemSelectedListener(this);

        //get data Sinner
        get_data_spinner();

        ArrayAdapter<String> adapter_cat = new ArrayAdapter<String>(ListWtrActivity.this, R.layout.list_itemcategory, R.id.IcatSpinnerText, sCategory_value);
       // ArrayAdapter<String> adapter_whd = new ArrayAdapter<String>(ListWtrActivity.this,  R.layout.list_itemwhdest,R.id.WhdestSpinnerText ,sWhDest_value);
       // ArrayAdapter<String> adapter_whsc = new ArrayAdapter<String>(ListWtrActivity.this, R.layout.list_itemwhsource,R.id.WhscSpinnerText, sWhSource_value);

        sCategory.setAdapter(adapter_cat);
     //   sWhDest.setAdapter(adapter_whd);
       // sWhSource.setAdapter(adapter_whsc);

        //give default value to spinner
        sCategory.setSelection(0);
        //sWhDest.setSelection(0);
        //sWhSource.setSelection(0);

        // initialize button cari
        mCari=findViewById(R.id.btn_cari);

        //on click button cari, retrieve data rv
        mCari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cat_selected = sCategory.getSelectedItem().toString();
              //  whdest_selected = items_value_dest[ sWhDest.getSelectedItemPosition() ];
                whdest_selected=whs_id;
               // String whsc_selected = items_value_source[ sWhSource.getSelectedItemPosition() ];
                Toast.makeText(ListWtrActivity.this,whsc_selected+" | "+whsc_selected,Toast.LENGTH_SHORT).show();
                /** Call data **/
                requestJsonObjectA(new ReaderActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(JSONArray result) {
                        Log.d("Spinner",result.toString());

                        dataItem.clear();
                        for(int i = 0; i < result.length(); i++) {
                            try {
                                JSONObject jsonObject = result.getJSONObject(i);
                                String judul= jsonObject.getString("no_wtr");
                                String tanggal=jsonObject.getString("waktu");
                                String epoch = jsonObject.getString("epoch");
                                String id_wtr = jsonObject.getString("id_wtr");
                                String presentase = jsonObject.getString("presentase");
                                String request_qty = jsonObject.getString("request_qty");
                                String Approved_qty = jsonObject.getString("Approved_qty");
                                Log.d("wtr_no", judul);
                                dataItem.add(new WtrModel(judul,tanggal,epoch,id_wtr,presentase,request_qty,Approved_qty));
                            }
                            catch(JSONException e) {
                                dataItem.add(new WtrModel("Error: " + e.getLocalizedMessage(),"error_tgl",
                                        "error epoch","error id wtr","error presentase","error req","error appro"));
                            }
                        }
                        listView = (RecyclerView) findViewById(R.id.rv_wtr);

                        /** set up layout View **/
                        setUpView();
                    }
                },cat_selected,whdest_selected,whsc_selected);

            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //after the change calling the method and passing the search input
                filter(editable.toString());
            }

            private void filter(String text) {
                //new array list that will hold the filtered data
                ArrayList<WtrModel> filterdNames = new ArrayList<>();

                //looping through existing elements
                for (WtrModel s : dataItem ) {

                    //if the existing elements contains the search input
                    if (s.getTitle().toLowerCase().contains(text.toLowerCase())) {
                        //adding the element to filtered list
                        filterdNames.add(s);
                    }
                }

                //calling a method of the adapter class and passing the filtered list
                adapter.filterList(filterdNames);
            }
        });

        setUpView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_search, menu);
//        // Associate searchable configuration with the SearchView
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setMaxWidth(Integer.MAX_VALUE);
//
//        // listening to search query text change
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // filter recycler view when query submitted
//                adapter.getFilter().filter(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String query) {
//                // filter recycler view when text is changed
//                adapter.getFilter().filter(query);
//                return false;
//            }
//        });
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        cat_selected = sCategory.getSelectedItem().toString();
       // whdest_selected = items_value_dest[ sWhDest.getSelectedItemPosition() ];
        whdest_selected = whs_id;
        // String whsc_selected = items_value_source[ sWhSource.getSelectedItemPosition() ];
        Toast.makeText(ListWtrActivity.this,whsc_selected+" | "+whsc_selected,Toast.LENGTH_SHORT).show();
        /** Call data **/
        requestJsonObjectA(new ReaderActivity.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                Log.d("Spinner",result.toString());

                dataItem.clear();
                for(int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject jsonObject = result.getJSONObject(i);
                        String judul= jsonObject.getString("no_wtr");
                        String tanggal=jsonObject.getString("waktu");
                        String epoch = jsonObject.getString("epoch");
                        String id_wtr = jsonObject.getString("id_wtr");
                        String presentase = jsonObject.getString("presentase");
                        String request_qty = jsonObject.getString("request_qty");
                        String Approved_qty = jsonObject.getString("Approved_qty");
                        Log.d("wtr_no", judul);
                        dataItem.add(new WtrModel(judul,tanggal,epoch,id_wtr,presentase,request_qty,Approved_qty));
                    }
                    catch(JSONException e) {
                        dataItem.add(new WtrModel("Error: " + e.getLocalizedMessage(),"error_tgl",
                                "error epoch","error id wtr","error presentase","error req","error appro"));
                    }
                }
                listView = (RecyclerView) findViewById(R.id.rv_wtr);

                /** set up layout View **/
                setUpView();
            }
        },cat_selected,whdest_selected,whsc_selected);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cat_selected = sCategory.getSelectedItem().toString();
        //whdest_selected = items_value_dest[ sWhDest.getSelectedItemPosition() ];
        whdest_selected=whs_id;
        // String whsc_selected = items_value_source[ sWhSource.getSelectedItemPosition() ];
        Toast.makeText(ListWtrActivity.this,whsc_selected+" | "+whsc_selected,Toast.LENGTH_SHORT).show();
        /** Call data **/
        requestJsonObjectA(new ReaderActivity.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                Log.d("Spinner",result.toString());

                dataItem.clear();
                for(int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject jsonObject = result.getJSONObject(i);
                        String judul= jsonObject.getString("no_wtr");
                        String tanggal=jsonObject.getString("waktu");
                        String epoch = jsonObject.getString("epoch");
                        String id_wtr = jsonObject.getString("id_wtr");
                        String presentase = jsonObject.getString("presentase");
                        String request_qty = jsonObject.getString("request_qty");
                        String Approved_qty = jsonObject.getString("Approved_qty");
                        Log.d("wtr_no", judul);
                        dataItem.add(new WtrModel(judul,tanggal,epoch,id_wtr,presentase,request_qty,Approved_qty));
                    }
                    catch(JSONException e) {
                        dataItem.add(new WtrModel("Error: " + e.getLocalizedMessage(),"error_tgl",
                                "error epoch","error id wtr","error presentase","error req","error appro"));
                    }
                }
                listView = (RecyclerView) findViewById(R.id.rv_wtr);

                /** set up layout View **/
                setUpView();
            }
        },cat_selected,whdest_selected,whsc_selected);

    }

    private void get_data_spinner() {
        //Creating a string request
        StringRequest stringRequest = new StringRequest(SpinnerWhs.DATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("retur", "onResponse: "+response.toString());
                        JSONObject j = null;
                        try {
                            //Parsing the fetched Json String to JSON Object
                            j = new JSONObject(response);

                            //Storing the Array of JSON String to our JSON Array
                            result = j.getJSONArray(SpinnerWhs.JSON_ARRAY);
                            Log.d("retur", "onResponse: "+result);

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
                Log.d("retur", "getKeterangan: "+json.getString(SpinnerWhs.TAG_NAMA_KETERANGAN));

                //Adding the name of the information to array list
                whs.add(json.getString(SpinnerWhs.TAG_NAMA_KETERANGAN));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Setting adapter to show the items in the spinner
        sWhSource.setAdapter(new ArrayAdapter<String>(ListWtrActivity.this, R.layout.list_itemwhsource,R.id.WhscSpinnerText,  whs));

    }
    //Method to get value keterangan of a particular position
    private String getValue(int position){
        String value="";
        try {
            //Getating object of given index
            JSONObject json = result.getJSONObject(position);

            //Fetching name from that object
            value = json.getString(SpinnerWhs.TAG_VALUE);
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
            name = json.getString(SpinnerWhs.TAG_NAMA_KETERANGAN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name
        return name;
    }


    /** Calling Data Function **/
    private void requestJsonObjectA(final ReaderActivity.VolleyCallback callback, final String cat_selected, final String whdest_selected, final String whsc_selected ) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://10.1.250.116/rest-api/index.php/api/WTR_header/"+cat_selected+"/"+whdest_selected+"/"+whsc_selected;
        //String url ="http://192.168.0.4/rest-api/index.php/api/WTR_header/";

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
        listView.setLayoutManager(new LinearLayoutManager(ListWtrActivity.this, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setCallback(adapterListeners);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        whsc_selected=getValue(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        whsc_selected="pilih";
    }
}
