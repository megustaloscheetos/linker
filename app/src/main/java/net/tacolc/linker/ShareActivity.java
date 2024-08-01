package net.tacolc.linker;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.tacolc.linker.services.WearListenerService;

public class ShareActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Intent intent = getIntent();
        if (!Intent.ACTION_SEND.equals(intent.getAction()))
            return;

        WearListenerService.setOnCompleteCallback(this::finish);
        BottomSheet bottomSheet = new BottomSheet(intent.getStringExtra(Intent.EXTRA_TEXT));
        bottomSheet.show(getSupportFragmentManager(), "BottomSheet");
    }
}