package me.nathan3882.howfast.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;
import me.nathan3882.howfast.*;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Timer;

public class StartAcitvity extends AppCompatActivity implements IActivityReferencer<StartAcitvity> {

    public static final int REQUEST_PERMISSION_ID = 1001;
    public static DecimalFormat format;
    private RelativeLayout relativeLayout;

    private ImageView helpButton;
    private ImageView middleDivider;

    private TextView helpCaption;
    private TextView welcomeText;
    private TextView currentAverageSpeedCaption;
    private TextView currentAverageSpeed;
    private TextView alternativelyDesc;

    private AverageSpeedTask averageSpeedTask;

    private WeakReference<StartAcitvity> weakReference;

    private Button setStartButton;
    private ButtonHandler setStartButtonHandler;

    private Button setEndButton;
    private ButtonHandler setEndButtonHandler;

    private Button resetButton;

    private Location previouslyGottenLocation = null;
    private long previouslyGotAtMillis;
    private long previouslyCalledMillis;

    private static int getPermissionCode(Activity activity, String permission) {
        return ActivityCompat.checkSelfPermission(activity, permission);
    }

    public static String getUnitString(long currentMillis, long startMillis, double meterDifference, SpeedUnit unit) {
        long elapsedMillis = currentMillis - startMillis;
        long elapsedSeconds = elapsedMillis / 1000;
        double elapsedMinutes = ((double) elapsedSeconds / 60);
        double elapsedHours = (elapsedMinutes / 60);

        double unitValue = 0.0;
        String toAppend = "";
        if (unit == SpeedUnit.MPH) {
            double mileDifference = (meterDifference * 0.00062137);
            double mphSpeed = (mileDifference / elapsedHours);
            unitValue = mphSpeed;
            toAppend = "mph";
        } else if (unit == SpeedUnit.KMPH) {
            double kilometerDifference = (meterDifference / 1000);
            double kmphSpeed = (kilometerDifference / elapsedHours);
            unitValue = kmphSpeed;
            toAppend = "km/h";
        }

        System.out.println("unit val = " + unitValue);
        if (unitValue < 1.0 ) {
            return "< 1" + toAppend + " - walk about";
        } else {
            return getFormat().format(unitValue) + toAppend;
        }
    }

    public void forceGetCurrentLocation(LocationChangedEvent event) {
        StartAcitvity referenceValue = getReferenceValue();
        LocationManager lm = (LocationManager) referenceValue.getSystemService(LOCATION_SERVICE);
        long callingCurrentMillis = System.currentTimeMillis();
        this.previouslyCalledMillis = callingCurrentMillis;

        long elapsedSinceLastCall = callingCurrentMillis - getPreviouslyCalledMillis();

        long elapsedSinceLastFetched = callingCurrentMillis - getPreviouslyGotAtMillis();

        boolean hasntExecutedThisBefore = getPreviouslyGottenLocation() == null;
        if (lm != null) {
            long leewayMillis = 3500;
            if (elapsedSinceLastCall <= leewayMillis && elapsedSinceLastFetched <= leewayMillis && !hasntExecutedThisBefore) {
                //Have executed and less than 3500 ago, and gotten an answer less than 3500 ago return prev answer
                event.gottenLocation(getPreviouslyGottenLocation());
                System.out.println("pressed below 3500 and has executed before");
                return;
            } else {
                //if more than leewayMillis millis have elapsed since the last time called, fetch new real value
                referenceValue.runOnUiThread(new Runnable() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                        lm.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                        event.gottenLocation(location);
                                        lm.removeUpdates(this);
                                        StartAcitvity.this.previouslyGottenLocation = location;
                                        StartAcitvity.this.previouslyGotAtMillis = System.currentTimeMillis();
                                    }

                                    @Override
                                    public void onStatusChanged(String provider, int status, Bundle extras) {

                                    }

                                    @Override
                                    public void onProviderEnabled(String provider) {

                                    }

                                    @Override
                                    public void onProviderDisabled(String provider) {

                                    }
                                });
                    }
                });
            }
        }
    }

    public long getPreviouslyCalledMillis() {
        return previouslyCalledMillis;
    }

    public boolean hasLocationPermissions(boolean askToApprove) {
        String permissionFineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        String permissionCoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;

        int granted = PackageManager.PERMISSION_GRANTED;

        boolean hasPermission = getPermissionCode(getReferenceValue(), permissionCoarseLocation) == granted &&
                getPermissionCode(getReferenceValue(), permissionFineLocation) == granted;

        if (askToApprove && !hasPermission) {

            ActivityCompat.requestPermissions(getReferenceValue(),
                    new String[]{permissionCoarseLocation, permissionFineLocation},
                    REQUEST_PERMISSION_ID);
        }
        return hasPermission;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start_acitvity);

        this.weakReference = new WeakReference<>(this);

        this.relativeLayout = findViewById(R.id.relative_content_layout);
        this.helpButton = findViewById(R.id.help_button);
        this.helpCaption = findViewById(R.id.help_button_caption);
        this.welcomeText = findViewById(R.id.welcome_text);
        this.middleDivider = findViewById(R.id.middle);
        this.currentAverageSpeedCaption = findViewById(R.id.current_average_speed_caption);
        this.currentAverageSpeed = findViewById(R.id.current_average_speed);
        this.alternativelyDesc = findViewById(R.id.alternatively_desc);

        this.setStartButton = findViewById(R.id.set_start_button);
        this.setEndButton = findViewById(R.id.set_end_button);

        this.resetButton = findViewById(R.id.resetButton);


        this.setEndButtonHandler = new ButtonHandler(this, setEndButton, ButtonHandler.Type.FINISH)
                .withResetButton(resetButton);
        this.setStartButtonHandler = new ButtonHandler(this, setStartButton, ButtonHandler.Type.START)
                .withResetButton(resetButton);
        setEndButtonHandler.setOtherButton(setStartButtonHandler);
        setStartButtonHandler.setOtherButton(setEndButtonHandler);


        setStartButton.setOnClickListener(getSetStartButtonHandler());
        setEndButton.setOnClickListener(getSetEndButtonHandler());

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSetEndButtonHandler().getPressedState() == PressedState.PRESSED) {
                    getSetEndButtonHandler().reset();
                }
                if (getSetStartButtonHandler().getPressedState() == PressedState.PRESSED) {
                    getSetStartButtonHandler().reset();
                }
            }
        });

        fillTextViews();

        getSetStartButton().setOnClickListener(setStartButtonHandler);

        getHelpButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startHelpActivity();
            }
        });

        this.averageSpeedTask = AverageSpeedTask.newInstance(this, new Timer());

        getAverageSpeedTask().start();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    /*
    store millis of when the last request was made,
    if new request has been sent when there's less than 5 seconds to go until the next request,
    send a new request, if not - use the previously stored request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_ID) {
            System.out.println("req code = " + requestCode);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (AverageSpeedTask.getInstance() != null) {
                    AverageSpeedTask.getInstance().start();
                    runOnUiThread(() ->
                            Toast.makeText(getReferenceValue(), "You can now use the app as intended.", Toast.LENGTH_SHORT).show());
                }

            } else {
                runOnUiThread(() -> Toast.makeText(getReferenceValue(), "Permission denied -", Toast.LENGTH_SHORT).show());
            }
        }
    }

    @Override
    public WeakReference<StartAcitvity> getWeakReference() {
        return this.weakReference;
    }

    private void startHelpActivity() {
        Intent intent = new Intent();
    }

    private void fillTextViews() {
        getCurrentAverageSpeed().setText("unknown...");
        getHelpCaption().setText(Util.html("Need help?<br>Why not click the fella on the left."));
        getAlternativelyDesc().setText(Util.html("<html><center>Alternatively, find out how fast<br>you're going between two points</center></html>"));
    }

    public long getPreviouslyGotAtMillis() {
        return previouslyGotAtMillis;
    }

    public Location getPreviouslyGottenLocation() {
        return previouslyGottenLocation;
    }

    public SpeedUnit getPreferredUnit() {
        return SpeedUnit.MPH;
    }

    private static DecimalFormat getFormat() {
        return StartAcitvity.format;
    }

    public ButtonHandler getSetStartButtonHandler() {
        return setStartButtonHandler;
    }

    public ButtonHandler getSetEndButtonHandler() {
        return setEndButtonHandler;
    }

    public AverageSpeedTask getAverageSpeedTask() {
        return averageSpeedTask;
    }

    public RelativeLayout getRelativeLayout() {
        return relativeLayout;
    }

    public ImageView getHelpButton() {
        return helpButton;
    }

    public TextView getHelpCaption() {
        return helpCaption;
    }

    public TextView getWelcomeText() {
        return welcomeText;
    }

    public Button getSetStartButton() {
        return setStartButton;
    }

    public ImageView getMiddleDivider() {
        return middleDivider;
    }

    public TextView getCurrentAverageSpeedCaption() {
        return currentAverageSpeedCaption;
    }

    public TextView getCurrentAverageSpeed() {
        return currentAverageSpeed;
    }

    public TextView getAlternativelyDesc() {
        return alternativelyDesc;
    }
}
