package com.tokbox.android.tutorials.basic_video_chat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Stream;
import com.tokbox.android.tutorials.basicvideochat.R;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends Activity
                            implements EasyPermissions.PermissionCallbacks, PublisherKit.PublisherListener {

    private Timer timer = new Timer();
    private static int timerCounter;

    private static final String LOG_TAG = SecondActivity.class.getSimpleName();

    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private static Thread thread;

    private Publisher mPublisher;

    private FrameLayout mPublisherViewContainer;
    private Button mVideoChatButton;

    private static final boolean enablePublisher = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timer.schedule(new MyTimerTask(), 0, 500);

        if(enablePublisher) {
            mPublisherViewContainer = (FrameLayout) findViewById(R.id.second_publisher);
        }

        mVideoChatButton = (Button)findViewById(R.id.video_chat);
        mVideoChatButton.setOnClickListener(view -> goNxtScreen());

        requestPermissions();
    }

    /* Activity lifecycle methods */

    @Override
    protected void onPause() {

        Log.d(LOG_TAG, "onPause");

        super.onPause();

        if(enablePublisher) {
            unpublish();
        }
        load();
    }

    @Override
    protected void onResume() {

        Log.d(LOG_TAG, "onResume");

        super.onResume();

        if(enablePublisher) {
            publish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        Log.d(LOG_TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        Log.d(LOG_TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setRationale(getString(R.string.rationale_ask_again))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel))
                    .setRequestCode(RC_SETTINGS_SCREEN_PERM)
                    .build()
                    .show();
        }
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {

        String[] perms = { Manifest.permission.CAMERA };
        if (EasyPermissions.hasPermissions(this, perms)) {

        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }

    private void goNxtScreen() {
        startActivity(new Intent(this, SecondActivity.class));
    }

    private void publish() {

        // initialize Publisher and set this object to listen to Publisher events
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);

        // set publisher video style to fill view
        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        mPublisherViewContainer.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(false);
        }
        mPublisher.startPreview();
    }

    private void unpublish() {
        mPublisherViewContainer.removeAllViews();
        mPublisher = null;
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

        Log.d(LOG_TAG, "onStreamCreated: Publisher Stream Created. Own stream "+stream.getStreamId());
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

        Log.d(LOG_TAG, "onStreamDestroyed: Publisher Stream Destroyed. Own stream "+stream.getStreamId());
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

        Log.e(LOG_TAG, "onError: "+opentokError.getErrorDomain() + " : " +
                opentokError.getErrorCode() +  " - "+opentokError.getMessage());
    }

    private void load() {
        thread = new Thread(){
            int i = 1024;
            public void run(){

                Vector v = new Vector();
                while (i > 0)
                {
                    Runtime rt = Runtime.getRuntime();
                    long freeMemory = rt.freeMemory();
                    System.out.println( "free memory: " + freeMemory);
                    if(freeMemory > 64000)
                    {
                        byte b[] = new byte[i];
                        v.add(b);
                    }
                }
            }
        };
        thread.start();
    }

    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timerCounter++;
                    int color =0;
                    switch (timerCounter % 3){
                        case 0:
                            color = Color.RED;
                            break;
                        case 1:
                            color = Color.GREEN;
                            break;
                        case 2:
                            color = Color.BLUE;
                            break;
                    }


                    mVideoChatButton.setBackgroundColor(color);
                }
            });
        }
    }
}
