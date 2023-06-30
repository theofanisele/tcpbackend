package org.example;

import io.jenetics.jpx.GPX;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GpxObject  implements Serializable {
    private String userName;
    private List<Wpt> waypoints;


    public GpxObject(GPX gpx) {
        this.waypoints = new ArrayList<>();
        this.userName = gpx.getCreator();
        for (int i = 0; i < gpx.getWayPoints().size(); i++) {
            Double lat = gpx.getWayPoints().get(i).getLatitude().doubleValue();
            Double lon = gpx.getWayPoints().get(i).getLongitude().doubleValue();
            Location location = new Location(lat,lon);
            Double ele = gpx.getWayPoints().get(i).getElevation().get().doubleValue();
            Long time = gpx.getWayPoints().get(i).getTime().get().getEpochSecond();
            Wpt wpt = new Wpt(location, ele, time);
            waypoints.add(wpt);
        }
        }

    public String getUserName() {
        return this.userName;
    }

    public List<Wpt> getWaypoints() {
        return this.waypoints;
    }
}
