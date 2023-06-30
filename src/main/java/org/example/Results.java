package org.example;

import java.io.Serializable;

public class Results implements Serializable {
    private String name;
    private Double totalDistance;
    private Double totalElevationGain;
    private Double totalTime;
    private Double averageSpeed;

    public Results(String name, Double totalDistance, Double totalElevationGain, Double totalTime, Double averageSpeed){
        this.name = name;
        this.totalDistance = totalDistance;
        this.totalElevationGain = totalElevationGain;
        this.totalTime = totalTime;
        this.averageSpeed = averageSpeed;
    }

    @Override
    public String toString() {
        return "Results{" +
                "name='" + name + '\'' +
                ", totalDistance=" + totalDistance +
                ", totalElevationGain=" + totalElevationGain +
                ", totalTime=" + totalTime +
                ", averageSpeed=" + averageSpeed +
                '}';
    }


    public String getName() {
        return this.name;
    }

    public Double getTotalDistance() {
        return this.totalDistance;
    }

    public Double getTotalElevationGain() {
        return this.totalElevationGain;
    }

    public Double getTotalTime() {
        return this.totalTime;
    }

    public Double getAverageSpeed() {
        return this.averageSpeed;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setTotalDistance(final Double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public void setTotalElevationGain(final Double totalElevationGain) {
        this.totalElevationGain = totalElevationGain;
    }

    public void setTotalTime(final Double totalTime) {
        this.totalTime = totalTime;
    }

    public void setAverageSpeed(final Double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
}

