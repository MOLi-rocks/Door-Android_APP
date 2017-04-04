package com.yy.molidoor;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MainActivity extends AppCompatActivity {
    MulticastSocket s;
    DatagramPacket dgramPacket;

    HandlerThread sendThread;
    Handler sendHnadler;

    private static final String TAG = "MainActivity";

    DatabaseReference status = FirebaseDatabase.getInstance().getReference("status");

    TextView doorstatustextbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doorstatustextbox = (TextView) findViewById(R.id.doorstatustext);
        status.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int doorstatus= snapshot.getValue(int.class);
                Log.d(TAG, "Value is: " + doorstatus);
                if (doorstatus == 1) {
                    doorstatustextbox.setText("MOLi 現在 關門中！");
                } else if (doorstatus == 0) {
                    doorstatustextbox.setText("MOLi 現在 開門中！");
                } else {
                    doorstatustextbox.setText("狀態不明\n猴子們正在努力找出問題");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                doorstatustextbox.setText("狀態不明\n猴子們正在努力找出問題");
            }
        });

        // send
        sendThread = new HandlerThread("multicasrSend");
        sendThread.start();
        sendHnadler = new Handler(sendThread.getLooper());
    }

    public void handleOpen(View view) {
        setDoor("open");
    }

    public void handleClose(View view) {
        setDoor("close");
    }

    public void setDoor(String action) {
        Toast t = Toast.makeText(MainActivity.this, action, Toast.LENGTH_LONG);
        t.show();

        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (wifi != null){
            WifiManager.MulticastLock lock = wifi.createMulticastLock("mylock");
            lock.acquire();
        }
        String command = action == "open" ? "DOOR_OPEN" : "DOOR_CLOSE";
        try {
            // send
            String msg = "{\"event\": \"" + command + "\"}";
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
