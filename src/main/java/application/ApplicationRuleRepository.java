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

  private final GitHubRuleMaker ruleMaker;

  public ApplicationRuleRepository(String branchName, String githubToken) {
    this(branchName, githubToken, null);
  }

  public ApplicationRuleRepository(String branchName, String githubToken, String rspecSha) {
    this.ruleMaker = createRuleMaker(branchName, githubToken, rspecSha);
  }

  private static GitHubRuleMaker createRuleMaker(String branchName, String githubToken, String rspecSha) {
    if (rspecSha == null || rspecSha.isBlank()) {
      return GitHubRuleMaker.create(branchName, githubToken);
    }
    return GitHubRuleMaker.createAtRevision(rspecSha.trim(), githubToken);
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
