package com.fireninjas.firebasehack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.fireninjas.firebasehack.utils.HeartBeatView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private GoogleApiClient googleClient;
    private Realm realm;

    private AppCompatButton mCare, mHeartPulse;
    private TextView mHeart, mCalory;
    private HeartBeatView heartbeat;


    String mFireUrl = "https://trialproject-d1eb6.firebaseio.com/pulse/pulse";
    Firebase mRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);


        heartbeat = (HeartBeatView)findViewById(R.id.heartbeat);
        mHeart = (TextView) findViewById(R.id.heart);
        mCalory = (TextView) findViewById(R.id.calory);
        mCare = (AppCompatButton) findViewById(R.id.reset);
        mHeartPulse = (AppCompatButton) findViewById(R.id.pulseRequest);

        mRef = new Firebase(mFireUrl);


        // Build a new GoogleApiClient that includes the Wearable API
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Realm.init(this);
        realm = Realm.getDefaultInstance();


        // Register the local broadcast receiver
        IntentFilter DataFilter = new IntentFilter(Intent.ACTION_SEND);
        HeartRateReciver DataReceiver = new HeartRateReciver();
        LocalBroadcastManager.getInstance(this).registerReceiver(DataReceiver, DataFilter);

        mHeartPulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendToDataLayerThread("/heart", "Start upbeat for heart rate").start();
            }
        });

        mCare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mCare = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(mCare);
            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }




    public class HeartRateReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("heart");
            Log.v("heart", "Mainhaertactivity received message: " + data);

            mHeart.setText(data);
            heartbeat.setDurationBasedOnBPM(Integer.valueOf(data));

            mRef.child("pulse").setValue(data);
            heartbeat.toggle();

            // Displaysage in UI

//            new SendToDataLayerThread("/message_path","You:-\n" + message).start();
        }
    }

    public class HydrateReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("hydrate");
            Log.v("heart", "Mainhaertactivity received message: " + data);

            mHeart.setText(data);
            heartbeat.setDurationBasedOnBPM(Integer.valueOf(data));
            heartbeat.toggle();

            // Displaysage in UI

//            new SendToDataLayerThread("/message_path","You:-\n" + message).start();
        }
    }

    class SendToDataLayerThread extends Thread {
        String path;
        String message;

        // Constructor to send a message to the data layer
        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.v("myTag", "ERROR: failed to send Message");
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();

    }

    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }


}
