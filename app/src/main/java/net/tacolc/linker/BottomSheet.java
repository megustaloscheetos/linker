package net.tacolc.linker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import net.tacolc.linker.services.ApduService;

import java.util.function.Consumer;

public class BottomSheet extends BottomSheetDialogFragment {
    private final String data;

    private boolean stopRequested = false;
    private boolean usingWatch = false;

    public BottomSheet(String data) {
        this.data = data;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        MaterialTextView textView = view.findViewById(R.id.hint_textview);
        MaterialButton button = view.findViewById(R.id.share_toggle_button);
        button.setOnClickListener((e) -> {
            if (usingWatch) {
                executeOnCapability((capabilityInfo) -> {
                    MessageClient messageClient = Wearable.getMessageClient(getActivity());
                    for (Node node : capabilityInfo.getNodes()) {
                        messageClient.sendMessage(node.getId(), "/cancel", data.getBytes());
                    }
                });

                startShareMobile(data);
                button.setIcon(AppCompatResources.getDrawable(getActivity(), R.drawable.baseline_watch_24));
                textView.setText(R.string.phone_tap_hint);
            } else {
                executeOnCapability((capabilityInfo) -> {
                    MessageClient messageClient = Wearable.getMessageClient(getActivity());
                    for (Node node : capabilityInfo.getNodes()) {
                        messageClient.sendMessage(node.getId(), "/emit", data.getBytes());
                    }
                });

                stopRequested = true;
                Intent serviceIntent = new Intent(getActivity(), ApduService.class);
                getActivity().stopService(serviceIntent);
                button.setIcon(AppCompatResources.getDrawable(getActivity(), R.drawable.baseline_smartphone_24));
                textView.setText(R.string.watch_tap_hint);
            }

            usingWatch = !usingWatch;
        });

        executeOnCapability((success) -> {}, () -> button.setVisibility(View.GONE));
        startShareMobile(data);

        MainActivity.setPingPongTesterCallback(getActivity(), () ->
                getActivity().runOnUiThread(() -> button.setVisibility(View.VISIBLE)));

        return view;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        if (usingWatch) {
            executeOnCapability((capabilityInfo) -> {
                MessageClient messageClient = Wearable.getMessageClient(getActivity());
                for (Node node : capabilityInfo.getNodes()) {
                    messageClient.sendMessage(node.getId(), "/cancel", data.getBytes());
                }
            });

            getActivity().finish();
            return;
        }

        Intent serviceIntent = new Intent(getActivity(), ApduService.class);
        getActivity().stopService(serviceIntent);
        getActivity().finish();
    }

    private void startShareMobile(String data) {
        stopRequested = false;

        Intent serviceIntent = new Intent(getActivity(), ApduService.class);
        serviceIntent.putExtra("data", data);
        getActivity().startService(serviceIntent);

        ApduService.setOnDoneCallback(() -> {
            if (stopRequested)
                return;

            getActivity().finish();
        });
    }

    private void executeOnCapability(Consumer<CapabilityInfo> consumer) {
        CapabilityClient capabilityClient = Wearable.getCapabilityClient(getActivity());
        capabilityClient.addLocalCapability("trigger_available");
        Task<CapabilityInfo> capabilityInfoTask = capabilityClient.getCapability(
                "trigger_available",
                CapabilityClient.FILTER_REACHABLE
        );

        capabilityInfoTask.addOnSuccessListener(consumer::accept);
    }

    private void executeOnCapability(Consumer<CapabilityInfo> consumerSuccess, Runnable runnableFailure) {
        CapabilityClient capabilityClient = Wearable.getCapabilityClient(getActivity());
        capabilityClient.addLocalCapability("trigger_available");
        Task<CapabilityInfo> capabilityInfoTask = capabilityClient.getCapability(
                "trigger_available",
                CapabilityClient.FILTER_REACHABLE
        );

        capabilityInfoTask.addOnSuccessListener(consumerSuccess::accept);
        capabilityInfoTask.addOnFailureListener(info -> runnableFailure.run());
    }
}
