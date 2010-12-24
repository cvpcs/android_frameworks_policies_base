/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.policy.impl;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;
import android.view.Window;
import android.view.WindowManager;

public class KillProcessDialog extends AlertDialog {
    private IntentFilter mBroadcastIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    private int mPid;

    public KillProcessDialog(Context context, int pid) {
        super(context);

        mPid = pid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getContext();

        setTitle(context.getText(com.android.internal.R.string.force_close));
        setMessage(context.getText(com.android.internal.R.string.long_press_back_kill));
        setCancelable(true);

        Window window = getWindow();
        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);


        setButton(DialogInterface.BUTTON_POSITIVE, context.getText(com.android.internal.R.string.force_close), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // kill
                Process.killProcess(mPid);
                dismiss();
            }
        });
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getText(com.android.internal.R.string.wait), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
                dismiss();
            }
        });

        if (context.getResources().getBoolean(
                com.android.internal.R.bool.config_sf_slowBlur)) {
            window.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // receive broadcasts
        getContext().registerReceiver(mBroadcastReceiver, mBroadcastIntentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // stop receiving broadcasts
        getContext().unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
                if (! PhoneWindowManager.SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    dismiss();
                }
            }
        }
    };
}
