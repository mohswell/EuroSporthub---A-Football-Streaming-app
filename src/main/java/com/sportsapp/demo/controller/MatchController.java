package com.sportsapp.demo.controller;

import com.sportsapp.demo.model.Match;
import com.sportsapp.demo.service.LiveScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    @Autowired
    private LiveScoreService liveScoreService;

    @GetMapping
    public List<Match> getAllMatches() {
        return liveScoreService.getAllMatches();
    }

    @GetMapping("/{id}")
    public Match getMatchById(@PathVariable String id) {
        return liveScoreService.getMatchById(id);
    }

    @GetMapping("/team/{teamId}")
    public List<Match> getMatchesByTeam(@PathVariable String teamId) {
        return liveScoreService.getMatchesByTeam(teamId);
    }

    @PutMapping("/{id}")
    public Match updateMatch(@PathVariable String id, @RequestBody Match matchDetails) {
        return liveScoreService.updateMatch(id, matchDetails);
    }

    @DeleteMapping("/{id}")
    public void deleteMatch(@PathVariable String id) {
        liveScoreService.deleteMatch(id);
    }

    @GetMapping("/livescores")
    public List<Match> fetchLiveScores(@RequestParam Map<String, String> params) {
        return liveScoreService.fetchLiveScores(params);
    }
}
