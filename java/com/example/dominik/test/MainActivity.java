package com.example.dominik.test;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.AsyncTask;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

 //   private final String userKeyOpenCell = "c4bd2da8-2820-477f-9af5-f4846f70e58a";
    private final String userKeyOpenCell = "84e673dd-fc70-4023-8edf-8cf54532e99c"; // kod Kamila
    private final int defaultRefreshInterval =  120000; // [ms] domyślny czas odświeżania
    private int mcc; //kod kraju
    private int mnc; //kod operatora
    private int lac; // lai
    private int cid; // id komórki
    private refreshInBackground refresh = new refreshInBackground(defaultRefreshInterval);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getNetworkInfo();
        new HttpRequestTask().execute();
        refresh.start();
    }
    @Override
    protected void onResume(){
        super.onResume();

        Button odswiez = (Button) findViewById(R.id.Przycisk);
        Button zmienCzas = (Button) findViewById(R.id.UstawCzas);

        zmienCzas.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText time = (EditText) findViewById(R.id.time);
                refresh.start(Integer.parseInt(time.getText().toString())*60000); // t*minuta
            }

            });
        odswiez.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getNetworkInfo();
                new HttpRequestTask().execute();

            }
        });
    }

    /**
     * Pobiera informacje z sieci komórkowej
     */
    void getNetworkInfo(){
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

        String networkOperator = telephonyManager.getNetworkOperator();

        if (TextUtils.isEmpty(networkOperator) == false) {
            mcc = Integer.parseInt(networkOperator.substring(0, 3));
            mnc = Integer.parseInt(networkOperator.substring(3));
            cid = cellLocation.getCid();
            lac = cellLocation.getLac();
            TextView wynik = (TextView) findViewById(R.id.wynik);
            wynik.setText("Id komórki: " + Integer.toString(cid) + "\nLAC: " + Integer.toString(lac) + "\nKod kraju: " + Integer.toString(mcc) + "\nKod operatora: " + Integer.toString(mnc));
        }

    }

    /**
     * Asynchroniczne wykonywanie zapytania REST
     */
    private class HttpRequestTask extends AsyncTask<Void, Void, OpenCellJSONAnswer> {
        protected OpenCellJSONAnswer doInBackground(Void... params) {
            try {
                final String url = "Http://opencellid.org/cell/get?key=" + userKeyOpenCell + "&mcc=" +mcc+ "&mnc=" +mnc+ "&lac=" +lac+ "&cellid=" +cid+ "&format=json";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                OpenCellJSONAnswer answer = restTemplate.getForObject(url, OpenCellJSONAnswer.class);
                return answer;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        protected void onPostExecute(OpenCellJSONAnswer answer) {
            TextView wynik = (TextView) findViewById(R.id.wynik2);
            try {
                Calendar c = Calendar.getInstance();
                String now = c.getTime().toString();
                wynik.setText("Szerokość geo: " + answer.getLat() + "\nDługość geo: " + answer.getLon() + "\nTechnologia: " + answer.getRadio() +"\nOstatnia aktualizacja: " + now);
            }
            catch(Exception e){
                Log.e("MainActivity", e.getMessage(), e);
                wynik.setText("Wystąpił błąd");
            }
        }

    }
    /**
     * Timer wykonujący cykliczne zapytania REST
     */
    private class refreshInBackground {
        private int time;
        Timer timer;
        refreshInBackground(int time){
            this.time = time;
        }
        public void start(){
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new Thread() {
                        public void run() {
                            runOnUiThread(  new Runnable() {
                                public void run() {
                                    getNetworkInfo();
                                    new HttpRequestTask().execute();
                                    Log.i("Background","Odświeżam");
                                }
                            });
                        }

                    }.run();
                }

            }, 0, time);
        }

        public void start(int time){
            this.time = time;
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new Thread() {
                        public void run() {
                            runOnUiThread(  new Runnable() {
                                public void run() {
                                    getNetworkInfo();
                                    new HttpRequestTask().execute();
                                    Log.i("Background","Odświeżam");
                                }
                            });
                        }

                    }.run();
                }

            }, 0, time);
        }
        }
    }

