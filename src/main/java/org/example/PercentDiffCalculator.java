package org.example;

import java.util.HashMap;
import java.util.Map;

public class PercentDiffCalculator {


    public  static double calculatePercentDiff(double value, double average) {
        return ((value - average) / average) * 100.0;
    }


    public static Map<String, Double> calculatePercentDiffs(Results result, Results averages) {
        Map<String, Double> percentDiffs = new HashMap<>();

        percentDiffs.put("% distance", calculatePercentDiff(result.getTotalDistance(), averages.getTotalDistance()));
        percentDiffs.put("% elevation", calculatePercentDiff(result.getTotalElevationGain(), averages.getTotalElevationGain()));
        percentDiffs.put("% time", calculatePercentDiff(result.getTotalTime(), averages.getTotalTime()));
        percentDiffs.put("% speed", calculatePercentDiff(result.getAverageSpeed(), averages.getAverageSpeed()));

        return percentDiffs;
    }
}

