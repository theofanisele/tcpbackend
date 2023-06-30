package org.example;

import java.io.Serializable;
import java.util.List;

public class Task implements Serializable {
    private final String username;
    private final List<Wpt> waypoints;

    public Task(String username, List<Wpt> waypoints) {
        this.username = username;
        this.waypoints = waypoints;
    }

    public String getUsername() {
        return username;
    }

    public List<Wpt> getWaypoints() {
        return waypoints;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Username: ").append(username).append("\n");
        sb.append("Waypoints: \n");
        for(Wpt wpt : waypoints){
            sb.append(wpt.toString()).append("\n");
        }
        return sb.toString();
    }

}
