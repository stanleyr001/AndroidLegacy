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
import android.widget.ListView;

import java.util.Map;

public class TopicsActivity extends AppCompatActivity {

    /*
     * Fields for the Navigation Menu
     */
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView navMenuDrawerList;
    private ArrayAdapter<String> navigationDrawerAdapter;
    private String[] leftSliderData = new String[4];

    /*
     * Fields for the Activity
     */
    private boolean isPrivate;
    private String user, pass;
    private ListView topicsList;
    private Button newTopicButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics);

        /*
         * Get the status of isPrivate from the Dashboard activity
         */
        Intent dashboard = getIntent();
        isPrivate = dashboard.getExtras().getBoolean("isPrivate");
        user = dashboard.getExtras().getString("username");
        pass = dashboard.getExtras().getString("password");
        Log.d("TOPICS isPrivate", "" + isPrivate);
        Log.d("USERNAME:", user);
        Log.d("PASSWORD:", pass);

        /*
         * Populate the Navigation menu
         */
        leftSliderData[0] = getString(R.string.dashboardNavMenu);
        leftSliderData[1] = getString(R.string.privateTopicsNavMenu);
        leftSliderData[2] = getString(R.string.publicTopicsNavMenu);
        leftSliderData[3] = getString(R.string.postNewTopicNavMenu);

        /*
         * Build the Navigation menu based on isPrivate status
         */
        nitView();
        if (toolbar != null) {
            if(isPrivate)
                toolbar.setTitle(getString(R.string.toolbarPrivateTopicsTitle));
            else
                toolbar.setTitle(getString(R.string.toolbarPublicTopicsTitle));
            setSupportActionBar(toolbar);
        }
        initDrawer();

        newTopicButton = (Button)findViewById(R.id.newTopicButton);
        newTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newTopic = new Intent(getBaseContext(), PostTopicActivity.class);
                newTopic.putExtra("isPrivate", isPrivate);
                newTopic.putExtra("username", user);
                newTopic.putExtra("password", pass);
                startActivity(newTopic);
            }
        });

        /*
         * Allows the main thread to process internet traffic, rather than providing the connection
         * a thread of its own.
         *
         * Separate threading for internet traffic will be implemented later.
         */
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /*
         * Obtains a list of active networks on the device.
         */
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        /*
         * Checks to see if there there are active networks on the device and if they are connected.
         * If there are connected networks a list of public topics is generated and displayed in
         * a ListView.
         */
        if (networkInfo != null && networkInfo.isConnected()) {

            /*
             * Generates Strings needed for generating and using an SNS Controller then initializes
             * one. Verifies the provided username and obtains its corresponding "u-[id]".
             */
            String endPoint = "http://tse.topicplaces.com/api/2/";
            SNSController topicsController = new SNSController(endPoint);
            String verifiedUserID = topicsController.verifyUsername(user);

            /*
             * Gets a Map of topics then generates a String[] to store all returned
             * keys (topic titles).
             */
            Map<String, String> topicMap;
            if(isPrivate)
                topicMap = topicsController.getPrivateTopicMap(verifiedUserID);
            else
                topicMap = topicsController.getPublicTopicMap(verifiedUserID);
            String[] topicKeys = (String[])topicMap.keySet().toArray(new String[topicMap.size()]);

            /*
             * Generates and populates the list view with topics
             */
            topicsList = (ListView) findViewById(R.id.topicsList);
            ArrayAdapter<String> topicsAdapter =
                    new ArrayAdapter<>(this, R.layout.topics_listview, R.id.topic_title, topicKeys);
            topicsList.setAdapter(topicsAdapter);
            topicsList.setOnItemClickListener(new TopicsListListener(topicKeys, topicMap));
        }

    }

    private void nitView() {
        navMenuDrawerList = (ListView) findViewById(R.id.nav_menu_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationDrawerAdapter = new ArrayAdapter<>(TopicsActivity.this,
                R.layout.nav_drawer_choices, leftSliderData);
        navMenuDrawerList.setAdapter(navigationDrawerAdapter);
        navMenuDrawerList.setOnItemClickListener(new NavMenuListener());
    }

    private void initDrawer() {

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                                 R.string.drawer_open, R.string.drawer_close) {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.topics_settings) {
            return true;
        }
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        }
    }

    /**
     * Accepts a String[] of topic map keys and a tree map then passes the t-[id] of the selected
     * topic to the Public Messages List activity
     */
    private class TopicsListListener implements AdapterView.OnItemClickListener {

        /*
         * Fields for the keys and topic tree map
         */
        private final String[] keys;
        private final Map topicTree;

        /*
         * Constructor accepts key array and topic tree map and assigns them to respective fields
         */
        TopicsListListener(String[] keys, Map topicTree) {
            super();
            this.topicTree = topicTree;
            this.keys = keys;
        }

        /*
         * Passes an Intent to the Public Messages List activity containing the t-[id] of the
         * selected topic and an isPrivate value of false (Public)
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent topicMessages = new Intent(getBaseContext(), MessagesActivity.class);
            String TID = (String) topicTree.get(keys[position]);
            topicMessages.putExtra("TID", TID);
            topicMessages.putExtra("isPrivate", isPrivate);
            topicMessages.putExtra("username", user);
            topicMessages.putExtra("password", pass);
            startActivity(topicMessages);

        }
    }
}
