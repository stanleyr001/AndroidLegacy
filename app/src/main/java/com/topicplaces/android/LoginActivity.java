package com.topicplaces.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private String user, pass;
    private EditText username;
    private EditText password;
    private Button loginButton;
    private CheckBox rememberMe;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            username = (EditText)findViewById(R.id.usernameEditText);
            password = (EditText)findViewById(R.id.passwordEditText);
            rememberMe = (CheckBox)findViewById(R.id.rememberMe);

            loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            loginPrefsEditor = loginPreferences.edit();

            saveLogin = loginPreferences.getBoolean("saveLogin", false);
            if (saveLogin == true) {
                username.setText(loginPreferences.getString("username", ""));
                password.setText(loginPreferences.getString("password", ""));
                rememberMe.setChecked(true);

                user = username.getText().toString();
                pass = password.getText().toString();
                String endPoint = "http://tse.topicplaces.com/api/2/";
                SNSController loginController = new SNSController(endPoint);

                ConnectivityManager cm =
                        (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {

                    String messageResID;
                    String authKey = loginController.acquireKey(user, pass);

                    if(authKey == null) {
                        messageResID = "Invalid Username/Password";
                        Toast.makeText(getBaseContext(), messageResID, Toast.LENGTH_LONG).show();
                    } else {
                        launchDashboard();
                    }
                }
            }

            loginButton = (Button)findViewById(R.id.loginButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String inputUsername = username.getText().toString();
                    String inputPassword = password.getText().toString();
                    String endPoint = "http://tse.topicplaces.com/api/2/";
                    SNSController loginController = new SNSController(endPoint);

                    ConnectivityManager cm =
                            (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                    if (networkInfo != null && networkInfo.isConnected()) {

                        String messageResID;
                        String authKey = loginController.acquireKey(inputUsername, inputPassword);

                        if(authKey == null) {
                            messageResID = "Invalid Username/Password";
                            Toast.makeText(getBaseContext(), messageResID, Toast.LENGTH_LONG).show();
                        } else {
                            if (rememberMe.isChecked()) {
                                user = username.getText().toString();
                                pass = password.getText().toString();
                                loginPrefsEditor.putBoolean("saveLogin", true);
                                loginPrefsEditor.putString("username", user);
                                loginPrefsEditor.putString("password", pass);
                                loginPrefsEditor.commit();
                            } else {
                                user = username.getText().toString();
                                pass = password.getText().toString();
                                loginPrefsEditor.clear();
                                loginPrefsEditor.commit();
                            }

                            launchDashboard();
                        }
                    }
                }
            });
        }
    }

    public void launchDashboard() {

        Intent dashboard = new Intent(getBaseContext(), DashboardActivity.class);
        dashboard.putExtra("username", user);
        dashboard.putExtra("password", pass);
        startActivity(dashboard);
        LoginActivity.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
