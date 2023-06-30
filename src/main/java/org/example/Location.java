package org.example;

import java.io.Serializable;

public class Location implements Serializable {
    public double latitude;
    public double longitude;

    public Location() {
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public double distance(Location other) {
        int R = 6371;

        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}