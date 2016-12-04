package com.rega.anunny;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import CommonInterface.CommonInterface;
import camera_service.CameraService;

public class MainActivity extends AppCompatActivity {
    private TextureView mTextureImagePreview;
    SocketClient mSocketClient;
    private static final String TAG = "NUNNY_MAIN";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        initTextureView();
        setSupportActionBar(toolbar);

        startCameraService();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSocketClient == null)
                {
                    mSocketClient = new SocketClient();
                    mSocketClient.initialize(CommonInterface.CAMERA_SVC_TCP_PORT);
                }
                else{
                    showToast(TAG + ": client not ready, try again in a while");
                }
//                if(mSocketClient.isClientRunning()){
//                    showToast(TAG + ": sending request to server");
//                    mSocketClient.sendRequest(CommonInterface.CaptureModes.CAPTURE_ONCE);
//                }else{
//                    showToast(TAG + ": client not ready, try again in a while");
//                }

            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    private void startCameraService() {
        Intent serviceIntent = new Intent(this, CameraService.class);
        startService(serviceIntent);
    }

    private void stopCameraService() {
        Intent serviceIntent = new Intent(this, CameraService.class);
        stopService(serviceIntent);
    }
    private void initTextureView() {
        requestCameraPermissions();
        mTextureImagePreview = (TextureView) findViewById(R.id.textureImagePreview);
    }

    private void requestCameraPermissions() {
        final int REQUEST_CAMERA_PERMISSION = 200;
        // Add permission for camera and let user grant the permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION);
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
//        camera.stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        camera.startBackgroundThread();
//        if (mTextureImagePreview.isAvailable()) {
//            camera.openCamera();
//        } else {
//            mTextureImagePreview.setSurfaceTextureListener(surfaceTextureListener);
//        }
    }

    public void showSnackbar(View view, String msg) {
        Snackbar.make(view, msg,
                Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCameraService();
        mSocketClient.stop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public TextureView getTextureImagePreview(){
        return mTextureImagePreview;
    }

    private void showToast(String text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, TAG + text, duration);
        toast.show();
    }
}

//TODOs:
//TODO: add client/server mode selection
//TODO: change app icon
