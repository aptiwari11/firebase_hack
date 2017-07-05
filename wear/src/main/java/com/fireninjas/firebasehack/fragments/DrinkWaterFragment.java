package com.fireninjas.firebasehack.fragments;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.fireninjas.firebasehack.R;
import com.fireninjas.firebasehack.services.WaterReminderReceiver;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;


public class DrinkWaterFragment extends Fragment {

    private AppCompatButton mStart;
    private AppCompatButton mStop;
    private Toast mToast;

    private GoogleApiClient mGoogleApiClient;

    public DrinkWaterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.drink_water_fragment, container, false);

        mStart = (AppCompatButton) rootView.findViewById(R.id.start);
        mStop = (AppCompatButton) rootView.findViewById(R.id.stop);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addApi(Wearable.API).build();
        mGoogleApiClient.connect();

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WaterReminderReceiver.class);
                PendingIntent sender = PendingIntent.getBroadcast(getActivity(),
                        0, intent, 0);

                // We want the alarm to go off 5 seconds from now.
                long firstTime = SystemClock.elapsedRealtime();
                firstTime += 5 * 1000;

                // Schedule the alarm!
                AlarmManager am = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        firstTime, 5 * 1000, sender);
                //DOZE MODE SUPPORT
                am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, sender);

                // Tell the user about what we did.
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(getActivity(), "Subscribed to water alarm",
                        Toast.LENGTH_LONG);
                mToast.show();
            }
        });



        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        // Create the same intent, and thus a matching IntentSender, for
                // the one that was scheduled.
                Intent intent = new Intent(getActivity(), WaterReminderReceiver.class);
                PendingIntent sender = PendingIntent.getBroadcast(getActivity(),
                        0, intent, 0);

                // And cancel the alarm.
                AlarmManager am = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                am.cancel(sender);

                // Tell the user about what we did.
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(getActivity(), "Unsubscribed from water reminder",
                        Toast.LENGTH_LONG);
                mToast.show();
            }
        });

        return rootView;
    }

    private void sendMessageToHandheld(final String message) {

        if (mGoogleApiClient == null)
            return;

        // use the api client to send the heartbeat value to our handheld
        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                final List<Node> nodes = result.getNodes();
                final String path = "/hydrate";

                for (Node node : nodes) {
                    Log.d("hydrate count", "SEND MESSAGE TO HANDHELD: " + message);
                    byte[] data = message.getBytes(StandardCharsets.UTF_8);
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, data);
                }
            }
        });
    }

}
