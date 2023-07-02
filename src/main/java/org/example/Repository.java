package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Repository {
    private HashMap<String, List<Results>> repo;
    private int counter;

    public Repository(){
        this.repo =  new HashMap<>();
        this.counter = 1;
    }


    public  void addResults(Results result){
        this.counter += 1;
        if(repo.containsKey(result.getName())){
            // If so, just add to the existing list
            repo.get(result.getName()).add(result);

        } else{
            // If not, create a new list with this result and add it to the map
            List<Results> resultsList = new ArrayList<>();
            resultsList.add(result);
            repo.put(result.getName(), resultsList);
        }


        if(repo.get("average") == null){
            System.out.println("average");
            List<Results> resultsList = new ArrayList<>();
            resultsList.add(result);
            repo.put("average", resultsList);
        } else{

            double x = this.counter -1;
            Results res = repo.get("average").get(0);

            double newAverageSpeed = ((res.getAverageSpeed()*x)+ result.getAverageSpeed())/this.counter;
            newAverageSpeed = Math.round(newAverageSpeed * 100.0) / 100.0;
            res.setAverageSpeed(newAverageSpeed);

            double newTotalElevationGain = ((res.getTotalElevationGain()*x)+ result.getTotalElevationGain())/this.counter;
            newTotalElevationGain = Math.round(newTotalElevationGain * 100.0) / 100.0;
            res.setTotalElevationGain(newTotalElevationGain);

            double newTotalTime = ((res.getTotalTime()*x)+ result.getTotalTime())/this.counter;
            newTotalTime = Math.round(newTotalTime * 100.0) / 100.0;
            res.setTotalTime(newTotalTime);

            double newTotalDistance = ((res.getTotalDistance()*x)+ result.getTotalDistance())/this.counter;
            newTotalDistance = Math.round(newTotalDistance * 100.0) / 100.0;
            res.setTotalDistance(newTotalDistance);
        }
    }

    public  Results getResult(String name){
        // This will get the last result of this user
        List<Results> resultsList = repo.get(name);
        if (resultsList != null && !resultsList.isEmpty()) {
            return resultsList.get(resultsList.size() - 1);
        } else {
            return null;
        }
    }
}
