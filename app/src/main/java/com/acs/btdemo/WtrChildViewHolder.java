package com.acs.btdemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WtrChildViewHolder extends RecyclerView.ViewHolder {
    private TextView txt_no_WTR;
    private TextView txt_kode_barang;
    private TextView txt_nama_barang;
    private TextView txt_qty_;
    private TextView txt_history;
    private TextView txt_sisa_qty;
    private TextView qty_temp;
    private TextView mTxtScan;
    private TextView judul;
    private RelativeLayout RL;
    private static String uid_card_nospace2;
    private static String uid_db_nospace2;
    public static String isIdentik;
    public static String picked_uid;
    String qty_card;
    String qty_ready;
    public String no_lot;
    private CardView cv;
    // A reference to an adapter's callback.
    protected Adapter.Callback callback;
    public int position;


    private Button btn_scan_id;
    private Button btn_pick;
    private Integer flag_sisa;

    private String epoch,exp_date;
    // Flag 0 baru, 1 masih ada, 2 habis

    public WtrChildViewHolder(View itemView) {
        super(itemView);


        String epoch2=ReaderActivity.epoch;
        if(epoch2.equals("0")){
            //get Epoch time untuk inisialisasi insert data ke database
            long currentTime = Calendar.getInstance().getTimeInMillis();
            epoch = Long.toString(currentTime);
        }else{
            epoch = ReaderActivity.epoch;
        }


        isIdentik="nan";
        txt_no_WTR=itemView.findViewById(R.id.no_WTR_barang);
        txt_kode_barang = itemView.findViewById(R.id.kode_barang);
        txt_nama_barang = itemView.findViewById(R.id.nama_barang);
        txt_qty_ = itemView.findViewById(R.id.qty_barang);
        txt_sisa_qty=itemView.findViewById(R.id.sisa_pick);
        qty_temp = itemView.findViewById(R.id.qty_temp);
        txt_history = itemView.findViewById(R.id.history_pick);
        mTxtScan = itemView.findViewById(R.id.uid);
        btn_scan_id =itemView.findViewById(R.id.btnScanSS);
        RL=itemView.findViewById(R.id.detail_layout);
        btn_pick = itemView.findViewById(R.id.btnPick);
        btn_pick.setEnabled(false);
        cv=itemView.findViewById(R.id.myCard);
        
    }
    public void onBind(final Context context, final ChildItem model) {

        txt_no_WTR.setText(model.getNo_WTR());
        txt_kode_barang.setText(model.getKode_barang());
        txt_nama_barang.setText(model.getNama_barang());
        txt_qty_.setText(model.getQty());
        String qty_histori = model.getPicked_qty();
        String qty_sisa_histori = model.getSisa_qty();
        if(qty_histori=="null"){
            qty_histori="0";
            txt_history.setText(qty_histori);
        }else{
            txt_history.setText(qty_histori);
        }
        if(qty_sisa_histori=="null"){
            qty_sisa_histori="0";
            txt_sisa_qty.setText(qty_sisa_histori);
        }else{
            txt_sisa_qty.setText(qty_sisa_histori);
        }

        //mTxtScan.setText(model.getUid());
        uid_card_nospace2="tes";
        uid_db_nospace2="tes";



        btn_scan_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context, "Click on Button Scan " , Toast.LENGTH_SHORT).show();
                /*** Retrieve data from Reader ACTIVITY uid nya ajaaaaaa gak usah serevice ,
                 *  semua proses baca disana hasilya doang yg dibawa sini***/
                final ReaderActivity activity = new ReaderActivity();
                String uid_card =((ReaderActivity) activity).requestUid_2();
                //String uid_db= (String) model.getUid();
                final String uid_card_nospace =((ReaderActivity) activity).requestUid_2().replace(" ", "");
                //String uid_db_nospace= (String) model.getUid().replace(" ", "");
                final String kode_picked=txt_kode_barang.getText().toString();
                final String qty_picked = txt_qty_.getText().toString();

                if(uid_card_nospace=="tes"){
                    mTxtScan.setText("tekan lagi");
                }else{

                    ifIdentik(new ReaderActivity.VolleyCallback() {
                        @Override
                        public void onSuccess(JSONArray result) {
                            //ada rcv dengan uid itu dan kode item
                            for(int i = 0; i < result.length(); i++) {
                                try {
                                    JSONObject jsonObject1 = result.getJSONObject(i);

                                    //toast identik
                                    Toast.makeText(context,
                                            "UID sama",
                                            Toast.LENGTH_SHORT).show();
                                    //cek qty
                                    String qty_rcv = jsonObject1.getString("nic_qty");
                                   no_lot =jsonObject1.getString("nic_lot");
                                    if(qty_rcv.equals("0")){
                                        alert3("Qty Kartu abis","abis bro");
                                    }
                                    else{
                                        // alert3("Qty Kartu Masih ada ","lanjut bro");
                                        //cek Expired
                                        cekExpired(new ReaderActivity.VolleyCallback() {
                                            @Override
                                            public void onSuccess(JSONArray result) {
                                                for(int i = 0; i < result.length(); i++) {
                                                    try {
                                                        JSONObject jsonObject = result.getJSONObject(i);
                                                        Log.d("obj_cek",jsonObject.toString());

                                                        if(jsonObject != null){
                                                            exp_date = jsonObject.getString("nic_expired");
                                                           // String no_rcv = jsonObject.getString("no_rcv");
                                                            String uid_rcv = jsonObject.getString("nic_uid_nfc");
                                                            qty_card=jsonObject.getString("qty_card");
                                                            qty_ready=jsonObject.getString("qty_ready");
                                                            String location_exp = jsonObject.getString("location_exp");
                                                            String expired_card = jsonObject.getString("expired_card");

                                                            boolean isIdentic = cekUid(uid_rcv);
                                                            Log.d("cek_uid_exp", "is identik"+isIdentic);

                                                            if (isIdentic) {
                                                                //String message = "Kartu sudah paling dekat. EXP : " + exp_date + ". Dengan Nomor LPB :" + no_rcv;
                                                                //alert(message);
                                                                isIdentik = "identik";
                                                                //set what would happen when positive button is clicked
                                                                RL.setBackgroundColor(Color.parseColor("#B2DFDB"));
                                                                btn_scan_id.setVisibility(View.INVISIBLE);
                                                                btn_scan_id.setEnabled(false);
                                                                btn_pick.setEnabled(true);
                                                                btn_pick.setVisibility(View.VISIBLE);

                                                            } else {
                                                                String message = kode_picked+" \n " +expired_card+"\n Expired mendekati \n"+exp_date+", lokasi : "+location_exp+"\n. Yakin memilih data Ini ?";
                                                                alert(message);
                                                                isIdentik = "nan";
                                                            }

                                                        }else{
                                                            //insert item when no latest exp date
                                                            btn_scan_id.setEnabled(false);
                                                            btn_pick.setEnabled(true);
                                                            btn_pick.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                    catch(JSONException e) {
                                                        // dataParent.add(new ParentModel("Error: " + e.getLocalizedMessage()));
                                                        // dataChild.add(new ChildItem("error","Error: " + e.getLocalizedMessage(),"error","error","error"));
                                                    }
                                                }
                                            }

                                            private boolean cekUid(String uid_rcv) {
                                                String uid_card_nospace =((ReaderActivity) activity).requestUid_2().replace(" ", "").substring(0,8);
                                                String uid_rcv_nospace = uid_rcv.replace(" ","");

                                                Log.d("cek_uid", "cekUid: " + uid_rcv_nospace +" | "+uid_card_nospace);
                                                //boolean ifIdentik;
                                                boolean ifsyama=uid_card_nospace.equals(uid_rcv_nospace);
                                                Log.d("cek_uid_ifsyama", "cekUid: ifdysms "+ifsyama);
                                                if(ifsyama){
                                                    return true;
                                                }else{
                                                    return false;
                                                }
                                            }
                                        },kode_picked,uid_card_nospace.substring(0,8) );
                                    }
                                }
                                catch(JSONException e) {
                                    //no action
                                    isIdentik="nan";
                                    String message = "Array not found : "+e;
                                    alert(message);
                                }
                            }
                        }

                    },kode_picked, uid_card_nospace);

                }
            }
            public void alert (String message){
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        //set icon
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        //set title
                        .setTitle("FIFO Alert !")
                        //set message
                        .setMessage(message)
                        //set positive button
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                RL.setBackgroundColor(Color.parseColor("#01ff90"));
                                btn_scan_id.setVisibility(View.INVISIBLE);
                                btn_scan_id.setEnabled(false);
                                btn_pick.setEnabled(true);
                                btn_pick.setVisibility(View.VISIBLE);

                            }
                        })
                        //set negative button
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what should happen when negative button is clicked
                                btn_scan_id.setEnabled(true);
                                btn_scan_id.setVisibility(View.VISIBLE);
                                btn_pick.setEnabled(false);
                            }
                        })
                        .show();
            }

            private void cekExpired(final ReaderActivity.VolleyCallback callback,String kode_picked,String uid_card_nospace) {
                RequestQueue queue = Volley.newRequestQueue(context);
                String url ="http://10.1.250.116/rest-api/index.php/api/Cek/index_get/"+kode_picked+"/"+uid_card_nospace;
                //String url ="http://192.168.0.4/rest-api/index.php/api/Cek/index_get/"+kode_picked+"/"+no_lot;
                Log.d("URL_C", "cekExpired: "+url);
                //String url ="http://192.168.0.5/rest-api/index.php/api/Cek/index_get/"+kode_picked+"/"+qty_picked;
                JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        if(jsonArray.toString().equals("[]")){
                            alert3("Kartu tidak tersedia","Silahkan cari Karu yang tersedia");
                        }else{
                            callback.onSuccess(jsonArray);
                            Log.d("cek_res", "onResponse: "+jsonArray);
                        }

                       // callback.onSuccess(jsonArray);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", "Error " + error.getMessage());
                    }
                });
                queue.add(request);

            }
            private void ifIdentik(final ReaderActivity.VolleyCallback callback,String kode_picked, String uid_card) {
                if(uid_card.equals("6300")){
                    alert3("Error Card"," Kartu Tidak ditemukan");

                }else{
                String kode_barang=uid_card.substring(0,8);
                RequestQueue queue = Volley.newRequestQueue(context);
                String url ="http://10.1.250.116/rest-api/index.php/api/UID_CEK/index_get/"+kode_picked+"/"+kode_barang;
              //  String url ="http://192.168.0.4/rest-api/index.php/api/UID_CEK/index_get/"+kode_picked+"/"+kode_barang;
                Log.d("URL", "url :"+url);
                JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray jsonArray) {

                        Log.d("identik_array",jsonArray.toString());
                        if(jsonArray.toString().equals("[]")){
                            String message = "UID berbeda, cari nomor WTR lain";
                            alert3("UID Salah","UID salah, Cari Item di WTR lain");
                        }else{
                            callback.onSuccess(jsonArray);
                        }

                    }


                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", "Error " + error.getMessage());

                    }
                });
                queue.add(request);

            }
        }
            private void alert3(String Judul, String Pesan) {
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        //set icon
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        //set title
                        .setTitle(Judul)
                        //set message
                        .setMessage(Pesan)
                        .show();
            }

        });

        btn_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ReaderActivity activity = new ReaderActivity();
                final String uid_card_nospace_p =((ReaderActivity) activity).requestUid_2().replace(" ", "").substring(0,8);
                // if(isIdentik=="identik"){
                // Toast.makeText(context, "This Item Already Picked ", Toast.LENGTH_SHORT).show();
                final String wtr_picked=txt_no_WTR.getText().toString();
                final String kode_picked=txt_kode_barang.getText().toString();
                final String nama_picked = txt_nama_barang.getText().toString();

                // final String qty_picked = txt_qty_.getText().toString();
                //Input qty
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.qty_prompt, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);
                final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        // get user input and set it to result
                                        // edit text
                                        //set current date

                                        String user_Input = userInput.getText().toString();
                                        String qty_awal = txt_qty_.getText().toString();
                                        String history_qty_ = txt_history.getText().toString();
                                        Log.d("qty_over", "userinput : "+Double.valueOf(user_Input)+" | qty_awal : "+Double.valueOf(qty_awal)+" | Histori : "+Double.valueOf(history_qty_));

                                        Double qty_card_= Double.valueOf(qty_card);
                                        Double Condition1=(Double.valueOf(user_Input) + Double.valueOf(history_qty_));
                                        //Condition 1 jika yang diinput lebih besar dari sisa pick
                                        //condition 2 jika qty yang di input > dari sisa qty kartu
                                        //condition 3 else if jika qty yg di pick masih ada yg menggantung
                                        if ( (Condition1 > Double.valueOf(qty_awal)) || (Double.valueOf(user_Input) > qty_card_)) {
                                            alert2("Qty Over","Qty Over");
                                            //isIdentik="nan";
                                        }else if((Double.valueOf(user_Input) > Double.valueOf(qty_ready)) ){
                                            alert2("Qty Over","Ada Qty yang Menggantung. Ready Stock :"+qty_ready);
                                        } else {
                                            Date c = Calendar.getInstance().getTime();
                                            System.out.println("Current time => " + c);

                                            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                                            String formattedDate = df.format(c);
                                            String history_awal = txt_history.getText().toString();
                                            qty_temp.setText(userInput.getText());
                                            Double hAwal = Double.valueOf(history_awal);
                                            Double qtyInput = Double.valueOf(userInput.getText().toString());
                                            Double totalHis = hAwal+qtyInput;
                                            txt_history.setText(""+totalHis.toString());
                                            Log.d("uid_card","uid picked :"+uid_card_nospace_p);

                                            //hide cardview when qtyawal = history picked
                                            Double qty_awal_dob= Double.valueOf(qty_awal);
                                            if(qty_awal_dob.equals(totalHis)){
                                                Log.d("Int_compare", "True ");
                                                cv.setVisibility(View.GONE);
                                            }

                                            String qty_picked2 = qty_temp.getText().toString();
                                            postJsonObject(wtr_picked, kode_picked, nama_picked, qty_picked2, formattedDate,uid_card_nospace_p,epoch, no_lot,exp_date);
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();


//                    postJsonObject(wtr_picked,kode_picked,nama_picked,qty_picked);

//                }else{
//                    Toast.makeText(context, "UID different, This Item Can't be Picked ", Toast.LENGTH_SHORT).show();
//                    isIdentik="nan";
//                }

            }
            public void alert2(String Judul, String message){
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        //set icon
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        //set title
                        .setTitle(Judul)
                        //set message
                        .setMessage(message)
                        .show();
            }
            private void postJsonObject(final String wtr_picked, final String kode_picked, final String nama_picked, final String qty_picked, final String formattedDate, final String uid_picked, final String epoch, final String no_lot, final String exp_date) {
                RequestQueue queue = Volley.newRequestQueue(context);
               final String url2 ="http://10.1.250.116/rest-api/index.php/api/Item_2/index_post/";
               // final String url2 ="http://192.168.0.4/rest-api/index.php/api/Item_2/index_post/";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url2, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(context, "Input Item Successed !", Toast.LENGTH_SHORT).show();
                        Log.d("my_suc", "onResponse: "+url2);
                        String mes="Input Item "+kode_picked+" Successed ! ";
                        alert2("Berhasil",mes);
                        Log.d("My_success",""+response);
                        String sisa = txt_sisa_qty.getText().toString();
                        Double Sisa_int = Double.valueOf(sisa);
                        if(Sisa_int.equals(0.00)){
                            Double qty_wtr=Double.valueOf(txt_qty_.getText().toString());
                            Double qty_pik = Double.valueOf(qty_picked);
                            Double total = qty_wtr-qty_pik;
                            txt_sisa_qty.setText(total.toString());
                        }else{
                            Double qty_pik = Double.valueOf(qty_picked);
                            Double total = Sisa_int-qty_pik;
                            txt_sisa_qty.setText(total.toString());
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        alert2("Gagal",error.getMessage());
                        Log.d("response_put", "Error " + error.getMessage());
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {

                        Map<String,String> map = new HashMap<String, String>();
                        map.put("no_WTR_pick",wtr_picked);
                        map.put("kode_barang",kode_picked);
                        map.put("nama_barang",nama_picked);
                        map.put("qty",qty_picked);
                        map.put("time_input",formattedDate);
                        map.put("uid_picked",uid_picked);
                        map.put("epoch",epoch);
                        map.put("no_lot", no_lot);
                        return map;
                    }
                };

                queue.add(stringRequest);
            }



        });

    }
}
