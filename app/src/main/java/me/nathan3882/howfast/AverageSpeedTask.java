package me.nathan3882.howfast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.Nullable;
import android.widget.TextView;
import me.nathan3882.howfast.activities.StartAcitvity;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class AverageSpeedTask extends TimerTask implements IActivityReferencer<StartAcitvity> {


    private static AverageSpeedTask singleton = null;

    static {
        StartAcitvity.format = new DecimalFormat("##.##");
    }

    private final Timer timer;
    private final Context context;
    private final StartAcitvity activity;
    private final TextView currentSpeedTextView;

    private WeakReference<StartAcitvity> weakReference;
    private Location previouslyKnownLocation = null;
    private long previousHeartbeatMillis = -1;

    private AverageSpeedTask(IActivityReferencer<StartAcitvity> iActivityReferencer, Timer timer) {
        this.weakReference = iActivityReferencer.getWeakReference();
        if (getReferenceValue() instanceof StartAcitvity) {
            this.activity = (StartAcitvity) getReferenceValue();
        } else {
            throw new RuntimeException("Programmatic error - AverageSpeedTask must be supplied with an activity reference of Type " + StartAcitvity.class.getName() + "!");
        }
        this.context = getActivity().getApplicationContext();
        this.currentSpeedTextView = getActivity().findViewById(R.id.current_average_speed);
        this.timer = timer;
    }

    public static AverageSpeedTask newInstance(IActivityReferencer<StartAcitvity> iActivityReferencer, Timer timer) {
        if (getInstance() == null) {
            singleton = new AverageSpeedTask(iActivityReferencer, timer);
        }
        return getInstance();
    }

    public void start() {
        timer.scheduleAtFixedRate(this, 3_000, 10_000); //every 10 seconds, will call Runnable#run()
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        System.out.println("run");

        if (!getReferenceValue().hasLocationPermissions(true)) {
            cancel(); //Cancel, when user has given all perms, will call Runnable#run again
        } else {
            //permissions granted
            long currentMillis = System.currentTimeMillis();

            getReferenceValue().forceGetCurrentLocation(newLocation -> {

                if (newLocation != null) {
                    System.out.println(newLocation.getLongitude() + " " + newLocation.getLatitude());

                    Location previouslyKnownLocation = getPreviouslyKnownLocation();
                    long previousHeartbeatMillis = getPreviousHeartbeatMillis();

                    if (previouslyKnownLocation != null && previousHeartbeatMillis != -1) {

                        double meterDifference = getDistanceBetween(newLocation, previouslyKnownLocation);
                        if (meterDifference < 1) { //less than 1 meter travelled, don't do anything - can tend to fluctuate even when standing still ~1 meter
                            updateCurrentSpeedTextView("unknown - walk around.");
                        }

                        SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                        //if has data about useMph and the user doesn't want to use mph execute condition = true else false

                        boolean useMph = !settings.contains("useMph") || settings.getBoolean("useMph", false);
                        SpeedUnit toUse = useMph ? SpeedUnit.MPH : SpeedUnit.KMPH;

                        updateCurrentSpeedTextView(
                                StartAcitvity.getUnitString(currentMillis, previousHeartbeatMillis, meterDifference, toUse));
                    } else {
                        updateCurrentSpeedTextView("unknown - wait 10s.");
                    }
                }
                setPreviouslyKnownLocation(newLocation);
                setPreviousHeartbeatMillis(currentMillis);
            });
        }

    }

    @Override
    public WeakReference<StartAcitvity> getWeakReference() {
        return weakReference;
    }

    private void updateCurrentSpeedTextView(String newString) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getCurrentSpeedTextView().setText(newString);
            }
        });
    }

    private double getDistanceBetween(Location a, Location b) {
        return a.distanceTo(b);
    }

    private long getPreviousHeartbeatMillis() {
        return previousHeartbeatMillis;
    }

    private void setPreviousHeartbeatMillis(long previousHeartbeatMillis) {
        this.previousHeartbeatMillis = previousHeartbeatMillis;
    }

    private Location getPreviouslyKnownLocation() {
        return previouslyKnownLocation;
    }

    private void setPreviouslyKnownLocation(Location previouslyKnownLocation) {
        this.previouslyKnownLocation = previouslyKnownLocation;
    }

    @Nullable
    public static AverageSpeedTask getInstance() {
        return singleton;
    }

    private StartAcitvity getActivity() {
        return this.activity;
    }

    private Context getContext() {
        return context;
    }

    private TextView getCurrentSpeedTextView() {
        return currentSpeedTextView;
    }
}
