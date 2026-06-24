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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.RuleDataTarget;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateRuleDataMojoConfigurationTest {

  @TempDir
  Path tempDir;

  @Test
  void shouldResolveLegacySingleTarget() {
    var targets = GenerateRuleDataMojoConfiguration.resolveTargets(" javascript ", " target/js ", null);

    assertEquals(List.of(new RuleDataTarget("javascript", "target/js")), targets);
  }

  @Test
  void shouldResolveConfiguredTargets() {
    var javascript = createTarget(" javascript ", " target/js ");
    var css = createTarget(" css ", " target/css ");

    var targets = GenerateRuleDataMojoConfiguration.resolveTargets(null, null, List.of(javascript, css));

    assertEquals(
      List.of(new RuleDataTarget("javascript", "target/js"), new RuleDataTarget("css", "target/css")),
      targets
    );
  }

  @Test
  void shouldRejectMixedTargetConfiguration() {
    var target = createTarget("javascript", "target/js");

    var error = assertThrows(
      IllegalArgumentException.class,
      () -> GenerateRuleDataMojoConfiguration.resolveTargets("javascript", "target/js", List.of(target))
    );

    assertEquals("Use either targets or ruleSubdirectory/targetDirectory, not both.", error.getMessage());
  }

  @Test
  void shouldResolveRspecShaFromFile() throws Exception {
    var rspecShaFile = tempDir.resolve("rspec.sha");
    Files.writeString(rspecShaFile, " abc123 \n");

    var resolved = GenerateRuleDataMojoConfiguration.resolveRspecSha(null, rspecShaFile.toString());

    assertEquals(new GenerateRuleDataMojoConfiguration.ResolvedRspecSha("abc123", "file '" + rspecShaFile + "'"), resolved);
  }

  @Test
  void shouldPreferExplicitRspecShaOverFile() throws Exception {
    var rspecShaFile = tempDir.resolve("rspec.sha");
    Files.writeString(rspecShaFile, "from-file\n");

    var resolved = GenerateRuleDataMojoConfiguration.resolveRspecSha("from-parameter", rspecShaFile.toString());

    assertEquals(
      new GenerateRuleDataMojoConfiguration.ResolvedRspecSha("from-parameter", "parameter rspecSha"),
      resolved
    );
  }

  @Test
  void shouldIgnoreMissingOrBlankRspecShaFile() throws Exception {
    assertNull(GenerateRuleDataMojoConfiguration.resolveRspecSha(null, tempDir.resolve("missing.sha").toString()));

    var rspecShaFile = tempDir.resolve("blank.sha");
    Files.writeString(rspecShaFile, " \n");

    assertNull(GenerateRuleDataMojoConfiguration.resolveRspecSha(null, rspecShaFile.toString()));
  }

  private static RuleDataTargetConfiguration createTarget(String ruleSubdirectory, String targetDirectory) {
    var target = new RuleDataTargetConfiguration();
    target.setRuleSubdirectory(ruleSubdirectory);
    target.setTargetDirectory(targetDirectory);
    return target;
  }
}
