package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IntermediateResult implements Serializable {
    private List<Double> totalDistances;
    private List<Long> totalTimes;
    private List<Double> totalElevationGains;
    private String name;


    public IntermediateResult() {
        totalDistances = new ArrayList<>();
        totalTimes = new ArrayList<>();
        totalElevationGains = new ArrayList<>();
    }

    public void addResult(double distance, long time, double elevationGain) {
        totalDistances.add(distance);
        totalTimes.add(time);
        totalElevationGains.add(elevationGain);
    }

    public List<Double> getTotalDistances() {
        return this.totalDistances;
    }

    public List<Long> getTotalTimes() {
        return this.totalTimes;
    }

    public List<Double> getTotalElevationGains() {
        return this.totalElevationGains;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
}
