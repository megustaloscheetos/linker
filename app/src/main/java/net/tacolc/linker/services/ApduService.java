/*
 * Copyright 2013 TechBooster
 * Copyright 2023 AndroidCrypto
 * Copyright 2024 Mizael Morales
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copied and adapted from NfcHceNdefEmulator: https://github.com/MichaelsPlayground/NfcHceNdefEmulator
 * https://github.com/MichaelsPlayground/NfcHceNdefEmulator/blob/master/app/src/main/java/de/androidcrypto/nfchcendefemulator/MyHostApduService.java
 *
 * DESCRIPTION OF MODIFICATIONS PER APACHE 2.0:
 * Most of the implementation of the NDEF HCE protocol remains largely the same. However, application
 * specific code to NfcHceNdefEmulator has been removed or adapted to this application's needs and requirements.
 *
 */

package net.tacolc.linker.services;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class ApduService extends HostApduService {
    private static final byte[] SELECT_APPLICATION = {
            (byte) 0x00, // CLA	- Class - Class of instruction
            (byte) 0xA4, // INS	- Instruction - Instruction code
            (byte) 0x04, // P1	- Parameter 1 - Instruction parameter 1
            (byte) 0x00, // P2	- Parameter 2 - Instruction parameter 2
            (byte) 0x07, // Lc field	- Number of bytes present in the data field of the command
            (byte) 0xD2, (byte) 0x76, (byte) 0x00, (byte) 0x00, (byte) 0x85, (byte) 0x01, (byte) 0x01, // NDEF Tag Application name D2 76 00 00 85 01 01
            (byte) 0x00  // Le field	- Maximum number of bytes expected in the data field of the response to the command
    };

    private static final byte[] SELECT_CAPABILITY_CONTAINER = {
            (byte) 0x00, // CLA	- Class - Class of instruction
            (byte) 0xa4, // INS	- Instruction - Instruction code
            (byte) 0x00, // P1	- Parameter 1 - Instruction parameter 1
            (byte) 0x0c, // P2	- Parameter 2 - Instruction parameter 2
            (byte) 0x02, // Lc field	- Number of bytes present in the data field of the command
            (byte) 0xe1, (byte) 0x03 // file identifier of the CC file
    };

    private static final byte[] SELECT_NDEF_FILE = {
            (byte) 0x00, // CLA	- Class - Class of instruction
            (byte) 0xa4, // Instruction byte (INS) for Select command
            (byte) 0x00, // Parameter byte (P1), select by identifier
            (byte) 0x0c, // Parameter byte (P1), select by identifier
            (byte) 0x02, // Lc field	- Number of bytes present in the data field of the command
            (byte) 0xE1, (byte) 0x04 // file identifier of the NDEF file retrieved from the CC file
    };

    private final static byte[] CAPABILITY_CONTAINER_FILE = new byte[] {
            0x00, 0x0f, // CCLEN
            0x20, // Mapping Version
            0x00, 0x3b, // Maximum R-APDU data size
            0x00, 0x34, // Maximum C-APDU data size
            0x04, 0x06, // Tag & Length
            (byte)0xe1, 0x04, // NDEF File Identifier
            (byte) 0x00, (byte) 0xff, // Maximum NDEF size, do NOT extend this value
            0x00, // NDEF file read access granted
            (byte)0xff, // NDEF File write access denied
    };

    // Status Word success
    private final static byte[] SUCCESS_SW = new byte[] {
            (byte)0x90,
            (byte)0x00,
    };
    // Status Word failure
    private final static byte[] FAILURE_SW = new byte[] {
            (byte)0x6a,
            (byte)0x82,
    };

    private boolean startedFromApp = false;

    private byte[] mNdefRecordFile;

    private boolean mAppSelected; // true when SELECT_APPLICATION detected

    private boolean mCcSelected; // true when SELECT_CAPABILITY_CONTAINER detected

    private boolean mNdefSelected; // true when SELECT_NDEF_FILE detected

    private static Runnable onDoneCallback = null;

    public static void setOnDoneCallback(Runnable onDoneCallback) {
        ApduService.onDoneCallback = onDoneCallback;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAppSelected = false;
        mCcSelected = false;
        mNdefSelected = false;

        NdefRecord ndefRecord =
                NdefRecord.createTextRecord("en", "No Active Share Requests. Start One from the Share Menu!");
        NdefMessage ndefMessage = new NdefMessage(ndefRecord);

        int nlen = ndefMessage.getByteArrayLength();
        mNdefRecordFile = new byte[nlen + 2];
        mNdefRecordFile[0] = (byte)((nlen & 0xff00) / 256);
        mNdefRecordFile[1] = (byte)(nlen & 0xff);
        System.arraycopy(ndefMessage.toByteArray(), 0, mNdefRecordFile, 2, ndefMessage.getByteArrayLength());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("data")) {
            NdefRecord ndefRecord;
            try {
                URL url = new URL(intent.getStringExtra("data"));
                ndefRecord = NdefRecord.createUri(url.toString());
            } catch (MalformedURLException ignored) {
                ndefRecord = NdefRecord.createTextRecord("en", intent.getStringExtra("data"));
            }

            NdefMessage ndefMessage = new NdefMessage(ndefRecord);
            int nlen = ndefMessage.getByteArrayLength();
            mNdefRecordFile = new byte[nlen + 2];
            mNdefRecordFile[0] = (byte) ((nlen & 0xff00) / 256);
            mNdefRecordFile[1] = (byte) (nlen & 0xff);

            System.arraycopy(ndefMessage.toByteArray(), 0, mNdefRecordFile, 2, ndefMessage.getByteArrayLength());
            startedFromApp = true;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * emulates an NFC Forum Tag Type 4
     */
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        if (Arrays.equals(SELECT_APPLICATION, commandApdu)) {
            if (!startedFromApp) {
                return FAILURE_SW;
            }

            mAppSelected = true;
            mCcSelected = false;
            mNdefSelected = false;
            return SUCCESS_SW;
        } else if (mAppSelected && Arrays.equals(SELECT_CAPABILITY_CONTAINER, commandApdu)) {
            mCcSelected = true;
            mNdefSelected = false;
            return SUCCESS_SW;
        } else if (mAppSelected && Arrays.equals(SELECT_NDEF_FILE, commandApdu)) {
            mCcSelected = false;
            mNdefSelected = true;
            return SUCCESS_SW;
        } else if (commandApdu[0] == (byte)0x00 && commandApdu[1] == (byte)0xb0) {
            int offset = (0x00ff & commandApdu[2]) * 256 + (0x00ff & commandApdu[3]);
            int le = 0x00ff & commandApdu[4];

            byte[] responseApdu = new byte[le + SUCCESS_SW.length];

            if (mCcSelected && offset == 0 && le == CAPABILITY_CONTAINER_FILE.length) {
                System.arraycopy(CAPABILITY_CONTAINER_FILE, offset, responseApdu, 0, le);
                System.arraycopy(SUCCESS_SW, 0, responseApdu, le, SUCCESS_SW.length);
                return responseApdu;
            } else if (mNdefSelected) {
                if (offset + le <= mNdefRecordFile.length) {
                    System.arraycopy(mNdefRecordFile, offset, responseApdu, 0, le);
                    System.arraycopy(SUCCESS_SW, 0, responseApdu, le, SUCCESS_SW.length);
                    return responseApdu;
                }
            }
        }

        return FAILURE_SW;
    }

    @Override
    public void onDeactivated(int reason) {
        if (onDoneCallback != null) {
            onDoneCallback.run();
            onDoneCallback = null;

            CapabilityClient capabilityClient = Wearable.getCapabilityClient(this);
            capabilityClient.addLocalCapability("trigger_available");
            Task<CapabilityInfo> capabilityInfoTask = capabilityClient.getCapability(
                    "trigger_available",
                    CapabilityClient.FILTER_REACHABLE
            );

            capabilityInfoTask.addOnSuccessListener(capabilityInfo -> {
                MessageClient messageClient = Wearable.getMessageClient(this);
                for (Node node : capabilityInfo.getNodes()) {
                    messageClient.sendMessage(node.getId(), "/done", null);
                }
            });
        }

        stopSelf();
    }
}
