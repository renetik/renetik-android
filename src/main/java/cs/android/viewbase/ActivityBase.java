package cs.android.viewbase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import cs.java.lang.Value;

import static cs.java.lang.Lang.*;

public abstract class ActivityBase extends AppCompatActivity implements CSActivity {

    private ActivityManager _manager;
    private CSViewController _controller;

    public Activity activity() {
        return this;
    }

    public Context context() {
        return this;
    }

    public CSViewController controller() {
        return _controller;
    }

    public MenuInflater getSupportMenuInflater() {
        return getMenuInflater();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        _controller.onActivityResult(new ActivityResult(requestCode, resultCode, data));
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        OnKeyDownResult onKeyDown = new OnKeyDownResult(keyCode, event);
        _controller.onKeyDown(onKeyDown);
        return super.onKeyDown(keyCode, event);
    }

    public void onLowMemory() {
        _controller.onLowMemory();
        super.onLowMemory();
    }

    protected void onPause() {
        _controller.onPauseNative();
        super.onPause();
    }

    protected void onNewIntent(Intent intent) {
        _controller.onNewIntent(intent);
        super.onNewIntent(intent);
    }

    protected void onResume() {
        _controller.onResumeNative();
        super.onResume();
    }

    public void onSaveInstanceState(Bundle state) {
        _controller.onSaveInstanceState(state);
    }

    protected void onStart() {
        _controller.onStart();
        super.onStart();
    }

    public void onCreate(Bundle state) {
        super.onCreate(state);
        _controller = activityManager().create();
        activityManager().onCreate(state);
    }

    private ActivityManager activityManager() {
        if (no(_manager)) _manager = createActivityManager();
        return _manager;
    }

    protected ActivityManager createActivityManager() {
        return new ActivityManager(this);
    }

    protected void onStop() {
        _controller.onStop();
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        activityManager().onDestroy();
        _controller = null;
        System.gc();
    }

    public void onBackPressed() {
        Value<Boolean> goBack = new Value<>(true);
        _controller.onBackPressed(goBack);
        if (goBack.get()) super.onBackPressed();
    }

    protected void onUserLeaveHint() {
        _controller.onUserLeaveHint();
        super.onUserLeaveHint();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        OnMenu onMenu = new OnMenu(activity(), menu);
        _controller.onCreateOptionsMenu(onMenu);
        return onMenu.showMenu();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        OnMenu onMenu = new OnMenu(activity(), menu);
        onMenu.showMenu(YES);
        _controller.onPrepareOptionsMenuImpl(onMenu);
        return onMenu.showMenu();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _controller.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        OnMenuItem onMenuItem = new OnMenuItem(item);
        _controller.onOptionsItemSelectedImpl(onMenuItem);
        return onMenuItem.consumed();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        _controller.onRequestPermissionsResult(new RequestPermissionResult(requestCode, permissions, grantResults));
    }
}
