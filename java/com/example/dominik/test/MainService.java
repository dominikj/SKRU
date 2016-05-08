package com.example.dominik.test;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * Created by dominik on 2016-05-06.
 */

import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends IntentService {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private final String userKeyOpenCell = "c4bd2da8-2820-477f-9af5-f4846f70e58a";
    //private final String userKeyOpenCell = "84e673dd-fc70-4023-8edf-8cf54532e99c"; // kod Kamila
    private String mcc, mnc, cid, lac, user, password;
    public static final String BROADCAST = "com.example.dominik.test.BROADCAST";
    public static final String DATA1 = "data";
    public static final String DATA2 = "data2";

    Intent broadcastIntent;
    private final int defaultRefreshInterval =  1; // [min] domyślny czas odświeżania
    private refreshInBackground refresh= new refreshInBackground();
    public String interval = Integer.toString(defaultRefreshInterval);

    public MainService(){
        super("MainService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        broadcastIntent = new Intent();
        broadcastIntent.setAction(BROADCAST);

        Bundle extra  = intent.getExtras();
        if(extra != null){
            if (extra.getString(MainActivity.TIME) != null)
                interval = extra.getString(MainActivity.TIME);

            if (extra.getString(MainActivity.USER) != null && extra.getString(MainActivity.PASSWD) != null) {
                user = extra.getString(MainActivity.USER);
                password = extra.getString(MainActivity.PASSWD);
            }
        }


        getNetworkInfo();
        HttpRequestTask task = new HttpRequestTask();
        new Thread(task).start();
        refresh.start(Integer.parseInt(interval));

    }



    /**
     * Pobiera informacje z sieci komórkowej
     */
    void getNetworkInfo(){
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

        String networkOperator = telephonyManager.getNetworkOperator();

        if (TextUtils.isEmpty(networkOperator) == false) {
            mcc = networkOperator.substring(0, 3);
            mnc = networkOperator.substring(3);
            cid = Integer.toString(cellLocation.getCid());
            lac = Integer.toString(cellLocation.getLac());
            String data = "Id komórki: " + cid + "\nLAC: " + lac + "\nKod kraju: " + mcc + "\nKod operatora: " + mnc;
            broadcastIntent.putExtra(DATA1, data);
            sendBroadcast(broadcastIntent);

        }

    }

    private class HttpRequestTask implements Runnable {
        public OpenCellJSONAnswer answer;

        protected OpenCellJSONAnswer doInBackground(Void... params) {
            try {
                final String url = "Http://opencellid.org/cell/get?key=" + userKeyOpenCell + "&mcc=" +mcc+ "&mnc=" +mnc+ "&lac=" +lac+ "&cellid=" +cid+ "&format=json";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                OpenCellJSONAnswer answer = restTemplate.getForObject(url, OpenCellJSONAnswer.class);
                return answer;
            } catch (Exception e) {
                Log.e("JSONTask", e.getMessage(), e);
            }

            return null;
        }

        protected void onPostExecute(OpenCellJSONAnswer answer) {
            String info = "-----";
            try {
              new Thread(new GeoSendTask(answer)).start();
                Calendar c = Calendar.getInstance();
                String now = c.getTime().toString();
                 info = "Szerokość geo:" + ((answer.getLat()==null) ? answer.getError() : answer.getLat() )+ "\nDługość geo: " + answer.getLon() + "\nOstatnia aktualizacja: " +now;

            }
            catch(Exception e){
                Log.e("MainActivity", e.getMessage(), e);
                info = "Błąd połączenia";
            }
            finally {
                broadcastIntent.putExtra(DATA2, info);
                sendBroadcast(broadcastIntent);
            }
        }

        @Override
        public void run() {
           onPostExecute(doInBackground());
        }
    }

    private class GeoSendTask implements Runnable{
        private OpenCellJSONAnswer answer;
        GeoSendTask(OpenCellJSONAnswer ans){
            answer =ans;
        }
        protected Void doInBackground(OpenCellJSONAnswer... coords){
            SocketAddress sockaddr = null;
            try {
                sockaddr = new InetSocketAddress(InetAddress.getByName("46.101.248.111"), 22029);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            Socket soc = new Socket();
            PrintWriter out = null;
            try {
                soc.connect(sockaddr, 5000);
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(soc.getOutputStream())),
                        true);
                out.println("LOGIN: " + user);
                out.println("PASS: " + password);
                out.println("LAT: " +coords[0].getLat());
                out.println("LONG: " +coords[0].getLon());
                out.println("");
                out.flush();
                BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                String read = in.readLine();
                if (read.equals("ERR_LOG")) {
                    broadcastIntent.putExtra(DATA2, "Błąd logowania");
                    sendBroadcast(broadcastIntent);
                }
                System.out.println("ODPOWIEDZ:" + read);
                soc.close();
            } catch (NullPointerException e) {
                Log.e("GeoSendTask", e.getMessage(), e);
                out.println("LAT: " +0.0);
                out.println("LONG: " +0.0);
                out.println("");
                out.flush();
                try {
                    soc.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
            catch (IOException e){
                Log.e("GeoSendTask", e.getMessage(), e);
            }
            return null;
        }

        @Override
        public void run() {
            doInBackground(answer);

        }
    }

    private class refreshInBackground {
        Timer timer = new Timer();
        public void start(int time){
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new Thread(new HttpRequestTask()).start();
                }
            }, 0, time*60000);
        }
    }
}

