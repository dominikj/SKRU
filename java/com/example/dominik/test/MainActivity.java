package com.example.dominik.test;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {
     public static final String TIME = "time";
     public static final String USER = "user";
     public static final String PASSWD = "passwd";
    public static final String PREFS_NAME = "Geo_Settings";

     private EditText user;
     private EditText password;
     private EditText time;

    Receiver receiver;
    IntentFilter filter;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
         receiver = new Receiver();
         filter = new IntentFilter(MainService.BROADCAST);
         serviceIntent= new Intent(this, MainService.class);

        registerReceiver(receiver,filter);
        startService(serviceIntent);
    }
    @Override
    protected void onResume(){
        super.onResume();
        user = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.passwd);
        time = (EditText) findViewById(R.id.time);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        try {
            user.setText(settings.getString(USER,""));
            time.setText(settings.getString(TIME, ""));
            password.setText(settings.getString(PASSWD, ""));
        }catch(Exception e){
            Log.e("Błąd preferencji",e.getMessage(),e);
        }

        Button odswiez = (Button) findViewById(R.id.Przycisk);
        Button zmienCzas = (Button) findViewById(R.id.UstawCzas);
        Button zmienDaneLog = (Button) findViewById(R.id.zapiszDaneLog);

        if (zmienCzas != null) {
            zmienCzas.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    stopService(serviceIntent);
                    if (time != null && time.getText().toString().isEmpty()) {
                        time.setError("Pole nie może być puste");
                        return;
                    }
                        serviceIntent.putExtra(TIME, time.getText().toString());

                    startService(serviceIntent);
                }

                });
        }

        if (odswiez != null) {
            odswiez.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    stopService(serviceIntent);
                    startService(serviceIntent);
                }
            });
        }

        if (zmienDaneLog != null) {
            zmienDaneLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (user != null && user.getText().toString().isEmpty()) {
                        user.setError("Nazwa użytkownika nie może być pusta");
                        return;
                    }

                    if (password != null && password.getText().toString().isEmpty()) {
                        password.setError("Hasło nie może być puste");
                        return;
                    }
                    stopService(serviceIntent);
                    if ((user != null) && (password != null)) {
                        serviceIntent.putExtra(USER, user.getText().toString());
                        serviceIntent.putExtra(PASSWD, password.getText().toString());
                        startService(serviceIntent);
                    }

                }
            });
        }
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        try {
            savedInstanceState.putString(TIME, time.getText().toString());
            savedInstanceState.putString(USER, user.getText().toString());
            savedInstanceState.putString(PASSWD, password.getText().toString());
        }catch(Exception e){
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (user != null) {
            user.setText(savedInstanceState.getString(USER));
        }
        if (password != null) {
            password.setText(savedInstanceState.getString(PASSWD));
        }
        if (time != null) {
            time.setText(savedInstanceState.getString(TIME));
        }
    }
    @Override
    protected void onStop(){
        super.onStop();

        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(TIME, time.getText().toString());
            editor.putString(USER, user.getText().toString());
            editor.putString(PASSWD, password.getText().toString());
            editor.commit();
        }catch (Exception e){
            Log.e("Błąd preferencji",e.getMessage(),e);

        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);

    }
    }

