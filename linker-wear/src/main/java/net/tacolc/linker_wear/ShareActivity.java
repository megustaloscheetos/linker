package net.tacolc.linker_wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.tacolc.linker_wear.services.ApduService;

public class ShareActivity extends AppCompatActivity {
    public static final String ACTION_CANCELLED = "net.tacolc.linker.cancelled";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(ACTION_CANCELLED))
                return;

            finishAndRemoveTask();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Intent intent = getIntent();
        if (intent.hasExtra("data")) {
            Intent serviceIntent = new Intent(this, ApduService.class);
            serviceIntent.putExtra("data", intent.getStringExtra("data"));
            startService(serviceIntent);

            IntentFilter filter = new IntentFilter(ACTION_CANCELLED);
            registerReceiver(broadcastReceiver, filter, RECEIVER_EXPORTED);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);

        Intent cancelIntent = new Intent(ACTION_CANCELLED);
        sendBroadcast(cancelIntent);
    }
}