package com.example.dominik.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

/**
 * Created by dominik on 2016-05-06.
 */
public class Receiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        TextView wynik = (TextView)((Activity) context).findViewById(R.id.wynik);
        String data = intent.getExtras().getString(MainService.DATA1);
        wynik.setText(data);
        TextView wynik2 = (TextView)((Activity) context).findViewById(R.id.wynik2);
        String data2 = intent.getExtras().getString(MainService.DATA2);
        wynik2.setText(data2);
        
    }
}
