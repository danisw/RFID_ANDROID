package com.acs.btdemo;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    EditText editTextUsername, editTextPassword;
    ProgressBar progressBar;
    String id_user_db, username_db, nama_db, wh_id, wh_name, company, company_id;
    AnimationDrawable animationDrawable;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        ConstraintLayout cl = (ConstraintLayout) findViewById(R.id.cl);
        animationDrawable =(AnimationDrawable) cl.getBackground();
        animationDrawable.setEnterFadeDuration(5000);
        animationDrawable.setExitFadeDuration(2000);

        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, UserProfile.class));
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        //if user presses on login
        //calling the method login
        findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (animationDrawable != null && !animationDrawable.isRunning())
            animationDrawable.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animationDrawable != null && animationDrawable.isRunning())
            animationDrawable.stop();
    }

    private void userLogin() {
        //first getting the values
        final String username = editTextUsername.getText().toString();
        final String password = editTextPassword.getText().toString();

        //validating inputs
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Please enter your username");
            editTextUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Please enter your password");
            editTextPassword.requestFocus();
            return;
        }


        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerUrl.URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);

                        Log.d("login_respose",""+response);
                        try {
                            //converting response to json object
                            //JSONObject obj = new JSONObject(response);
                            //if(!obj.toString().equals("[]") || !obj.toString().equals("Gagal") )
                            if(!response.equals("[]")){

                                JSONArray obj = new JSONArray(response);
                                Log.d("login_arrya",""+obj.toString());
                                Log.d("login_arrya",""+response);

                                for(int i=0; i < obj.length(); i++) {
                                    JSONObject jsonobject = obj.getJSONObject(i);
                                    id_user_db       = jsonobject.getString("id");
                                    username_db    = jsonobject.getString("username");
                                    nama_db = jsonobject.getString("nama");
                                    wh_id   =jsonobject.getString("WH_ID");
                                    wh_name = jsonobject.getString("wh_name");
                                    company = jsonobject.getString("company");
                                    company_id = jsonobject.getString("company_id");
                                }
                                Users user =new Users(id_user_db,username,nama_db,wh_id,wh_name,company,company_id);
                                SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);
                                //starting the profile activity
                                finish();
                                startActivity(new Intent(getApplicationContext(), UserProfile.class));
                            }else{
                                Log.d("error_obj_array", "ERROR ARRAY NULL");
                                alert("Gagal Login","Invalid username / password");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("error_login", "onResponse: "+e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        alert("Gagal Login",""+error.getMessage());
                    }
                })  {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                Log.d("login_response", "onResponse: "+ServerUrl.URL_LOGIN);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void alert(String Judul, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(Judul)
                .setMessage(message)
                .show();
    }
}
