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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.sonarsource.ruleapi.domain.RuleFiles;
import domain.FileSystem;
import domain.RegistrarsGenerator;
import domain.Rule;
import domain.RuleRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RegistrarsGeneratorTest {

  @Test
  void shouldActivateRulesForTheCompatibleLanguage() throws domain.Exception {
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
            "ts": ["Sonar way"]
          }
        }
        """
      )
      .getAsJsonObject();
    var rule = RuleFactory.create(
      "javascript",
      new RuleFiles("S111", metadata, "<p>Rule</p>", Set.of(), Set.of())
    );
    var fileSystem = new RecordingFileSystem();
    var generator = new RegistrarsGenerator(
      message -> {},
      new TestRuleRepository(rule),
      fileSystem
    );

    generator.execute("org.sonar", "javascript", "js", "javascript", "/js", "Sonar way");
    generator.execute("org.sonar", "javascript", "ts", "typescript", "/ts", "Sonar way");

    assertFalse(fileSystem.writes.get("/js/javascriptProfileRegistrar.java").contains("activateRule"));
    assertTrue(
      fileSystem.writes.get("/ts/typescriptProfileRegistrar.java").contains("activateRule")
    );
  }

  private record TestRuleRepository(Rule rule) implements RuleRepository {
    public List<Rule> getRulesByLanguage(String languageKey) {
      return List.of(rule);
    }

    public List<RuleFiles> getRuleManifestsByRuleSubdirectory(String ruleSubdirectory) {
      return List.of();
    }
  }

  private static final class RecordingFileSystem implements FileSystem {
    private final Map<String, String> writes = new HashMap<>();

    public String resolve(String first, String... more) {
      return first + "/" + String.join("/", more);
    }

    public void write(String filePath, String content) {
      writes.put(filePath, content);
    }
  }
}
