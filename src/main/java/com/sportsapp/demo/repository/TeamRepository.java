package com.sportsapp.demo.repository;

import com.sportsapp.demo.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeamRepository extends MongoRepository<Team, String> {
}
