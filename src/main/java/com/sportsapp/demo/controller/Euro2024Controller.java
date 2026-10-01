package com.sportsapp.demo.controller;

import com.sportsapp.demo.service.Euro2024Service;
import com.sportsapp.demo.service.EuroSyncService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/euro-2024")
public class Euro2024Controller {
    private final Euro2024Service euro2024Service;
    private final EuroSyncService euroSyncService;
    private final String syncToken;

    public Euro2024Controller(
            Euro2024Service euro2024Service,
            EuroSyncService euroSyncService,
            @Qualifier("euroSyncToken") String syncToken) {
        this.euro2024Service = euro2024Service;
        this.euroSyncService = euroSyncService;
        this.syncToken = syncToken;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return euro2024Service.summary();
    }

    @GetMapping("/sync-runs")
    public List<Map<String, Object>> syncRuns() {
        return euro2024Service.syncRuns();
    }

    @GetMapping({"/teams", "/participants"})
    public List<Map<String, Object>> teams() {
        return euro2024Service.teams();
    }

    @GetMapping("/fixtures")
    public List<Map<String, Object>> fixtures(
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String status) {
        return euro2024Service.fixtures(groupId, teamId, status);
    }

    @GetMapping("/fixtures/group/{groupId}")
    public List<Map<String, Object>> groupFixtures(@PathVariable String groupId) {
        return euro2024Service.fixtures(groupId, null, null);
    }

    @GetMapping("/fixtures/team/{teamId}")
    public List<Map<String, Object>> teamFixtures(@PathVariable String teamId) {
        return euro2024Service.fixtures(null, teamId, null);
    }

    @GetMapping("/groups")
    public List<Map<String, Object>> groups() {
        return euro2024Service.groups();
    }

    @GetMapping("/groups/{groupId}/table")
    public List<Map<String, Object>> table(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "false") boolean live) {
        return euro2024Service.table(groupId, live);
    }

    @GetMapping("/groups/{groupId}/standings")
    public List<Map<String, Object>> finalStandings(@PathVariable String groupId) {
        return euro2024Service.table(groupId, false);
    }

    @GetMapping("/groups/{groupId}/standings/live")
    public List<Map<String, Object>> liveStandings(@PathVariable String groupId) {
        return euro2024Service.table(groupId, true);
    }

    @GetMapping("/players")
    public List<Map<String, Object>> players(@RequestParam(required = false) String teamId) {
        return euro2024Service.players(teamId);
    }

    @GetMapping("/players/team/{teamId}")
    public List<Map<String, Object>> teamPlayers(@PathVariable String teamId) {
        return euro2024Service.players(teamId);
    }

    @PostMapping("/sync")
    public Map<String, Object> sync(@RequestHeader(name = "X-Sync-Token", required = false) String providedToken) {
        if (syncToken == null || syncToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "EURO_SYNC_TOKEN is not configured on the API server");
        }
        if (providedToken == null || !MessageDigest.isEqual(
                syncToken.getBytes(StandardCharsets.UTF_8),
                providedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A valid X-Sync-Token is required");
        }
        return euroSyncService.sync();
    }
}
