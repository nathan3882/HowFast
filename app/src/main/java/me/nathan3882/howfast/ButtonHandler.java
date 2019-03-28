package me.nathan3882.howfast;

import android.graphics.Color;
import android.location.Location;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import me.nathan3882.howfast.activities.StartAcitvity;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

public class ButtonHandler implements View.OnClickListener, IActivityReferencer<StartAcitvity> {

    private static Calendar calendar;

    static {
        calendar = Calendar.getInstance();
    }

    private final Button button;
    private final Type buttonType;
    private final WeakReference<StartAcitvity> weakReference;
    private ButtonHandler otherButton;
    private long lastClickedAt = -1;
    private Location clickedAtThisLocation;
    private PressedState pressedState = PressedState.NOT_BEEN_PRESSED;

    private Button resetButton;

    public ButtonHandler(IActivityReferencer<StartAcitvity> activityReferencer, Button button, Type buttonType) {
        this(activityReferencer, button, buttonType, null);
    }

    public ButtonHandler(IActivityReferencer<StartAcitvity> activityReferencer, Button button, Type buttonType, ButtonHandler otherButton) {
        this.otherButton = otherButton;
        this.button = button;
        this.weakReference = activityReferencer.getWeakReference();
        this.buttonType = buttonType;
    }


    public void registerClick(long currentMillis, Location currentLocation) {
        this.lastClickedAt = currentMillis;
        this.clickedAtThisLocation = currentLocation;

        ButtonHandler otherButton = getOtherButton();
        Type thisType = getButtonType();
        if (thisType == Type.START && otherButton != null) {
            //Other button is finish
            setPressedState(PressedState.PRESSED);
            update(System.currentTimeMillis(), currentLocation);
            otherButton.reset();


            doToast("Current location updated, press finish to find average speed");

        } else if (thisType == Type.FINISH && otherButton != null) {

            PressedState otherPressedState = otherButton.getPressedState();

            if (otherPressedState == PressedState.PRESSED) {

                doToast("Calculating...");

                //do calculations etc
                long startedAt = otherButton.getLastClickedAt();

                Location startLocation = otherButton.getClickedAtThisLocation();
                double distance = startLocation.distanceTo(currentLocation);

                String averageString = StartAcitvity.getUnitString(currentMillis, startedAt, distance, getReferenceValue().getPreferredUnit());

                doToast("Avg over last " + getReferenceValue().getFormat().format(distance) + "m was " + averageString);

                otherButton.reset();
                reset();
            }
        }
    }

    public final void reset() {
        setPressedState(PressedState.NOT_BEEN_PRESSED);
        setClickedAtThisLocation(null);
        setLastClickedAt(-1);
    }

    public ButtonHandler withResetButton(Button resetButton) {
        this.resetButton = resetButton;
        return this;
    }

    @Override
    public void onClick(View v) {
        StartAcitvity referenceValue = getReferenceValue();

        if (!referenceValue.hasLocationPermissions(true)) {
            doToast("Please accept permission");
            return;
        }

        referenceValue.getCurrentLocation(new LocationChangedEvent() {
            @Override
            public void onLocationChange(Location newLocation) {
                registerClick(System.currentTimeMillis(), newLocation);
            }
        });
    }

    @Override
    public WeakReference<StartAcitvity> getWeakReference() {
        return this.weakReference;
    }

    private void doToast(String s) {
        Toast.makeText(getReferenceValue(), s, Toast.LENGTH_LONG).show();
    }

    private void update(long lastClickedAt, Location currentLocation) {
        setLastClickedAt(lastClickedAt);
        setClickedAtThisLocation(currentLocation);
    }

    public PressedState getPressedState() {
        return pressedState;
    }

    public void setPressedState(PressedState pressedState) {
        this.pressedState = pressedState;

        Button button = getButton();
        Date date = new Date(getLastClickedAt());
        calendar.setTime(date);
        if (pressedState == PressedState.PRESSED) {
            button.setBackgroundColor(Color.GREEN);
            if (getButtonType() == Type.START) {
                button.setText("Pressed at " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + " + " + calendar.get(Calendar.SECOND) + "s");
                otherButton.getButton().setVisibility(View.VISIBLE);
            }
        } else if (pressedState == PressedState.NOT_BEEN_PRESSED) {
            button.setBackgroundColor(Color.RED);
            if (getButtonType() == Type.START) {
                button.setText(getReferenceValue().getString(R.string.set_start_button));
            } else if (getButtonType() == Type.FINISH) {
                if (getOtherButton().getPressedState() == PressedState.PRESSED) {
                    button.setText(getReferenceValue().getString(R.string.get_speed));
                } else {
                    button.setVisibility(View.GONE);
                }
            }
        }
    }

    @Nullable //null if Type == RESET
    public ButtonHandler getOtherButton() {
        return otherButton;
    }

    public void setOtherButton(ButtonHandler otherButton) {
        this.otherButton = otherButton;
        otherButton.reset();
    }

    public Type getButtonType() {
        return buttonType;
    }

    public long getLastClickedAt() {
        return lastClickedAt;
    }

    public void setLastClickedAt(long lastClickedAt) {
        this.lastClickedAt = lastClickedAt;
    }

    public Location getClickedAtThisLocation() {
        return clickedAtThisLocation;
    }

    public void setClickedAtThisLocation(Location clickedAtThisLocation) {
        this.clickedAtThisLocation = clickedAtThisLocation;
    }

    public Button getButton() {
        return button;
    }

    public enum Type {
        START,
        FINISH
    }


}
