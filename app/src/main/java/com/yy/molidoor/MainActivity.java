package com.yy.molidoor;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MainActivity extends AppCompatActivity {
    MulticastSocket s;
    DatagramPacket dgramPacket;

    HandlerThread sendThread;
    Handler sendHnadler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // send
        sendThread = new HandlerThread("multicasrSend");
        sendThread.start();
        sendHnadler = new Handler(sendThread.getLooper());
    }

    public void handleOpen(View view) {
        Toast t = Toast.makeText(MainActivity.this, "open", Toast.LENGTH_LONG);
        t.show();

        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (wifi != null){
            WifiManager.MulticastLock lock = wifi.createMulticastLock("mylock");
            lock.acquire();
        }

        try {
            // send
            String msg = "{\"event\": \"DOOR_OPEN\"}";
            InetAddress group = InetAddress.getByName("239.5.6.7");
            s = new MulticastSocket(12333);
            s.joinGroup(group);
            dgramPacket = new DatagramPacket(msg.getBytes(), msg.length(), group, 12332);
            sendHnadler.post(multicastSend);
        } catch (IOException e) {

        }
    }

    public void handleClose(View view) {
        Toast t = Toast.makeText(MainActivity.this, "open", Toast.LENGTH_LONG);
        t.show();

        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (wifi != null){
            WifiManager.MulticastLock lock = wifi.createMulticastLock("mylock");
            lock.acquire();
        }

        try {
            // send
            String msg = "{\"event\": \"DOOR_CLOSE\"}";
            InetAddress group = InetAddress.getByName("239.5.6.7");
            s = new MulticastSocket(12333);
            s.joinGroup(group);
            dgramPacket = new DatagramPacket(msg.getBytes(), msg.length(), group, 12332);
            sendHnadler.post(multicastSend);
        } catch (IOException e) {

        }
    }

    private Runnable multicastSend = new Runnable() {
        @Override
        public void run() {
              try {
                  s.send(dgramPacket);
              } catch(IOException e) {

              }
        }
    };
}
