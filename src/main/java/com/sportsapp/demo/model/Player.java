package com.sportsapp.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "players")
public class Player{
    @Id
    private String id;
    private String name;
    private String position;
    private int jerseyNumber;

    @DBRef
    private Team team;

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
    public String getPosition(){
        return position;
    }
    public void setPosition(String position){
        this.position = position;
    }
    public int getJerseyNumber(){
        return jerseyNumber;
    }
    public void setJerseyNumber(int jerseyNumber){
        this.jerseyNumber = jerseyNumber;
    }
    public Team getTeam(){
        return team;
    }
    public void setTeam(Team team){
        this.team = team;
    }

    // Default Constructor
    public Player() {
    }

    // Option argument constructor
    public Player(String name, String position, int jerseyNumber, Team team) {
        this.name = name;
        this.position = position;
        this.jerseyNumber = jerseyNumber;
        this.team = team;
    }

}
