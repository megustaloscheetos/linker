package net.tacolc.linker_wear.services;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import net.tacolc.linker_wear.ShareActivity;

public class WearListenerService extends WearableListenerService {
    public static final String ACTION_CANCELLED = "net.tacolc.linker.cancelled";

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/emit")) {
            String data = new String(messageEvent.getData());
            Intent intent = new Intent(this, ShareActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("data", data);
            startActivity(intent);
        } else if (messageEvent.getPath().equals("/cancel")) {
            Intent cancelIntent = new Intent(ACTION_CANCELLED);
            sendBroadcast(cancelIntent);
        } else if (messageEvent.getPath().equals("/ping")) {
            CapabilityClient capabilityClient = Wearable.getCapabilityClient(this);
            capabilityClient.addLocalCapability("trigger_available");
            Task<CapabilityInfo> capabilityInfoTask = capabilityClient.getCapability(
                    "trigger_available",
                    CapabilityClient.FILTER_REACHABLE
            );

            capabilityInfoTask.addOnSuccessListener((capabilityInfo) -> {
                MessageClient messageClient = Wearable.getMessageClient(this);
                for (Node node : capabilityInfo.getNodes()) {
                    messageClient.sendMessage(node.getId(), "/pong", null);
                }
            });
        }
    }
}
