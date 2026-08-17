/*
 * RSPEC Maven Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.sonarsource.ruleapi.domain.Profile;
import com.sonarsource.ruleapi.domain.RuleFiles;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RuleFactoryTest {

  @Test
  void shouldSelectQualityProfilesForCompatibleLanguage() {
    var metadata = JsonParser
      .parseString(
        """
        {
          "title": "Rule",
          "type": "CODE_SMELL",
          "defaultSeverity": "MAJOR",
          "scope": "All",
          "status": "ready",
          "compatibleLanguages": ["js", "ts"],
          "defaultQualityProfiles": {
            "js": [],
            "ts": ["Sonar way", "Sonar agentic AI"]
          }
        }
        """
      )
      .getAsJsonObject();
    var ruleFiles = new RuleFiles(
      "S111",
      metadata,
      "<p>Rule</p>",
      Set.of(new Profile("Sonar way"), new Profile("Sonar agentic AI")),
      Set.of()
    );
    var rule = RuleFactory.create("javascript", ruleFiles);

    assertEquals(List.of(), rule.qualityProfiles("js"));
    assertEquals(List.of("Sonar way", "Sonar agentic AI"), rule.qualityProfiles("ts"));
  }

  @Test
  void shouldApplyArrayQualityProfilesToEveryCompatibleLanguage() {
    var metadata = JsonParser
      .parseString(
        """
        {
          "title": "Rule",
          "type": "CODE_SMELL",
          "defaultSeverity": "MAJOR",
          "scope": "All",
          "status": "ready",
          "compatibleLanguages": ["js", "ts"]
        }
        """
      )
      .getAsJsonObject();
    var ruleFiles = new RuleFiles(
      "S111",
      metadata,
      "<p>Rule</p>",
      Set.of(new Profile("Sonar way")),
      Set.of()
    );
    var rule = RuleFactory.create("javascript", ruleFiles);

    assertEquals(List.of("Sonar way"), rule.qualityProfiles("js"));
    assertEquals(List.of("Sonar way"), rule.qualityProfiles("ts"));
  }
}
