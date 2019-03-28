package me.nathan3882.howfast;

public enum SpeedUnit {

    KMPH("km/h"),
    MPH("mph"),
    KNOTS("knots"),
    FPS("fps");

    private String prettyRepresentation;

    SpeedUnit(String prettyRepresentation) {
        this.prettyRepresentation = prettyRepresentation;
    }

    public static SpeedUnit getFromPrettyRepresentation(String toString) {
        for (SpeedUnit value : values()) {
            if (value.getPrettyRepresentation().equalsIgnoreCase(toString)) return value;
        }
        return MPH;
    }

    public String getPrettyRepresentation() {
        return this.prettyRepresentation;
    }
}