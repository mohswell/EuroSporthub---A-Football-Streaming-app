package com.sportsapp.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sportsapp.demo.exception.ResourceNotFoundException;
import com.sportsapp.demo.model.Player;
import com.sportsapp.demo.repository.PlayerRepository;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    public List<Player> getAllPlayers(){
        return playerRepository.findAll();
    }

    // Find players by team
    public List<Player> getPlayersByTeam(String teamId){
        return playerRepository.findByTeamId(teamId);
    }

    // Find specific player by Id
    public Player getPlayerById(String id) {
        return playerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Player not found"));
    }

    // Create a new player
    public Player createPlayer(Player player){
        return playerRepository.save(player);
    }

    // Update player details
    public Player updatePlayer(String id, Player playerDetails){
        Player player = playerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Player not found"));
        player.setName(playerDetails.getName());
        player.setPosition(playerDetails.getPosition());
        player.setJerseyNumber(playerDetails.getJerseyNumber());
        player.setTeam(playerDetails.getTeam());
        return playerRepository.save(player);
    }

    // Delete player
    public void deletePlayer(String id){
        Player player = playerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Player not found"));
        playerRepository.delete(player);
    }

}
