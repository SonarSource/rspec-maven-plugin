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
    var repositoryConfiguration = resolveRepositoryConfiguration(branchName, rspecSha);
    return repositoryConfiguration.rspecSha() == null
      ? create(repositoryConfiguration.branchName(), githubToken)
      : createAtRevision(repositoryConfiguration.rspecSha(), githubToken);
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

  static RepositoryConfiguration resolveRepositoryConfiguration(String branchName, String rspecSha) {
    var normalizedBranchName = normalize(branchName);
    var normalizedRspecSha = normalize(rspecSha);

    if (normalizedRspecSha != null) {
      return RepositoryConfiguration.forRevision(normalizedRspecSha);
    }

    return RepositoryConfiguration.forBranch(
      normalizedBranchName == null ? DEFAULT_BRANCH_NAME : normalizedBranchName
    );
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

  record RepositoryConfiguration(String branchName, String rspecSha) {
    private static RepositoryConfiguration forBranch(String branchName) {
      return new RepositoryConfiguration(branchName, null);
    }

    private static RepositoryConfiguration forRevision(String rspecSha) {
      return new RepositoryConfiguration(null, rspecSha);
    }
  }
}
