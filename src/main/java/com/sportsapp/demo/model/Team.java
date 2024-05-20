package com.sportsapp.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "teams")
public class Team {
    @Id
    private String id;
    private String name;
    private String coach;
    private String group;
    private String players;

    // Getters and setters
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getCoach(){
        return coach;
    }
    public void setCoach(String coach){
        this.coach = coach;
    }
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPlayers() {
        return players;
    }

    public void setPlayers(String players) {
        this.players = players;
    }

    // Default Constructor
    public Team() {
    }

    // Option argument constructor
    public Team(String name, String coach, String group, String players) {
        this.name = name;
        this.coach = coach;
        this.group = group;
        this.players = players;
    }
}