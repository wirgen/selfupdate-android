package by.wirgen.selfupdateapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import by.wirgen.selfupdate.*;

public class MainActivity extends AppCompatActivity {

    private SelfUpdate selfUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        selfUpdate = new SelfUpdate(getApplicationContext(), "http://updates.wirgen.by/updates.xml", BuildConfig.APPLICATION_ID, BuildConfig.VERSION_CODE);
        selfUpdate.applicationUpdateCheck();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_check_update) {
            selfUpdate.applicationUpdateCheck();
            return true;
        }

        if (id == R.id.action_update) {
            if (isStoragePermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                selfUpdate.applicationUpdateApk();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isStoragePermissionGranted(String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        selfUpdate.applicationUpdateApk();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class SelfUpdate extends by.wirgen.selfupdate.SelfUpdate {

        SelfUpdate(Context context, String url, String name, int version) {
            super(context, url, name, version);
        }

        @Override
        protected void updateResult(boolean result) {
            if (result) {
                Toast.makeText(getApplicationContext(), "Update available", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Update is not required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
