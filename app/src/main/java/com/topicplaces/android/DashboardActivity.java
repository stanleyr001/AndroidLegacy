package com.topicplaces.android;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

public class DashboardActivity extends AppCompatActivity {

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
    private String user, pass;
    private GridView dashboardGridView;
    private Button dashboardOptions;

    /*
     * Fields for Intents
     */
    public static final String EXTRA_MESSAGE = "com.topicplaces.android.dashboardactivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        /*
         * Grab username and password from LoginActivity intent
         */

        Intent login = getIntent();
        user = login.getExtras().getString("username");
        pass = login.getExtras().getString("password");
        Log.d("USERNAME:", user);
        Log.d("PASSWORD", pass);

        leftSliderData[0] = getString(R.string.dashboardNavMenu);
        leftSliderData[1] = getString(R.string.privateTopicsNavMenu);
        leftSliderData[2] = getString(R.string.publicTopicsNavMenu);
        leftSliderData[3] = getString(R.string.postNewTopicNavMenu);

        nitView();
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.toolbarDashboardTitle));
            setSupportActionBar(toolbar);
        }
        initDrawer();

        /*
         * Sample GridView adapter
         */
        String[] titles = new String[40];
        for(int i = 0; i < 40; i++)
            titles[i] = "Topic Title";
        int[] images = new int[40];
        for(int i = 0; i < 40; i++)
            images[i] = R.drawable.dashboard_filler;
        DashboardAdapter adapter = new DashboardAdapter(DashboardActivity.this, titles, images);
        dashboardGridView = (GridView)findViewById(R.id.grid);
        dashboardGridView.setAdapter(adapter);


    }

    private void nitView() {
        navMenuDrawerList = (ListView) findViewById(R.id.nav_menu_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationDrawerAdapter = new ArrayAdapter<>(DashboardActivity.this,
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
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

    private class NavMenuListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent topicsIntent = new Intent(getBaseContext(), TopicsActivity.class);
            Intent postTopicIntent = new Intent(getBaseContext(), PostTopicActivity.class);
            switch (position) {
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
}
