package ru.shutoff.cgstarter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;

public class State extends BroadcastReceiver {

    static final String AUTO_PAUSE = "auto_pause";
    static final String INACTIVE_PAUSE = "inactive_pause";
    static final String INACTIVE_MODE = "inactive_mode";
    static final String CAR_MODE = "carmode";
    static final String CAR_STATE = "car_state";
    static final String POWER_TIME = "powertime";
    static final String BT = "bt";
    static final String BT_DEVICES = "bt_devices";
    static final String VOLUME = "volume";
    static final String LEVEL = "level";
    static final String RING_LEVEL = "ring_level";
    static final String SAVE_RING_LEVEL = "save_ring_level";
    static final String PHONE = "phone";
    static final String DATA = "data";
    static final String SPEAKER = "speaker";
    static final String ANSWER_TIME = "answertime";
    static final String RINGING_TIME = "ringtime";
    static final String SMS = "sms";
    static final String ID = "ID";
    static final String ORIENTATION = "orientation";
    static final String SAVE_ROTATE = "save_rotate";
    static final String CALL_VOLUME = "save_call_volume";
    static final String SAVE_ORIENTATION = "save_orientation";
    static final String CUR_CHANNEL = "cur_channel";
    static final String SAVE_CHANNEL = "channel";
    static final String SAVE_LEVEL = "save_level";
    static final String MUTE_LEVEL = "mute_level";
    static final String GPS = "gps";
    static final String GPS_SAVE = "gps_save";
    static final String SAVE_WIFI = "save_wifi";
    static final String SAVE_DATA = "save_data";
    static final String START = "start";
    static final String PHONE_X = "phone_x";
    static final String PHONE_Y = "phone_y";
    static final String PHONE_LAND_X = "phone_land_x";
    static final String PHONE_LAND_Y = "phone_land_y";
    static final String PHONE_SHOW = "phone_show";
    static final String RTA_LOGS = "rta_logs";
    static final String ROUTE = "route";
    static final String KILL_CAR = "kill_car";
    static final String KILL_POWER = "kill_power";
    static final String KILL_BT = "kill_bt";
    static final String PING = "ping";
    static final String NOTIFICATION = "notification";
    static final String NOTIFICATION_IGNORE = "notification_ignore";
    static final String SHOW_SMS = "show_sms";
    static final String MAPCAM = "mapcam";
    static final String STRELKA = "strelka";
    static final String TRAFFIC = "traffic";
    static final String UPD_TIME = "upd_time";
    static final String NAME = "Name";
    static final String ORIGINAL = "Original";
    static final String LATITUDE = "Latitude";
    static final String LONGITUDE = "Longitude";
    static final String INTERVAL = "Interval";
    static final String DAYS = "Days";
    static final String POINTS = "Points";
    static final int WORKDAYS = 1;
    static final int HOLIDAYS = 2;
    static final int ALLDAYS = 3;
    static final String TITLE = "title";
    static final String INFO = "info";
    static final String TEXT = "text";
    static final String APP = "app";
    static final String ICON = "icon";
    static final String START_POINT = "start_point";
    static final String VERTICAL = "vertical";
    static final String APPS = "apps";
    static final String FULL_TIME = "full_time";
    static final String QUICK_ALPHA = "quick_alpha";
    static final String QUICK_SIZE = "quick_size";
    static final String WIFI = "wifi";
    static final String LAST_LAT = "last_lat";
    static final String LAST_LNG = "last_lng";
    static public boolean can_root;
    static Point[] points;
    static int telephony_state = 0;
    static String cg_package = null;
    static boolean cg_files = true;
    static boolean is_cg = false;
    static boolean is_cn = false;

    static String cg = "cityguide.probki.net";
    static String cn = "net.probki.geonet";
    static File cg_folder = null;

    static Point[] get(Context context, boolean force) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (force)
            points = null;
        if (points == null) {
            points = new Point[8];
            boolean is_init = false;
            for (int i = 0; i < 8; i++) {
                Point p = new Point();
                if (!force) {
                    p.name = preferences.getString(NAME + i, "");
                    p.original = preferences.getString(ORIGINAL + i, "");
                    p.lat = preferences.getString(LATITUDE + i, "");
                    p.lng = preferences.getString(LONGITUDE + i, "");
                    p.interval = preferences.getString(INTERVAL + i, "");
                    p.days = preferences.getInt(DAYS + i, 0);
                    p.points = preferences.getString(POINTS + i, "");
                    if (p.lat.equals("") || p.lng.equals("")) {
                        p.name = "";
                        p.original = "";
                        p.lat = "";
                        p.lng = "";
                        p.interval = "";
                        p.days = 0;
                        p.points = "";
                    }
                    if (!p.name.equals(""))
                        is_init = true;
                }
                points[i] = p;
            }
            if (!is_init) {
                Bookmarks.Point[] from = Bookmarks.get(context);
                for (int i = 0; i < 8; i++) {
                    if (i >= from.length)
                        break;
                    Point p = points[i];
                    p.name = from[i].name;
                    p.original = from[i].name;
                    p.lat = from[i].lat + "";
                    p.lng = from[i].lng + "";
                    p.points = from[i].points;
                }
                save(preferences);
            }
        }
        return points;
    }

    static void save(SharedPreferences preferences) {
        SharedPreferences.Editor ed = preferences.edit();
        for (int i = 0; i < 8; i++) {
            Point p = points[i];
            if (p.name.equals("")) {
                ed.remove(NAME + i);
                ed.remove(ORIGINAL + i);
                ed.remove(LATITUDE + i);
                ed.remove(LONGITUDE + i);
                ed.remove(INTERVAL + i);
                ed.remove(DAYS + i);
                ed.remove(POINTS + i);
                continue;
            }
            ed.putString(NAME + i, p.name);
            ed.putString(ORIGINAL + i, p.original);
            ed.putString(LATITUDE + i, p.lat);
            ed.putString(LONGITUDE + i, p.lng);
            ed.putString(INTERVAL + i, p.interval);
            ed.putString(POINTS + i, p.points);
            ed.putInt(DAYS + i, p.days);
        }
        ed.commit();
    }

    static boolean canToggleGPS(Context context) {
        PackageManager pacman = context.getPackageManager();
        PackageInfo pacInfo = null;

        try {
            pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);
        } catch (PackageManager.NameNotFoundException e) {
            return false; //package not found
        }

        if (pacInfo != null) {
            for (ActivityInfo actInfo : pacInfo.receivers) {
                //test if recevier is exported. if so, we can toggle GPS.
                if (actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported) {
                    return true;
                }
            }
        }
        return false; //default
    }

    static void turnGPSOn(Context context) {
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!provider.contains("gps")) { //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            context.sendBroadcast(poke);
        }
    }

    static void turnGPSOff(Context context) {
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (provider.contains("gps")) { //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            context.sendBroadcast(poke);
        }
    }

    static public void appendLog(String text) {
        Log.v("CG starter", text);
        File logFile = Environment.getExternalStorageDirectory();
        logFile = new File(logFile, "cg.log");
        if (text == null)
            text = " (null)";
        try {
            if (!logFile.exists())
                logFile.createNewFile();
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            Date d = new Date();
            buf.append(d.toLocaleString());
            buf.append(" ");
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
        }
    }

    static public void print(Throwable ex) {
        appendLog("Error: " + ex.toString());
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String s = sw.toString();
        appendLog(s);
    }

    static boolean inInterval(String interval) {
        if (interval.equals(""))
            return false;
        Calendar calendar = Calendar.getInstance();
        int now = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        String[] parts = interval.split(";");
        for (String part : parts) {
            if (inInterval(now, part))
                return true;
        }
        return false;
    }

    static boolean inInterval(int now, String interval) {
        String[] times = interval.split("-");
        String[] time = times[0].split(":");
        int start = Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
        time = times[1].split(":");
        int end = Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
        if (end > start) {
            if (now < start)
                return false;
            return (now <= end);
        }
        if (now <= end)
            return true;
        if (now >= start)
            return true;
        return false;
    }

    static boolean isDebug() {
        return Build.FINGERPRINT.startsWith("generic");
    }

    static boolean hasTelephony(Context context) {
        if (isDebug())
            return true;
        if (telephony_state == 0) {
            PackageManager pm = context.getPackageManager();
            if (!pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                telephony_state = -1;
                return false;
            }
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
                telephony_state = -1;
                return false;
            }
            telephony_state = 1;
        }
        return telephony_state > 0;
    }

    static boolean doRoot(Context context, String command, boolean show_error) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(command);
            os.writeBytes("\nexit\n");
            os.flush();
            p.waitFor();
            int ev = p.exitValue();
            if (ev == 0)
                return true;
        } catch (Exception ex) {
            if (show_error) {
                Toast toast = Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG);
                toast.show();
            }
            // State.appendLog("su error " + command + " - " + ex.toString());
            // ignore
        }
        return false;
    }

    static void init_package(Context context) {
        cg_files = true;
        cg_folder = Environment.getExternalStorageDirectory();
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(cg, 0);
            is_cg = true;
            String[] ver = info.versionName.split("\\.");
            if (Integer.parseInt(ver[0]) > 7)
                cg_files = false;
        } catch (Exception ex) {
            // ignore
        }
        try {
            pm.getPackageInfo(cn, 0);
            is_cn = true;
        } catch (Exception ex) {
            // ignore
        }
        if (!is_cn) {
            cg_package = cg;
            cg_folder = new File(cg_folder, "Android/data/cityguide.probki.net/files"); // Fix me
            context.sendBroadcast(new Intent(MainActivity.CHANGE_APP));
            return;
        }
        if (!is_cg) {
            cg_package = cn;
            cg_folder = new File(cg_folder, "GeoNet");
            cg_files = false;
            context.sendBroadcast(new Intent(MainActivity.CHANGE_APP));
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getString("cg_app", "").equals(cn)) {
            cg_package = cn;
            cg_folder = new File(cg_folder, "GeoNet");
            cg_files = false;
            context.sendBroadcast(new Intent(MainActivity.CHANGE_APP));
            return;
        }
        cg_package = cg;
        cg_folder = new File(cg_folder, "CityGuide");
        context.sendBroadcast(new Intent(MainActivity.CHANGE_APP));
    }

    static File CG_Folder(Context context) {
        if (cg_package == null)
            init_package(context);
        String folder = cg_folder.getAbsolutePath();
        return cg_folder;
    }

    static String CG_Package(Context context) {
        if (cg_package == null)
            init_package(context);
        return cg_package;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        init_package(context);
    }

    interface OnBadGPS {
        abstract void gps_message(Context context);
    }

    static class Point {
        String name;
        String original;
        String lat;
        String lng;
        String interval;
        String points;
        int days;
    }

}
