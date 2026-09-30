package com.sportsapp.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void euroSummaryUsesTheMigratedDatabase() throws Exception {
		mockMvc.perform(get("/api/euro-2024/summary"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.team_count").value(0))
				.andExpect(jsonPath("$.match_count").value(0));
	}

	@Test
	void syncRequiresAConfiguredServerToken() throws Exception {
		mockMvc.perform(post("/api/euro-2024/sync"))
				.andExpect(status().isServiceUnavailable());
	}

	@Test
	@Transactional
	void individualRoutesReturnStableFrontendFields() throws Exception {
		jdbcTemplate.update("""
				INSERT INTO euro_competitions (id, name, season, country)
				VALUES ('387', 'Test Euro', 2024, 'Germany')
				""");
		jdbcTemplate.update("""
				INSERT INTO euro_groups (id, competition_id, name, stage)
				VALUES ('test-group', '387', 'Group T', 'Group Stage')
				""");
		jdbcTemplate.update("""
				INSERT INTO euro_teams (id, competition_id, name, group_id, group_name, logo_url)
				VALUES ('test-team', '387', 'Testland', 'test-group', 'Group T', 'crest.png')
				""");
		jdbcTemplate.update("""
				INSERT INTO euro_matches (id, competition_id, group_id, home_team_id, home_name, away_name,
					home_score, away_score, status)
				VALUES ('test-match', '387', 'test-group', 'test-team', 'Testland', 'Sample FC', 2, 1, 'FINISHED')
				""");
		jdbcTemplate.update("""
				INSERT INTO euro_group_standings (competition_id, group_id, team_id, group_name,
					position, played, won, drawn, lost, goals_for, goals_against, goal_difference, points)
				VALUES ('387', 'test-group', 'test-team', 'Group T', 1, 1, 1, 0, 0, 2, 1, 1, 3)
				""");

		mockMvc.perform(get("/api/euro-2024/participants"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("test-team"))
				.andExpect(jsonPath("$[0].name").value("Testland"));
		mockMvc.perform(get("/api/euro-2024/groups"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].groupId").value("test-group"))
				.andExpect(jsonPath("$[0].name").value("Group T"));
		mockMvc.perform(get("/api/euro-2024/fixtures/group/test-group"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("test-match"))
				.andExpect(jsonPath("$[0].homeName").value("Testland"));
		mockMvc.perform(get("/api/euro-2024/groups/test-group/standings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].teamName").value("Testland"))
				.andExpect(jsonPath("$[0].position").value(1));
	}

}
