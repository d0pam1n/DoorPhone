package com.d0pam1n.doorphone.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.d0pam1n.doorphone.DoorPhoneApplication;
import com.d0pam1n.doorphone.R;
import com.d0pam1n.doorphone.utils.SettingsUtil;
import com.d0pam1n.siplib.BroadcastHandler.BroadcastEventReceiver;
import com.d0pam1n.siplib.SipCommands;

import org.pjsip.pjsua2.pjsip_status_code;

/**
 * Activity sharing basic functionality.
 * All activity should implements this one.
 *
 * @author Andreas Pfister
 */

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int REQUEST_MICROPHONE = 0;
    private static final int SETTINGS_ACTIVITY_RESULT_CODE = 0;

    protected static boolean isSipConnected = false;
    protected static String activeAccount;
    private static boolean shutdown = false;

    protected String accountIDUri;
    protected View navigationHeader;
    protected TextView accountID;
    protected TextView username;
    protected LinearLayout navHeader;
    NavigationView navigationView;

    private Toolbar mToolbar;

    private BroadcastEventReceiver sipEvents = new BroadcastEventReceiver() {
        @Override
        public void onRegistration(String accountID, pjsip_status_code registrationStateCode) {
            if(registrationStateCode == pjsip_status_code.PJSIP_SC_OK) {
                isSipConnected = true;
                setConnected(true);
            } else {
                isSipConnected = false;
                setConnected(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initNavDrawer();

        SipCommands.startService(this);

        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

        sipEvents.register(this);

        activeAccount = SettingsUtil.getConfiguredSipAccount(this).getIdUri();

        SipCommands.getRegistrationStatus(this, activeAccount);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sipEvents.unregister(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_drawer_item_settings) {
            startActivity(new Intent(this, SettingsActivity.class));

        } else if (id == R.id.nav_drawer_item_about) {
            startActivity(new Intent(this, AboutActivity.class));

        } else if (id == R.id.nav_drawer_item_close) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
            builder.setTitle(R.string.app_name);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setMessage(R.string.quit_app_text)
                    .setCancelable(false)
                    .setPositiveButton(R.string.quit_app_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DoorPhoneApplication.setQuitApp(true);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("LOGOUT", true);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.quit_app_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * Sets the header of the navdrawer to state connected.
     * Sets the complete account ID uri and the background.
     * @param connected true if connected to sip; otherwise false.
     */
    protected void setConnected(final boolean connected) {
        if (connected) {
            accountID.setText(R.string.notification_registered);
            username.setText(activeAccount);
            navHeader.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.side_nav_bar_connected));
        } else {
            accountID.setText(R.string.notification_not_registered);
            username.setText(R.string.app_name);
            navHeader.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.side_nav_bar_disconnected));
        }
    }

    /**
     * Unchecks all items of navdrawer
     */
    protected void uncheckAllMenuItems() {
        final Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            item.setChecked(false);
        }
    }

    /**
     * Selects the menu item of the navigation drawer.
     * @param id Id of the menu item.
     */
    protected void setMenuItem(int id) {
        navigationView.setCheckedItem(id);
    }

    /**
     * Initialize NavDrawer
     */
    private void initNavDrawer() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationHeader = navigationView.getHeaderView(0);

        accountID = (TextView) navigationHeader.findViewById(R.id.nav_drawer_account_id);
        username = (TextView) navigationHeader.findViewById(R.id.nav_drawer_username);
        navHeader = (LinearLayout) navigationHeader.findViewById(R.id.nav_drawer);
    }

    /**
     * Checks for required permissions.
     */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MICROPHONE);

        }
    }
}
