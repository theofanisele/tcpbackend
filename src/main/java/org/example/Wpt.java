package org.example;

import java.io.Serializable;
import java.time.Instant;
import java.time.InstantSource;
import java.util.ArrayList;
import java.util.List;

public class Wpt implements Serializable {
    private Location location;
    private Double elevation;
    private Long time;

    public Wpt(Location location, Double elevation, Long time){
        this.location = location;
        this.elevation = elevation;
        this.time = time;
    }

    public Location getLocation(){
        return this.location;
    }

    public Double getElevation() {
        return this.elevation;
    }

    public Long getTime() {
        return this.time;
    }

    @Override
    public String toString(){
        return "Location{"+location.toString()
                + "}, Elevation{" +elevation
                +"}, Time{" + time +"}";
    }
}
