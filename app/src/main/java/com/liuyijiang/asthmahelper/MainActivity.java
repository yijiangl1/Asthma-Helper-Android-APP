package com.liuyijiang.asthmahelper;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import com.liuyijiang.asthmahelper.fragment.MainPage;
import com.liuyijiang.asthmahelper.fragment.NavigationPage;
import com.liuyijiang.asthmahelper.fragment.SettingPage;
import com.liuyijiang.asthmahelper.service.BackGroundService;
import com.liuyijiang.asthmahelper.tool.SettingTitleView;

public class MainActivity extends Activity implements View.OnClickListener, InitialView {

    private UpdateUIBroadcastReceiver receiver;
    private SharedPreferences config;

    protected MainPage pageMain = new MainPage();
    protected NavigationPage pageNavigation = new NavigationPage();
    protected SettingPage pageSetting = new SettingPage();

    protected SettingTitleView buttonMain;
    protected SettingTitleView buttonNavigation;
    protected SettingTitleView buttonSetting;

    private int gray = Color.rgb(183, 183, 183);
    private int blue = Color.rgb(80, 105, 141);
    private static final String IS_AQI = "isAQI";
    private static final String NOTIFICATION = "Notification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        config = getSharedPreferences("config", MODE_MULTI_PROCESS);
        buttonMain = this.findViewById(R.id.button_main);
        buttonNavigation = this.findViewById(R.id.button_navigation);
        buttonSetting = this.findViewById(R.id.button_setting);

        buttonMain.setOnClickListener(this);
        buttonNavigation.setOnClickListener(this);
        buttonSetting.setOnClickListener(this);

        buttonMain.setDrawableTop(R.mipmap.map_pressed);
        buttonMain.setTextColor(blue);
        this.getFragmentManager()
                .beginTransaction()
                .add(R.id.content_container, pageMain)
                .add(R.id.content_container, pageNavigation).hide(pageNavigation)
                .add(R.id.content_container, pageSetting).hide(pageSetting)
                .commit();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.liuyijiang.asthmahelper.service.BackGroundService.state");
        receiver = new UpdateUIBroadcastReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_main:
                buttonMain.setDrawableTop(R.mipmap.map_pressed);
                buttonMain.setTextColor(blue);
                buttonNavigation.setDrawableTop(R.mipmap.compass);
                buttonNavigation.setTextColor(gray);
                buttonSetting.setDrawableTop(R.mipmap.gear);
                buttonSetting.setTextColor(gray);
                pageMain.initView();
                this.getFragmentManager()
                        .beginTransaction()
                        .show(pageMain)
                        .hide(pageNavigation)
                        .hide(pageSetting)
                        .commit();
                break;
            case R.id.button_navigation:
                buttonMain.setDrawableTop(R.mipmap.map);
                buttonMain.setTextColor(gray);
                buttonNavigation.setDrawableTop(R.mipmap.compass_pressed);
                buttonNavigation.setTextColor(blue);
                buttonSetting.setDrawableTop(R.mipmap.gear);
                buttonSetting.setTextColor(gray);
                this.getFragmentManager()
                        .beginTransaction()
                        .hide(pageMain)
                        .show(pageNavigation)
                        .hide(pageSetting)
                        .commit();
                break;
            case R.id.button_setting:
                buttonMain.setDrawableTop(R.mipmap.map);
                buttonMain.setTextColor(gray);
                buttonNavigation.setDrawableTop(R.mipmap.compass);
                buttonNavigation.setTextColor(gray);
                buttonSetting.setDrawableTop(R.mipmap.gear_pressed);
                buttonSetting.setTextColor(blue);
                this.getFragmentManager()
                        .beginTransaction()
                        .hide(pageMain)
                        .hide(pageNavigation)
                        .show(pageSetting)
                        .commit();
                break;
        }
    }

    @Override
    public void initialAllView() {
        pageMain.initView();
        pageNavigation.initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!config.getBoolean(NOTIFICATION, false)) {
            stopDownload(this, BackGroundService.class, BackGroundService.ACTION);
        }
        unregisterReceiver(receiver);
    }

    private class UpdateUIBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                boolean state = bundle.getBoolean("state");
                if (state) {
                    pageMain.initView();
                }
            }
        }
    }

    public static void stopDownload(Context context, Class<?> cls, String action) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (manager != null) {
            manager.cancel(pendingIntent);
        }
    }
}
