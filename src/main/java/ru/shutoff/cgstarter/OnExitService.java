package ru.shutoff.cgstarter;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.util.List;

public class OnExitService extends Service {

    static final int TIMEOUT = 3000;
    static final String START = "Start";

    AlarmManager alarmMgr;
    PendingIntent pi;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, OnExitService.class);
        pi = PendingIntent.getService(this, 0, intent, 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ((action != null) && action.equals(START)) {
                alarmMgr.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + TIMEOUT, TIMEOUT, pi);
                return START_STICKY;
            }
        }
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        int i;
        for (i = 0; i < procInfos.size(); i++) {
            if (procInfos.get(i).processName.equals("cityguide.probki.net"))
                break;
        }
        if (i >= procInfos.size()) {
            alarmMgr.cancel(pi);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor ed = preferences.edit();
            int rotate = preferences.getInt("save_rotate", 0);
            if (rotate > 0) {
                try {
                    Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, rotate);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ed.remove("save_rotate");
            }
            boolean bt = preferences.getBoolean("save_bt", false);
            if (bt) {
                try {
                    BluetoothAdapter.getDefaultAdapter().disable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ed.remove("save_bt");
            }
            int channel = preferences.getInt("save_channel", 0);
            if (channel > 0) {
                int level = preferences.getInt("save_level", 0);
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.setStreamVolume(channel, level, 0);
                ed.remove("save_channel");
            }
            ed.commit();
            stopSelf();
        }

        return START_STICKY;
    }
}
