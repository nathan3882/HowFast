package me.nathan3882.howfast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;
import android.widget.TextView;
import me.nathan3882.howfast.activities.StartAcitvity;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class AverageSpeedTask extends TimerTask implements IActivityReferencer<StartAcitvity> {


    private static AverageSpeedTask singleton = null;
    private final Timer timer;

    private final StartAcitvity activity;
    private WeakReference<StartAcitvity> weakReference;

    private final Context context;

    private final TextView currentSpeedTextView;

    private Location previouslyKnownLocation = null;
    private long previousHeartbeatMillis = -1;

    private AverageSpeedTask(IActivityReferencer<StartAcitvity> iActivityReferencer, Timer timer) {
        this.weakReference = iActivityReferencer.getWeakReference();
        if (getReferenceValue() != null) {
            this.activity = getReferenceValue();
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
        timer.scheduleAtFixedRate(this, 1_000, 8500); //every 10 seconds, will call Runnable#run()
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        StartAcitvity referenceValue = getReferenceValue();
        if (referenceValue == null || !referenceValue.hasLocationPermissions(true)) {
            cancel(); //Cancel, when user has given all perms, will call Runnable#run again
        } else {
            //permissions granted
            long currentMillis = System.currentTimeMillis();
            referenceValue.getCurrentLocation(newLocation -> {

                if (newLocation != null) {
                    Location previouslyKnownLocation = getPreviouslyKnownLocation();
                    long previousHeartbeatMillis = getPreviousHeartbeatMillis();

                    if (previouslyKnownLocation != null && previousHeartbeatMillis != -1) {
                        double meterDifference = getDistanceBetween(newLocation, previouslyKnownLocation);

                        updateCurrentSpeedTextView(
                                StartAcitvity.getUnitString(currentMillis, previousHeartbeatMillis, meterDifference, referenceValue.getPreferredUnit()));
                    } else {
                        updateCurrentSpeedTextView("unknown...");
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
