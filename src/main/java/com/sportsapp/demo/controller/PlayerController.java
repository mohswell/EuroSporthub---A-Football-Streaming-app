package com.sportsapp.demo.controller;

import com.sportsapp.demo.model.Player;
import com.sportsapp.demo.service.PlayerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    // Get Request for the all the players endpoint
    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    // Get Request for the player for each team using teamId param endpoint
    @GetMapping("/team/{teamId}")
    public List<Player> getPlayersByTeam(@PathVariable String teamId) {
        return playerService.getPlayersByTeam(teamId);
    }

    // Get Request for the player for each team using playerId param endpoint
    @GetMapping("/{id}")
    public Player getPlayerById(@PathVariable String id) {
        return playerService.getPlayerById(id);
    }

    // Post Request for the player endpoint
    @PostMapping
    public Player createPlayer(@RequestBody Player player) {
        return playerService.createPlayer(player);
    }

    // Update Player with player id param, ensure you attach the body with the values to put onto the player
    @PutMapping("/{id}")
    public Player updatePlayer(@PathVariable String id, @RequestBody Player playerDetails) {
        return playerService.updatePlayer(id, playerDetails);
    }
    
    // Delete player endpoint
    // Ensure to add auth security here to prevent unauthorized deletion of data
    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable String id){
        playerService.deletePlayer(id);
    }
}
