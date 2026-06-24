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

import domain.RuleDataTarget;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class GenerateRuleDataMojoConfiguration {

  private static final String MIXED_TARGETS_ERROR_MESSAGE =
    "Use either targets or ruleSubdirectory/targetDirectory, not both.";
  private static final String MISSING_TARGETS_ERROR_MESSAGE =
    "Either targets or both ruleSubdirectory and targetDirectory must be provided.";

  private GenerateRuleDataMojoConfiguration() {}

  static List<RuleDataTarget> resolveTargets(
    String ruleSubdirectory,
    String targetDirectory,
    List<RuleDataTargetConfiguration> targets
  ) {
    var hasLegacyTarget = normalize(ruleSubdirectory) != null || normalize(targetDirectory) != null;
    var hasMultiTargets = targets != null && !targets.isEmpty();

    if (hasLegacyTarget && hasMultiTargets) {
      throw new IllegalArgumentException(MIXED_TARGETS_ERROR_MESSAGE);
    }

    if (hasMultiTargets) {
      return targets.stream().map(GenerateRuleDataMojoConfiguration::toRuleDataTarget).toList();
    }

    var normalizedRuleSubdirectory = normalize(ruleSubdirectory);
    var normalizedTargetDirectory = normalize(targetDirectory);
    if (normalizedRuleSubdirectory == null || normalizedTargetDirectory == null) {
      throw new IllegalArgumentException(MISSING_TARGETS_ERROR_MESSAGE);
    }

    return List.of(new RuleDataTarget(normalizedRuleSubdirectory, normalizedTargetDirectory));
  }

  static ResolvedRspecSha resolveRspecSha(String rspecSha, String rspecShaFile) {
    var normalizedRspecSha = normalize(rspecSha);
    if (normalizedRspecSha != null) {
      return new ResolvedRspecSha(normalizedRspecSha, "parameter rspecSha");
    }

    var normalizedRspecShaFile = normalize(rspecShaFile);
    if (normalizedRspecShaFile == null) {
      return null;
    }

    var rspecShaPath = Path.of(normalizedRspecShaFile);
    if (!Files.exists(rspecShaPath)) {
      return null;
    }

    try {
      var fileContents = normalize(Files.readString(rspecShaPath));
      if (fileContents == null) {
        return null;
      }
      return new ResolvedRspecSha(fileContents, String.format("file '%s'", rspecShaPath));
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("Failed to read rspecShaFile '%s'.", rspecShaPath), e);
    }
  }

  private static RuleDataTarget toRuleDataTarget(RuleDataTargetConfiguration target) {
    var normalizedRuleSubdirectory = normalize(target.getRuleSubdirectory());
    var normalizedTargetDirectory = normalize(target.getTargetDirectory());

    if (normalizedRuleSubdirectory == null || normalizedTargetDirectory == null) {
      throw new IllegalArgumentException(MISSING_TARGETS_ERROR_MESSAGE);
    }

    return new RuleDataTarget(normalizedRuleSubdirectory, normalizedTargetDirectory);
  }

  private static String normalize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  record ResolvedRspecSha(String sha, String sourceDescription) {}
}
