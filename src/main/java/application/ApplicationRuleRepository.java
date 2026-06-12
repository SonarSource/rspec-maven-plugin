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

import com.sonarsource.ruleapi.domain.RuleFiles;
import com.sonarsource.ruleapi.github.GitHubRuleMaker;
import domain.Rule;
import java.util.List;

public class ApplicationRuleRepository implements domain.RuleRepository {

  private static final String DEFAULT_BRANCH_NAME = "master";
  private static final String BRANCH_AND_SHA_ERROR_MESSAGE = "Use either vcsBranchName or rspecSha, not both.";

  private final GitHubRuleMaker ruleMaker;

  public static ApplicationRuleRepository create(String branchName, String githubToken) {
    var normalizedBranchName = normalize(branchName);
    return new ApplicationRuleRepository(
      GitHubRuleMaker.create(normalizedBranchName == null ? DEFAULT_BRANCH_NAME : normalizedBranchName, githubToken)
    );
  }

  public static ApplicationRuleRepository createAtRevision(String rspecSha, String githubToken) {
    var normalizedRspecSha = normalize(rspecSha);
    if (normalizedRspecSha == null) {
      throw new IllegalArgumentException("rspecSha must not be blank.");
    }
    return new ApplicationRuleRepository(
      GitHubRuleMaker.createAtRevision(normalizedRspecSha, githubToken)
    );
  }

  static ApplicationRuleRepository createFromConfiguration(String branchName, String githubToken, String rspecSha) {
    var normalizedBranchName = normalize(branchName);
    var normalizedRspecSha = normalize(rspecSha);

    if (normalizedRspecSha == null) {
      return create(normalizedBranchName, githubToken);
    }
    if (normalizedBranchName != null) {
      throw new IllegalArgumentException(BRANCH_AND_SHA_ERROR_MESSAGE);
    }
    return createAtRevision(normalizedRspecSha, githubToken);
  }

  private ApplicationRuleRepository(GitHubRuleMaker ruleMaker) {
    this.ruleMaker = ruleMaker;
  }

  private static String normalize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  public List<RuleFiles> getRuleManifestsByRuleSubdirectory(String ruleSubdirectory) {
    return this.ruleMaker.getRulesByRuleSubdirectory(ruleSubdirectory);
  }

  public List<Rule> getRulesByLanguage(String languageKey) {
    var ruleManifests = this.getRuleManifestsByRuleSubdirectory(languageKey);

    return ruleManifests
      .stream()
      .map(ruleManifest -> RuleFactory.create(languageKey, ruleManifest))
      .toList();
  }
}
