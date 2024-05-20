package com.sportsapp.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

@Document(collection = "matches")
public class Match{
    @Id
    private String id;

    private LocalDateTime matchDateTime;
    private String venue;
    
    @DBRef
    private Team team1;
    @DBRef
    private Team team2;
    private int team1Score;
    private int team2Score;
    private String matchResult;
    private String matchStatus;
    
    // Getters and Setters
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id=id;
    }

    public LocalDateTime getMatchDateTime(){
        return matchDateTime;
    }
    public void setMatchDateTime(LocalDateTime matchDateTime){
        this.matchDateTime= matchDateTime;
    }

    public String getVenue(){
        return venue;
    }
    public void setVenue(String venue){
        this.venue= venue;
    }
    public Team getTeam1(){
        return team1;
    }
    public void setTeam1(Team team1){
        this.team1 = team1;
    }
    public Team getTeam2(){
        return team2;
    }
    public void setTeam2(Team team2){
        this.team2 = team2;
    }
    public int getTeam1Score(){
        return team1Score;
    }
    public void setTeam1Score(int team1Score){
        this.team1Score = team1Score;
    }
    public int getTeam2Score(){
        return team2Score;
    }
    public void setTeam2Score(int team2Score){
        this.team2Score = team2Score;
    }
    public String getMatchResult(){
        return matchResult;
    }
    public void setMatchResult(String matchResult){
        this.matchResult = matchResult;
    }
    public String getMatchStatus(){
        return matchStatus;
    }
    public void setMatchStatus(String matchStatus){
        this.matchStatus = matchStatus;
    }

    // Default Constructor
    public Match() {
    }

    // Optional args Constructor
    public Match(LocalDateTime matchDateTime, String venue, Team team1, Team team2, int team1Score, int team2Score, String matchResult, String matchStatus) {
        this.matchDateTime = matchDateTime;
        this.venue = venue;
        this.team1 = team1;
        this.team2 = team2;
        this.team1Score = team1Score;
        this.team2Score = team2Score;
        this.matchResult = matchResult;
        this.matchStatus = matchStatus;
    }
}