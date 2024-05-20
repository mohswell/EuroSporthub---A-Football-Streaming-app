package com.sportsapp.demo.repository;

import com.sportsapp.demo.model.Match;
import com.sportsapp.demo.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MatchRepository extends MongoRepository<Match, String> {
    List<Match> findByTeam1OrTeam2(Team team1, Team team2);
}

