package com.topicplaces.android;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class PostTopicActivity extends AppCompatActivity {

    private boolean isPrivate;
    private String user, pass;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView navMenuDrawerList;
    private ArrayAdapter<String> navigationDrawerAdapter;
    private String[] leftSliderData = new String[4];
    private ImageView photoImageView;
    private RadioButton privateCheckBox;
    private RadioButton publicCheckBox;
    private Button postTopicButton;

    private Button takePictureButton;

    /*
     * Fields for MainActivity
     */
    private File file, filepath, dir;
    final int CAMERA_CAPTURE = 1;
    final int PIC_CROP = 2;
    private String errorMessage;
    private Toast errorToast;
    private Uri picUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_topic);

        Intent generalInfo = getIntent();
        isPrivate = generalInfo.getExtras().getBoolean("isPrivate");
        user = generalInfo.getExtras().getString("username");
        pass = generalInfo.getExtras().getString("password");

        privateCheckBox = (RadioButton) findViewById(R.id.private_check_box);
        publicCheckBox = (RadioButton) findViewById(R.id.public_check_box);

        if(isPrivate){
            privateCheckBox.setChecked(true);
        } else {
            publicCheckBox.setChecked(true);
        }

        leftSliderData[0] = getString(R.string.dashboardNavMenu);
        leftSliderData[1] = getString(R.string.privateTopicsNavMenu);
        leftSliderData[2] = getString(R.string.publicTopicsNavMenu);
        leftSliderData[3] = getString(R.string.postNewTopicNavMenu);

        nitView();
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.toolbarPostTopicTitle));
            setSupportActionBar(toolbar);
        }
        initDrawer();

        takePictureButton = (Button) findViewById(R.id.take_picture_button);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.getId() == R.id.take_picture_button) {
                    try {
                        // Standard image capture intent
                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(captureIntent, CAMERA_CAPTURE);
                    }
                    catch(ActivityNotFoundException anfe){
                        // Display an error message
                        errorMessage = "Whoops - your device doesn't support capturing images!";
                        Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        photoImageView = (ImageView) findViewById(R.id.post_topic_image_view);
        photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureButton.performClick();
            }
        });

        postTopicButton = (Button) findViewById(R.id.post_topic_button);
        postTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy =
                        new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            ConnectivityManager cm =
                    (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {

                String endPoint = "http://tse.topicplaces.com/api/2/";
                AndroidSNSController topicsController = new AndroidSNSController(endPoint);
                String authKey = topicsController.acquireKey(user, pass);

                isPrivate = privateCheckBox.isChecked();
                String topicTitle = ((EditText) findViewById(R.id.topic_title_text_field)).getText().toString();
                String topicDescription = ((EditText) findViewById(R.id.topic_description_text_field)).getText().toString();
                String TID;

                if (isPrivate) {
                    TID = topicsController.newPrivateTopic(topicTitle, authKey);
                } else {
                    TID = topicsController.newPublicTopic(topicTitle, authKey);
                }
                //A media file will be uploaded here and the MID passed to the null refrence
                //where update topic is called.

                String mediaID = null;
                mediaID = topicsController.newMediaFromLocal(file, authKey);
                topicsController.updateTopic(topicTitle, topicDescription, mediaID, isPrivate, TID, authKey);

                Intent i = new Intent(getBaseContext(), MessagesActivity.class);
                i.putExtra("isPrivate", isPrivate);
                i.putExtra("TID", TID);
                i.putExtra("username", user);
                i.putExtra("password", pass);
                startActivity(i);
            }
            }
        });

    }

    private void nitView() {
        navMenuDrawerList = (ListView) findViewById(R.id.nav_menu_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationDrawerAdapter = new ArrayAdapter<>(PostTopicActivity.this,
                R.layout.nav_drawer_choices, leftSliderData);
        navMenuDrawerList.setAdapter(navigationDrawerAdapter);
        navMenuDrawerList.setOnItemClickListener(new NavMenuListener());
    }

    private void initDrawer() {

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_topic, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.dashboard_settings) {
            return true;
        }
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent camera) {

        if (resultCode == RESULT_OK) {
            //user is returning from capturing an image using the camera
            if (requestCode == CAMERA_CAPTURE) {
                //get the Uri for the captured image
                picUri = camera.getData();
                //carry out the crop operation
                performCrop();
            }
            //user is returning from cropping the image
            else if (requestCode == PIC_CROP) {
                //get the returned data
                Bundle extras = camera.getExtras();
                //get the cropped bitmap
                Bitmap thePic = extras.getParcelable("data");
                //display the returned cropped image
                photoImageView.setImageBitmap(thePic);

                OutputStream output;
                // Find the SD Card path
                filepath = Environment.getExternalStorageDirectory();
                Log.d("FILEPATH", filepath.getAbsolutePath());

                // Create a new folder in SD Card
                dir = new File(filepath.getAbsolutePath()
                        + "/CroppedImage/");
                Log.d("DIR PATH", dir.getAbsolutePath());
                dir.mkdirs();

                // Create a name for the saved image
                file = new File(dir.getAbsolutePath(), "crop.jpg");
                Log.d("FILEPATH", file.getAbsolutePath());

                try {

                    output = new FileOutputStream(file);

                    // Compress into png format image from 0% - 100%
                    thePic.compress(Bitmap.CompressFormat.JPEG, 60, output);
                    output.flush();
                    output.close();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Crops the image taken with camera
     */
    private void performCrop(){
        //take care of exceptions
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        //respond to users whose devices do not support the crop action
        catch(ActivityNotFoundException anfe){
            //display an error message
            errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private class NavMenuListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent topicsIntent = new Intent(getBaseContext(), TopicsActivity.class);
            Intent postTopicIntent = new Intent(getBaseContext(), PostTopicActivity.class);
            Intent dashboardIntent = new Intent(getBaseContext(), DashboardActivity.class);

            switch (position) {

                case 0:
                    dashboardIntent.putExtra("username", user);
                    dashboardIntent.putExtra("password", pass);
                    startActivity(dashboardIntent);
                    break;
                case 1:
                    topicsIntent.putExtra("username", user);
                    topicsIntent.putExtra("password", pass);
                    topicsIntent.putExtra("isPrivate", true);
                    startActivity(topicsIntent);
                    break;
                case 2:
                    topicsIntent.putExtra("username", user);
                    topicsIntent.putExtra("password", pass);
                    topicsIntent.putExtra("isPrivate", false);
                    startActivity(topicsIntent);
                    break;
                case 3:
                    postTopicIntent.putExtra("username", user);
                    postTopicIntent.putExtra("password", pass);
                    postTopicIntent.putExtra("isPrivate", true);
                    startActivity(postTopicIntent);
                    break;
            }

            Log.d("NAV MENU CLICK", "SUCCESS!");

        }
    }
}


