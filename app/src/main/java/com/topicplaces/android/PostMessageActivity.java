package com.topicplaces.android;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
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
import android.widget.TextView;



public class PostMessageActivity extends AppCompatActivity {

    /*
     * Views and Adapters
     */
    private ActionBarDrawerToggle drawerToggle;
    private ArrayAdapter<String> navigationDrawerAdapter;
    private Button postMessageButton, takePictureButton;
    private DrawerLayout drawerLayout;
    private EditText msgTitle, msgDescription;
    private ImageView photoImageView;
    private ListView navMenuDrawerList;
    private TextView topicTitleTextView;
    private Toolbar toolbar;
    private String[] leftSliderData = new String[4];

    /*
     * Fields for MainActivity
     */
    private boolean isPrivate;
    private String pass, GID,  TID, user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_message);

        Intent i = getIntent();
        isPrivate = i.getExtras().getBoolean("isPrivate");
        TID = i.getExtras().getString("TID");
        user = i.getExtras().getString("username");
        pass = i.getExtras().getString("password");

        leftSliderData[0] = getString(R.string.dashboardNavMenu);
        leftSliderData[1] = getString(R.string.privateTopicsNavMenu);
        leftSliderData[2] = getString(R.string.publicTopicsNavMenu);
        leftSliderData[3] = getString(R.string.postNewTopicNavMenu);

        nitView();
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.toolbarPostMessageTitle));
            setSupportActionBar(toolbar);
        }
        initDrawer();

        photoImageView = (ImageView)findViewById(R.id.post_msg_image_view);

        msgTitle = (EditText)findViewById(R.id.msg_title_text_field);
        msgDescription = (EditText)findViewById(R.id.msg_description_text_field);

        takePictureButton = (Button)findViewById(R.id.take_picture_button);

        postMessageButton = (Button)findViewById(R.id.post_message_button);
        postMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy =
                            new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }

                ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {

                    String endPoint = "http://tse.topicplaces.com/api/2/";
                    AndroidSNSController msgController = new AndroidSNSController(endPoint);
                    String authKey = msgController.acquireKey(user, pass);

                    String title = msgTitle.getText().toString();
                    String descrip = msgDescription.getText().toString();

                    if (isPrivate)
                        GID = msgController.newPrivateMessage(title, descrip, null, TID, authKey);
                    else
                        GID = msgController.newPublicMessage(title, descrip, null, TID, authKey);
                }

                Intent i = new Intent(getBaseContext(), ViewMessageActivity.class);
                i.putExtra("isPrivate", isPrivate);
                i.putExtra("GID", GID);
                i.putExtra("username", user);
                i.putExtra("password", pass);
                startActivity(i);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent camera) {

    }

    private void nitView() {
        navMenuDrawerList = (ListView) findViewById(R.id.nav_menu_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationDrawerAdapter = new ArrayAdapter<>(PostMessageActivity.this,
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_message, menu);
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
}
