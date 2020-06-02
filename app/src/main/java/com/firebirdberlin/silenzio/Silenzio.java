package com.firebirdberlin.silenzio;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;

public class Silenzio extends AppCompatActivity {
    public static String TAG = "Silenzio";
    public static String PREFS_KEY = "preferences";
    public static final int NOTIFICATION_ID_RINGER_SERVICE = 1000;
    public static final String NOTIFICATION_CHANNEL_ID_RINGER_SERVICE = "notification channel id ringer service";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.createNotificationChannels(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SetRingerService.running) {
                    SetRingerService.stop(getApplicationContext());
                } else {
                    SetRingerService.start(getApplicationContext());
                }
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem btn = menu.findItem(R.id.action_enabled);
        final SwitchCompat switchEnabled = (SwitchCompat) btn.getActionView();

        switchEnabled.setChecked(Settings.getEnabled(this));
        switchEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Settings.setEnabled(getApplicationContext(), isChecked);
            }
        });
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
            PreferencesActivity.start(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkForPermissions();
            }
        }, 2000);
    }

    protected void checkForPermissions() {
        View parentLayout = findViewById(android.R.id.content);
        if (! hasPermission(Manifest.permission.READ_PHONE_STATE)) {

            Snackbar.make(parentLayout, "Please grant permissions", Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                            android.R.string.ok,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                                        return;
                                    }
                                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
                                }
                            }
                    ).show();
            return;
        }

        if (!mNotificationListener.isRunning) {
            Snackbar.make(parentLayout, "Please enable notification access", Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                            android.R.string.ok,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                    startActivity(intent);
                                }
                            }
                    ).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= 23 && !hasNotificationAccessPermission()) {
            Snackbar.make(parentLayout, "Please allow to enable Do not disturb", Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                            android.R.string.ok,
                            new View.OnClickListener() {
                                @Override
                                    public void onClick(View view) {
                                    Intent intent =
                                            new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

                                    startActivity(intent);
                                }
                            }
                    ).show();
            return;
        }

    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasNotificationAccessPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return true;
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)  {
             return notificationManager.isNotificationPolicyAccessGranted();
        }

        return false;
    }
}
