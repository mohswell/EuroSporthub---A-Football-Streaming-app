package com.sportsapp.demo.repository;

import com.sportsapp.demo.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PlayerRepository extends MongoRepository<Player, String> {
    List<Player> findByTeamId(String teamId);
}

