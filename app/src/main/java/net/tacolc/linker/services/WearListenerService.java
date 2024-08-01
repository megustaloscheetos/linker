package net.tacolc.linker.services;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearListenerService extends WearableListenerService {
    private static Runnable onCompleteCallback = null;
    private static Runnable onPongCallback = null;

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/done")) {
            if (onCompleteCallback != null) {
                onCompleteCallback.run();
                onCompleteCallback = null;
            }
        } else if (messageEvent.getPath().equals("/pong")) {
            if (onPongCallback != null) {
                onPongCallback.run();
                onPongCallback = null;
            }
        }
    }

    public static void setOnCompleteCallback(Runnable onCompleteCallback) {
        WearListenerService.onCompleteCallback = onCompleteCallback;
    }

    public static void setOnPongCallback(Runnable onPongCallback) {
        WearListenerService.onPongCallback = onPongCallback;
    }
}
