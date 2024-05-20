package com.sportsapp.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sportsapp.demo.exception.ResourceNotFoundException;
import com.sportsapp.demo.model.Match;
import com.sportsapp.demo.model.Team;
import com.sportsapp.demo.repository.MatchRepository;
import com.sportsapp.demo.repository.TeamRepository;

@Service
public class MatchService {
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TeamRepository teamRepository;

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    // Get match by id
    public Match getMatchById(String id){
        return matchRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Match Not Found"));
    }

    // Create a new match
    public Match createMatch(Match match){
        return matchRepository.save(match);
    }

    // Get all Matches by the Team either Team1/Team2
    public List<Match> getMatchesByTeam(String teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new ResourceNotFoundException("Team not found"));
        return matchRepository.findByTeam1OrTeam2(team, team);
    }

    // Update match details
    public Match updateMatch(String id, Match matchDetails){
        Match match = matchRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        match.setTeam1(matchDetails.getTeam1());
        match.setTeam2(matchDetails.getTeam2());
        match.setMatchDateTime(matchDetails.getMatchDateTime());
        match.setVenue(matchDetails.getVenue());
        match.setMatchResult(matchDetails.getMatchResult());
        match.setMatchStatus(matchDetails.getMatchStatus());
        match.setTeam1Score(matchDetails.getTeam1Score());
        match.setTeam2Score(matchDetails.getTeam2Score());
        return matchRepository.save(match);
    }

    // Delete match e.g next matchweek
    public void deleteMatch(String id){
        Match match = matchRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        matchRepository.delete(match);
    }
}
