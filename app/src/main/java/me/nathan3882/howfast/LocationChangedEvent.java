package me.nathan3882.howfast;

import android.location.Location;

public interface LocationChangedEvent {

    void gottenLocation(Location newLocation);
}
