package org.example;

import java.util.HashMap;

public class Repository {
    private  HashMap<String,Results> repo;
    private int totalUsers;

    public Repository(){
        this.repo =  new HashMap<>();
        this.totalUsers = 0;
    }


    public void addResults(Results result){
        repo.put(result.getName(),result);
        this.totalUsers += 1;
        if(repo.get("average") == null){
            repo.put("average",result);
        } else{
            double x = this.totalUsers -1;
            Results res = repo.get("average");
            res.setAverageSpeed(((res.getAverageSpeed()*x)+ result.getAverageSpeed())/this.totalUsers);
            res.setTotalElevationGain(((res.getTotalElevationGain()*x)+ result.getTotalElevationGain())/this.totalUsers);
            res.setTotalTime(((res.getTotalTime()*x)+ result.getTotalTime())/this.totalUsers);
            res.setTotalDistance(((res.getTotalDistance()*x)+ result.getTotalDistance())/this.totalUsers);
        }
    }

    public Results getResult(String name){
        return repo.get(name);
    }
}
