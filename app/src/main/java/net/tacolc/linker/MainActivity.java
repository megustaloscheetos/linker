package net.tacolc.linker;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import net.tacolc.linker.services.WearListenerService;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setPingPongTesterCallback(this, () -> runOnUiThread(() -> {
            TextView textView = findViewById(R.id.text_main);
            textView.setText(R.string.wear_app_installed);
        }));
    }

    public static void setPingPongTesterCallback(Context context, Runnable runnable) {
        CapabilityClient capabilityClient = Wearable.getCapabilityClient(context);
        capabilityClient.addLocalCapability("trigger_available");
        Task<CapabilityInfo> capabilityInfoTask = capabilityClient.getCapability(
                "trigger_available",
                CapabilityClient.FILTER_REACHABLE
        );

        capabilityInfoTask.addOnSuccessListener(capabilityInfo -> {
            WearListenerService.setOnPongCallback(runnable);

            MessageClient messageClient = Wearable.getMessageClient(context);
            for (Node node : capabilityInfo.getNodes()) {
                messageClient.sendMessage(node.getId(), "/ping", null);
            }
        });
    }
}