package com.example.watchapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity implements MessageClient.OnMessageReceivedListener {

    private Button button;

    private Node node;

    private MonumentViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.search_button);
        button.setOnClickListener(v -> requestMonumentList());

        Wearable.getMessageClient(this).addListener(this);

        WearableRecyclerView wearableRecyclerView = (WearableRecyclerView) findViewById(R.id.wearable_recycler_view);
        wearableRecyclerView.setEdgeItemsCenteringEnabled(true);
        wearableRecyclerView.setHasFixedSize(true);
        wearableRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this));

        mAdapter = new MonumentViewAdapter(new String[]{});
        wearableRecyclerView.setAdapter(mAdapter);

        new PagerSnapHelper().attachToRecyclerView(wearableRecyclerView);
    }

    private void requestMonumentList() {
        button.setEnabled(false);
        CapabilityClient capabilityClient = Wearable.getCapabilityClient(this);
        Task<Map<String, CapabilityInfo>> capabilityTask =
                capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE);

        capabilityTask.addOnSuccessListener(capabilityInfoMap -> {
            if (capabilityInfoMap.isEmpty()) {
                node = null;
                button.setEnabled(true);
                return;
            }

            CapabilityInfo capacityInfo = capabilityInfoMap.get("send_monument_list");
            if (capacityInfo == null) {
                node = null;
                button.setEnabled(true);
                return;
            }

            Set<Node> nodes = capacityInfo.getNodes();
            if (!nodes.isEmpty()) {
                node = nodes.iterator().next();
                Log.i("MainActivity", "Node found: " + node.getDisplayName());
                MessageClient messageClient = Wearable.getMessageClient(this);
                messageClient.sendMessage(node.getId(), "/monument_request", null);
            }  else {
                node = null;
                button.setEnabled(true);
            }
        });
        Handler.createAsync(getMainLooper()).postDelayed(() -> button.setEnabled(true), 3000);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.i("MainActivity", "Message received: " + messageEvent.getPath());
        if (messageEvent.getPath().equals("/monument_list")) {
            button.setEnabled(true);
            String[] monuments = new String(messageEvent.getData()).split("\n");
            Log.i("MainActivity", "Monuments: " + Arrays.toString(monuments));
            mAdapter.setDataset(monuments);
        }
    }
}