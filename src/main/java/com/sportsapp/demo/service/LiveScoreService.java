package com.sportsapp.demo.service;

import com.sportsapp.demo.model.Match;
import com.sportsapp.demo.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class LiveScoreService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private String liveScoreApiUrl;

    @Autowired
    private String liveScoreApiKey;

    @Autowired
    private String liveScoreApiSecret;

    public List<Match> fetchLiveScores(Map<String, String> params) {
        // Construct the full API URL with the key, secret, and any additional parameters
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(liveScoreApiUrl)
            .queryParam("key", liveScoreApiKey)
            .queryParam("secret", liveScoreApiSecret);

        // Add additional parameters to the URL
        for (Map.Entry<String, String> param : params.entrySet()) {
            uriBuilder.queryParam(param.getKey(), param.getValue());
        }

        String url = uriBuilder.toUriString();
        ResponseEntity<Match[]> response = restTemplate.getForEntity(url, Match[].class);

        Match[] matches = response.getBody();
        List<Match> matchList = Arrays.asList(matches);

        // Save or update matches in the database
        for (Match match : matchList) {
            matchRepository.save(match);
        }

        return matchList;
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Match getMatchById(String id) {
        return matchRepository.findById(id).orElse(null);
    }

    public List<Match> getMatchesByTeam(String teamId) {
        return matchRepository.findByTeam1OrTeam2(teamId, teamId);
    }

    public Match updateMatch(String id, Match matchDetails) {
        Match match = matchRepository.findById(id).orElse(null);
        if (match != null) {
            match.setMatchDateTime(matchDetails.getMatchDateTime());
            match.setVenue(matchDetails.getVenue());
            match.setTeam1(matchDetails.getTeam1());
            match.setTeam2(matchDetails.getTeam2());
            match.setTeam1Score(matchDetails.getTeam1Score());
            match.setTeam2Score(matchDetails.getTeam2Score());
            return matchRepository.save(match);
        }
        return null;
    }

    public void deleteMatch(String id) {
        matchRepository.deleteById(id);
    }
}
