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
package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.sonarsource.ruleapi.domain.Profile;
import com.sonarsource.ruleapi.domain.RuleFiles;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RuleDataGeneratorTest {

  @Test
  void shouldGenerateRuleDataForAllTargets() throws Exception {
    var repository = new RecordingRuleRepository(
      Map.of(
        "javascript",
        List.of(createRuleFiles("S111", "<p>js</p>", Set.of(new Profile("Sonar way")))),
        "css",
        List.of(createRuleFiles("S222", "<p>css</p>", Set.of(new Profile("Sonar way"))))
      )
    );
    var fileSystem = new RecordingFileSystem();
    var logs = new ArrayList<String>();

    var generator = new RuleDataGenerator(logs::add, repository, fileSystem);
    generator.execute(
      List.of(
        new RuleDataTarget("javascript", "/generated/javascript"),
        new RuleDataTarget("css", "/generated/css")
      )
    );

    assertEquals(List.of("javascript", "css"), repository.requestedSubdirectories);
    assertEquals(
      List.of(
        "Generating javascript rule data into /generated/javascript",
        "Generating css rule data into /generated/css"
      ),
      logs
    );
    assertEquals("<p>js</p>", fileSystem.writes.get("/generated/javascript/S111.html"));
    assertEquals("<p>css</p>", fileSystem.writes.get("/generated/css/S222.html"));
    assertTrue(fileSystem.writes.get("/generated/javascript/S111.json").contains("\"defaultQualityProfiles\""));
    assertTrue(fileSystem.writes.get("/generated/javascript/S111.json").contains("\"Sonar way\""));
    assertTrue(fileSystem.writes.get("/generated/css/S222.json").contains("\"defaultQualityProfiles\""));
    assertTrue(fileSystem.writes.get("/generated/css/S222.json").contains("\"Sonar way\""));
  }

  private static RuleFiles createRuleFiles(String key, String description, Set<Profile> profiles) {
    return new RuleFiles(key, new JsonObject(), description, profiles, Set.of());
  }

  private static final class RecordingRuleRepository implements RuleRepository {
    private final Map<String, List<RuleFiles>> rulesBySubdirectory;
    private final List<String> requestedSubdirectories = new ArrayList<>();

    private RecordingRuleRepository(Map<String, List<RuleFiles>> rulesBySubdirectory) {
      this.rulesBySubdirectory = rulesBySubdirectory;
    }

    @Override
    public List<Rule> getRulesByLanguage(String languageKey) {
      return List.of();
    }

    @Override
    public List<RuleFiles> getRuleManifestsByRuleSubdirectory(String ruleSubdirectory) {
      requestedSubdirectories.add(ruleSubdirectory);
      return rulesBySubdirectory.getOrDefault(ruleSubdirectory, List.of());
    }
  }

  private static final class RecordingFileSystem implements FileSystem {
    private final Map<String, String> writes = new LinkedHashMap<>();

    @Override
    public String resolve(String first, String... more) {
      var path = new StringBuilder(first);
      for (var segment : more) {
        path.append('/').append(segment);
      }
      return path.toString();
    }

    @Override
    public void write(String filePath, String content) {
      writes.put(filePath, content);
    }
  }
}
