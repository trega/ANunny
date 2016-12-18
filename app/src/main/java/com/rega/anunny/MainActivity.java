package com.rega.anunny;

import android.Manifest;
import android.app.FragmentManager;
import android.content.ComponentName;
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
import android.util.Log;
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
    private static final String TAG = R.string.main_log_tag +  "_MAIN";
    private static final String TAG_RETAINER_FRAGMENT = "retainer_fragment";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private ComponentName mServiceName;
    private RetainerFragment mRetainerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        FragmentManager fm = getFragmentManager();
        mRetainerFragment = (RetainerFragment) fm.findFragmentByTag(TAG_RETAINER_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mRetainerFragment == null) {
            mRetainerFragment = new RetainerFragment();
            fm.beginTransaction().add(mRetainerFragment, TAG_RETAINER_FRAGMENT).commit();
            startCameraService();
        }else{
            mServiceName = mRetainerFragment.getServerName();
        }

        initTextureView();
        setSupportActionBar(toolbar);




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectClientSocket();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    private void connectClientSocket()
    {
        if (mSocketClient == null)
        {
            Log.v(TAG, "Creating SocketClient");
            mSocketClient = new SocketClient();

        }
        if(!mSocketClient.isSocketConnected())
        {
            Log.v(TAG, "Connecting socket in mSocketClient");
            mSocketClient.initialize(CommonInterface.CAMERA_SVC_TCP_PORT);
        }
    }

    private void startCameraService() {

        Intent serviceIntent = new Intent(this, CameraService.class);
        mServiceName  = startService(serviceIntent);
        mRetainerFragment.setServerName(mServiceName);

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
        if(mSocketClient != null)
            mSocketClient.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    protected void onDestroy()
    {
//        stopCameraService();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
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