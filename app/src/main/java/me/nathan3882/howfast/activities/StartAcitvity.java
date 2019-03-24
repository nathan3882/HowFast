package me.nathan3882.howfast.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import me.nathan3882.howfast.AverageSpeedTask;
import me.nathan3882.howfast.IActivityReferencer;
import me.nathan3882.howfast.R;
import me.nathan3882.howfast.Util;

import java.lang.ref.WeakReference;
import java.util.Timer;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class StartAcitvity extends AppCompatActivity implements IActivityReferencer<Activity> {

    private RelativeLayout relativeLayout;

    private ImageView helpButton;
    private ImageView middleDivider;

    private TextView helpCaption;
    private TextView welcomeText;
    private TextView currentAverageSpeedCaption;
    private TextView currentAverageSpeed;
    private TextView alternativelyDesc;

    private AverageSpeedTask averageSpeedTask;

    private WeakReference<Activity> weakReference;

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

        fillTextViews();

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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AverageSpeedTask.REQUEST_PERMISSION_ID: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (AverageSpeedTask.getInstance() != null) {
                        AverageSpeedTask.getInstance().start();
                        Toast.makeText(getReferenceValue(), "You can now use the app as intented.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getReferenceValue(), "Permission denied -", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public WeakReference<Activity> getWeakReference() {
        return this.weakReference;
    }

    private void startHelpActivity() {
        Intent intent = new Intent();
    }

    private void fillTextViews() {
        getHelpCaption().setText(Util.html("Need help?<br>Why not click the fella on the left."));
        getAlternativelyDesc().setText(Util.html("Alternatively, find out how fast<br>you're going between two points"));
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
