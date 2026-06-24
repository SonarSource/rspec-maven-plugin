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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class ApplicationRuleRepository implements domain.RuleRepository {

  private static final String DEFAULT_BRANCH_NAME = "master";
  private static final String BRANCH_AND_SHA_ERROR_MESSAGE = "Use either vcsBranchName or rspecSha, not both.";
  private static final ConcurrentMap<RepositoryKey, ApplicationRuleRepository> CACHE =
    new ConcurrentHashMap<>();
  private static final RuleMakerCreator DEFAULT_RULE_MAKER_CREATOR = new DefaultRuleMakerCreator();

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
    return createFromConfiguration(branchName, githubToken, rspecSha, message -> {}, DEFAULT_RULE_MAKER_CREATOR);
  }

  static ApplicationRuleRepository createFromConfiguration(
    String branchName,
    String githubToken,
    String rspecSha,
    Consumer<String> logger
  ) {
    return createFromConfiguration(branchName, githubToken, rspecSha, logger, DEFAULT_RULE_MAKER_CREATOR);
  }

  static ApplicationRuleRepository createFromConfiguration(
    String branchName,
    String githubToken,
    String rspecSha,
    Consumer<String> logger,
    RuleMakerCreator ruleMakerCreator
  ) {
    var normalizedBranchName = normalize(branchName);
    var normalizedRspecSha = normalize(rspecSha);

    if (normalizedRspecSha != null && normalizedBranchName != null) {
      throw new IllegalArgumentException(BRANCH_AND_SHA_ERROR_MESSAGE);
    }

    var repositoryKey =
      normalizedRspecSha == null
        ? RepositoryKey.forBranch(normalizedBranchName == null ? DEFAULT_BRANCH_NAME : normalizedBranchName)
        : RepositoryKey.forRevision(normalizedRspecSha);
    var cachedRepository = CACHE.get(repositoryKey);
    if (cachedRepository != null) {
      logger.accept(String.format("Reusing cached RSPEC repository for %s", repositoryKey.describe()));
      return cachedRepository;
    }

    var createdRepository = createRepository(repositoryKey, githubToken, ruleMakerCreator);
    var racedRepository = CACHE.putIfAbsent(repositoryKey, createdRepository);
    if (racedRepository != null) {
      logger.accept(String.format("Reusing cached RSPEC repository for %s", repositoryKey.describe()));
      return racedRepository;
    }

    logger.accept(String.format("Initialized RSPEC repository for %s", repositoryKey.describe()));
    return createdRepository;
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

  private static ApplicationRuleRepository createRepository(
    RepositoryKey repositoryKey,
    String githubToken,
    RuleMakerCreator ruleMakerCreator
  ) {
    return repositoryKey.rspecSha() == null
      ? new ApplicationRuleRepository(ruleMakerCreator.createForBranch(repositoryKey.branchName(), githubToken))
      : new ApplicationRuleRepository(ruleMakerCreator.createAtRevision(repositoryKey.rspecSha(), githubToken));
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

  private record RepositoryKey(String branchName, String rspecSha) {
    private static RepositoryKey forBranch(String branchName) {
      return new RepositoryKey(branchName, null);
    }

    private static RepositoryKey forRevision(String rspecSha) {
      return new RepositoryKey(null, rspecSha);
    }

    private String describe() {
      return rspecSha == null ? String.format("branch '%s'", branchName) : String.format("revision '%s'", rspecSha);
    }
  }

  interface RuleMakerCreator {
    GitHubRuleMaker createForBranch(String branchName, String githubToken);

    GitHubRuleMaker createAtRevision(String rspecSha, String githubToken);
  }

  private static final class DefaultRuleMakerCreator implements RuleMakerCreator {
    @Override
    public GitHubRuleMaker createForBranch(String branchName, String githubToken) {
      return GitHubRuleMaker.create(branchName, githubToken);
    }

    @Override
    public GitHubRuleMaker createAtRevision(String rspecSha, String githubToken) {
      return GitHubRuleMaker.createAtRevision(rspecSha, githubToken);
    }
  }
}
