package me.nathan3882.howfast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;
import me.nathan3882.howfast.activities.StartAcitvity;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class AverageSpeedTask extends TimerTask implements IActivityReferencer<Activity> {


    private static DecimalFormat format;
    static {
        format = new DecimalFormat("##.##");
    }
    public static final int REQUEST_PERMISSION_ID = 1001;
    private static AverageSpeedTask singleton = null;

    private final Timer timer;
    private final Context context;
    private final StartAcitvity activity;
    private final TextView currentSpeedTextView;

    private WeakReference<Activity> weakReference;
    private Location previouslyKnownLocation = null;
    private long previousHeartbeatMillis = -1;

    private AverageSpeedTask(IActivityReferencer<Activity> iActivityReferencer, Timer timer) {
        this.weakReference = iActivityReferencer.getWeakReference();
        this.activity = (StartAcitvity) getReferenceValue();
        this.context = getActivity().getApplicationContext();
        this.currentSpeedTextView = getActivity().findViewById(R.id.current_average_speed);
        this.timer = timer;
    }

    public static AverageSpeedTask newInstance(IActivityReferencer<Activity> iActivityReferencer, Timer timer) {
        if (getInstance() == null) {
            singleton = new AverageSpeedTask(iActivityReferencer, timer);
        }
        return getInstance();
    }

    public void start() {
        timer.scheduleAtFixedRate(this, 10_000, 10_000); //every 10 seconds, will call Runnable#run()
    }

    public static DecimalFormat getFormat() {
        return format;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        String permissionFineLocation = Manifest.permission.ACCESS_FINE_LOCATION;

        String permissionCoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;

        int granted = PackageManager.PERMISSION_GRANTED;
        if (getPermissionCode(permissionFineLocation) != granted &&
                getPermissionCode(permissionCoarseLocation) != granted) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{permissionCoarseLocation, permissionFineLocation},
                    AverageSpeedTask.REQUEST_PERMISSION_ID);
            cancel(); //Cancel, when user has given all perms, will call Runnable#run again
        } else {
            //permissions granted

            if (lm != null) {
                long currentMillis = System.currentTimeMillis();
                Location currentLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                Location previouslyKnownLocation = getPreviouslyKnownLocation();
                long previousHeartbeatMillis = getPreviousHeartbeatMillis();

                if (previouslyKnownLocation != null && previousHeartbeatMillis != -1) {

                    double meterDifference = getDistanceBetween(currentLocation, previouslyKnownLocation);

                    long elapsedMillis = currentMillis - previousHeartbeatMillis;
                    long elapsedSeconds = elapsedMillis / 1000;
                    float elapsedMinutes = (float) elapsedSeconds / 60;
                    float elapsedHours = elapsedMinutes / 60;

                    SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                    //if has data about useMph and the user doesn't want to use mph execute condition = true else false
                    boolean useMph = !settings.contains("useMph") || settings.getBoolean("useMph", false);
                    if (useMph) {
                        float mileDifference = (float) (meterDifference * 0.00062137);
                        float mphSpeed = (mileDifference / elapsedHours);
                        getCurrentSpeedTextView().setText(getFormat().format(mphSpeed) + "mph");
                    }else {
                        float kilometerDifference = (float) (meterDifference / 1000);
                        float kmphSpeed  = (kilometerDifference / elapsedHours);
                        getCurrentSpeedTextView().setText(getFormat().format(kmphSpeed) + "km/h");

                    }
                }

                setPreviouslyKnownLocation(currentLocation);
                setPreviousHeartbeatMillis(currentMillis);
            }
        }

    }

    @Override
    public WeakReference<Activity> getWeakReference() {
        return weakReference;
    }

    private double getDistanceBetween(Location a, Location b) {
        return a.distanceTo(b);
    }

    private int getPermissionCode(String permission) {
        return ActivityCompat.checkSelfPermission(getContext(), permission);
    }

    public long getPreviousHeartbeatMillis() {
        return previousHeartbeatMillis;
    }

    public void setPreviousHeartbeatMillis(long previousHeartbeatMillis) {
        this.previousHeartbeatMillis = previousHeartbeatMillis;
    }

    private Location getPreviouslyKnownLocation() {
        return previouslyKnownLocation;
    }

    public void setPreviouslyKnownLocation(Location previouslyKnownLocation) {
        this.previouslyKnownLocation = previouslyKnownLocation;
    }

    @Nullable
    public static AverageSpeedTask getInstance() {
        return singleton;
    }

    public StartAcitvity getActivity() {
        return this.activity;
    }

    public Timer getTimer() {
        return timer;
    }

    public Context getContext() {
        return context;
    }

    public TextView getCurrentSpeedTextView() {
        return currentSpeedTextView;
    }
}
