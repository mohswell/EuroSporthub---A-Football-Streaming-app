package com.sportsapp.demo.service;

import com.sportsapp.demo.model.Team;
import com.sportsapp.demo.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sportsapp.demo.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class TeamService {
    @Autowired
    private TeamRepository teamRepository;

    public List<Team> getAllTeams(){
        return teamRepository.findAll();
    }

    public Team getTeamById(String id){
        return teamRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Team Not Found"));
    }

    public Team createTeam(Team team){
        return teamRepository.save(team);
    }

    // Method to update team e.g. team goes to knockout, players are eliminated
    public Team updateTeam(String id, Team teamDetails) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Team not found"));
        team.setName(teamDetails.getName());
        team.setCoach(teamDetails.getCoach());
        team.setGroup(teamDetails.getGroup());
        team.setPlayers(teamDetails.getPlayers());
        return teamRepository.save(team);
    }
    
    // Method to delete team e.g. team is eliminated from euros
    public void deleteTeam(String id){
        Team team = teamRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Team not found"));
        teamRepository.delete(team);
    }
}
