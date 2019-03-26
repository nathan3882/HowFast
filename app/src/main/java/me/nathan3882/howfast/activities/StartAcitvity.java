package me.nathan3882.howfast.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.Timer;

public class StartAcitvity extends AppCompatActivity implements IActivityReferencer<StartAcitvity> {

    private static final int REQUEST_PERMISSION_ID = 1001;
    private static final String PREFERENCES_LOCATION = "me.nathan3882.howfast.unitprefs";
    private static final String UNIT_KEY = "speedUnitKey";
    private static DecimalFormat format;

    static {
        format = new DecimalFormat("##.##");
    }

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

    private Location previouslyGottenLocation;
    private long previouslyGotAtMillis;
    private long previouslyCalledMillis;

    private SharedPreferences preferences;
    private SpeedUnit preferredUnit;

    private AlertDialog.Builder querySpeedUnitDialog;

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

        if (Double.valueOf(unitValue).isNaN() || unitValue < 1.0) {
            System.out.println("speed = " + unitValue);
            return "< 1" + toAppend + " - walk about";
        } else {
            return getFormat().format(unitValue) + toAppend;
        }
    }

    public void getCurrentLocation(LocationChangedEvent event) {
        StartAcitvity activity = getReferenceValue();

        LocationManager lm = (LocationManager) activity.getSystemService(LOCATION_SERVICE);

        long callingCurrentMillis = System.currentTimeMillis();
        this.previouslyCalledMillis = callingCurrentMillis;

        long elapsedSinceLastCall = callingCurrentMillis - getPreviouslyCalledMillis();
        long elapsedSinceLastFetched = callingCurrentMillis - getPreviouslyGotAtMillis();

        boolean hasntExecutedThisBefore = getPreviouslyGottenLocation() == null;

        if (lm != null) {
            long leewayMillis = 3500;
            if (elapsedSinceLastFetched <= leewayMillis && !hasntExecutedThisBefore) {
                //Have executed and less than 3500 ago, and gotten an answer less than 3500 ago return prev answer
                event.onLocationChange(getPreviouslyGottenLocation());
                System.out.println("pressed below 3500 and has executed before");
            } else {
                //if more than leewayMillis millis have elapsed since the last time called, fetch new real value
                activity.runOnUiThread(new Runnable() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                        lm.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                        event.onLocationChange(location);
                                        StartAcitvity.this.previouslyGottenLocation = location;
                                        StartAcitvity.this.previouslyGotAtMillis = System.currentTimeMillis();
                                        lm.removeUpdates(this);
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

        getHelpButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPickUnitDialog(getPreferredUnit());
            }
        });

        this.averageSpeedTask = AverageSpeedTask.newInstance(this, new Timer());

        getAverageSpeedTask().start();

        this.querySpeedUnitDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Pick a unit");

        this.preferences = getSharedPreferences(PREFERENCES_LOCATION, MODE_PRIVATE);
        if (getPreferences().contains(UNIT_KEY)) {
            updatePreferredUnit(SpeedUnit.valueOf(getPreferences().getString(UNIT_KEY, SpeedUnit.MPH.name())));
        } else {
            showPickUnitDialog(null);
        }

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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AverageSpeedTask averageSpeedTask = AverageSpeedTask.getInstance();
                if (averageSpeedTask != null) {
                    averageSpeedTask.start();
                    doUiToast("You can now use the app as intended.");
                }

            } else {
                doUiToast("Permission denied -");
            }
        }
    }

    @Override
    public WeakReference<StartAcitvity> getWeakReference() {
        return this.weakReference;
    }

    private void doUiToast(String s) {
        runOnUiThread(() -> Toast.makeText(getReferenceValue(), s, Toast.LENGTH_SHORT).show());
    }

    private void showPickUnitDialog(SpeedUnit alreadySelectedUnit) {
        SpeedUnit[] speedUnitValues = SpeedUnit.values();
        int length = speedUnitValues.length;
        CharSequence[] speedUnits = (CharSequence[]) Array.newInstance(CharSequence.class, length);
        for (int i = 0; i < length; i++) {
            Array.set(speedUnits, i, Util.upperFirst(speedUnitValues[i].name()));
        }
        querySpeedUnitDialog.setItems(speedUnits, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SpeedUnit clicked = SpeedUnit.valueOf(speedUnits[which].toString().toUpperCase());
                if (alreadySelectedUnit != clicked) {
                    getReferenceValue().updatePreferredUnit(clicked);
                    dialog.dismiss();
                    dialog.cancel();
                }
            }
        });


        querySpeedUnitDialog.show();
    }

    private void updatePreferredUnit(SpeedUnit clicked) {
        preferences.edit().putString(UNIT_KEY, clicked.name()).apply();
        this.preferredUnit = clicked;
        System.out.println("set to " + clicked);
    }

    private void fillTextViews() {
        getCurrentAverageSpeed().setText("unknown...");
        getHelpCaption().setText(Util.html("Want to show a different speed unit?<br>Why not click the fella on the left."));
        getAlternativelyDesc().setText(Util.html("<html><center>Alternatively, find out how fast<br>you're going between two points</center></html>"));
    }

    private SharedPreferences getPreferences() {
        return preferences;
    }

    private long getPreviouslyCalledMillis() {
        return previouslyCalledMillis;
    }

    private long getPreviouslyGotAtMillis() {
        return previouslyGotAtMillis;
    }

    private Location getPreviouslyGottenLocation() {
        return previouslyGottenLocation;
    }

    public SpeedUnit getPreferredUnit() {
        SpeedUnit preferredUnit = this.preferredUnit;
        SpeedUnit preferencedUnit = SpeedUnit.valueOf(getPreferences().getString(UNIT_KEY, preferredUnit.name()));
        if (preferredUnit != preferencedUnit) {
            updatePreferredUnit(preferencedUnit);
        }
        return preferredUnit;
    }

    private static DecimalFormat getFormat() {
        return StartAcitvity.format;
    }

    private ButtonHandler getSetStartButtonHandler() {
        return setStartButtonHandler;
    }

    private ButtonHandler getSetEndButtonHandler() {
        return setEndButtonHandler;
    }

    private AverageSpeedTask getAverageSpeedTask() {
        return averageSpeedTask;
    }

    private ImageView getHelpButton() {
        return helpButton;
    }

    private TextView getHelpCaption() {
        return helpCaption;
    }

    private TextView getCurrentAverageSpeed() {
        return currentAverageSpeed;
    }

    private TextView getAlternativelyDesc() {
        return alternativelyDesc;
    }
}
