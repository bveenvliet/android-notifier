/****************************************************************************
 Copyright 2014 Pluralsight LLC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

****************************************************************************/
package com.pluralsight.notifier;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class NotifierActivity extends Activity implements View.OnFocusChangeListener, TextWatcher {
    private int                 mCurID = -1;
    private NotificationManager mMgr;
    private EditText            mTitle;
    private EditText            mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Get the NotificationManager from our context so we can send and
        //  clear our notifications as directed by the user.
        mMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //  Setup our UI components and hook up our listeners to ensure
        //  we actually have content to send.
        setContentView(R.layout.activity_notifier);
        mTitle = (EditText)findViewById(R.id.notify_title);
        mTitle.setOnFocusChangeListener(this);
        mTitle.addTextChangedListener(this);
        mContent = (EditText)findViewById(R.id.notify_content);
        mContent.setOnFocusChangeListener(this);
        mContent.addTextChangedListener(this);
    }

    @Override
    protected void onPause() {
        mMgr.cancelAll();
        mCurID = -1;
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notifier, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean                 ret = false;
        Notification            notify;

        int id = item.getItemId();

        if (id == R.id.set_notify) {
            //  Using a Notification.Builder, create a new Notification object
            //  with the user's title and content.  Note that we must also set
            //  an icon of some kind, otherwise the NotificationManager will
            //  ignore our request and log a complaint.
            Notification.Builder bldr = new Notification.Builder(getApplicationContext());
            String dummy = mContent.getText().toString();
            bldr.setContentTitle(mTitle.getText().toString());
            bldr.setContentText(dummy);
            bldr.setTicker(dummy);
            bldr.setSmallIcon(android.R.drawable.ic_dialog_alert);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                //noinspection deprecation
                notify = bldr.getNotification();
            } else {
                notify = bldr.build();
            }

            //  Prevent the user from clearing the notification, we want to be
            //  responsible for clearing it via our AB button.
            notify.flags |= Notification.FLAG_NO_CLEAR;
            mCurID = (int)System.currentTimeMillis();

            //  Now that we have created our notification with the requested
            //  details, send it over to the NotificationManager for display.
            mMgr.notify(mCurID, notify);
            ret = true;
        } else if (id == R.id.clear_notify) {
            mMgr.cancel(mCurID);
            mCurID = -1;
            ret = true;
        }

        if (!ret) {
            ret = super.onOptionsItemSelected(item);
        } else {
            //  Invalidate the options menu so the buttons are updated appropriately
            invalidateOptionsMenu();
        }

        return ret;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem setItem = menu.findItem(R.id.set_notify);
        MenuItem clearItem = menu.findItem(R.id.clear_notify);

        clearItem.setEnabled(isClearAvailable());
        setItem.setEnabled(isSetAvailable());
        return super.onPrepareOptionsMenu(menu);
    }

    private boolean isSetAvailable() {
        String title = mTitle.getText().toString();
        String content = mContent.getText().toString();
        return !(title.isEmpty() || content.isEmpty()) && (mCurID == -1);
    }

    private boolean isClearAvailable() {
        return (mCurID != -1);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            invalidateOptionsMenu();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //  Ignore
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //  Ignore
    }

    @Override
    public void afterTextChanged(Editable s) {
        //  After any change, invalidate the options menu so the buttons are updated
        invalidateOptionsMenu();
    }
}
