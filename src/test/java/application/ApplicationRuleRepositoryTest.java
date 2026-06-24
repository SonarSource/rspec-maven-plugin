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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sonarsource.ruleapi.github.GitHubRuleMaker;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ApplicationRuleRepositoryTest {

  @Test
  void shouldReuseRepositoryForSameBranch() {
    var branchName = uniqueValue("branch");
    var messages = new ArrayList<String>();
    var ruleMakerCreator = new RecordingRuleMakerCreator();

    var first = ApplicationRuleRepository.createFromConfiguration(
      branchName,
      "token",
      null,
      messages::add,
      ruleMakerCreator
    );
    var second = ApplicationRuleRepository.createFromConfiguration(
      branchName,
      "token",
      null,
      messages::add,
      ruleMakerCreator
    );

    assertSame(first, second);
    assertEquals(1, ruleMakerCreator.branchCalls.get());
    assertEquals(0, ruleMakerCreator.revisionCalls.get());
    assertEquals(
      List.of(
        String.format("Initialized RSPEC repository for branch '%s'", branchName),
        String.format("Reusing cached RSPEC repository for branch '%s'", branchName)
      ),
      messages
    );
  }

  @Test
  void shouldReuseRepositoryForSameRevision() {
    var revision = uniqueValue("revision");
    var messages = new ArrayList<String>();
    var ruleMakerCreator = new RecordingRuleMakerCreator();

    var first = ApplicationRuleRepository.createFromConfiguration(
      null,
      "token",
      revision,
      messages::add,
      ruleMakerCreator
    );
    var second = ApplicationRuleRepository.createFromConfiguration(
      null,
      "token",
      revision,
      messages::add,
      ruleMakerCreator
    );

    assertSame(first, second);
    assertEquals(0, ruleMakerCreator.branchCalls.get());
    assertEquals(1, ruleMakerCreator.revisionCalls.get());
    assertEquals(
      List.of(
        String.format("Initialized RSPEC repository for revision '%s'", revision),
        String.format("Reusing cached RSPEC repository for revision '%s'", revision)
      ),
      messages
    );
  }

  @Test
  void shouldCreateSeparateRepositoriesForDifferentBranches() {
    var firstBranch = uniqueValue("branch");
    var secondBranch = uniqueValue("branch");
    var messages = new ArrayList<String>();
    var ruleMakerCreator = new RecordingRuleMakerCreator();

    var first = ApplicationRuleRepository.createFromConfiguration(
      firstBranch,
      "token",
      null,
      messages::add,
      ruleMakerCreator
    );
    var second = ApplicationRuleRepository.createFromConfiguration(
      secondBranch,
      "token",
      null,
      messages::add,
      ruleMakerCreator
    );

    assertNotSame(first, second);
    assertEquals(2, ruleMakerCreator.branchCalls.get());
    assertEquals(0, ruleMakerCreator.revisionCalls.get());
    assertEquals(
      List.of(
        String.format("Initialized RSPEC repository for branch '%s'", firstBranch),
        String.format("Initialized RSPEC repository for branch '%s'", secondBranch)
      ),
      messages
    );
  }

  @Test
  void shouldRejectBranchAndRevisionTogether() {
    var messages = new ArrayList<String>();
    var ruleMakerCreator = new RecordingRuleMakerCreator();

    var error = assertThrows(
      IllegalArgumentException.class,
      () ->
        ApplicationRuleRepository.createFromConfiguration(
          uniqueValue("branch"),
          "token",
          uniqueValue("revision"),
          messages::add,
          ruleMakerCreator
        )
    );

    assertEquals("Use either vcsBranchName or rspecSha, not both.", error.getMessage());
    assertEquals(0, ruleMakerCreator.branchCalls.get());
    assertEquals(0, ruleMakerCreator.revisionCalls.get());
    assertEquals(List.of(), messages);
  }

  private static String uniqueValue(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private static final class RecordingRuleMakerCreator implements ApplicationRuleRepository.RuleMakerCreator {
    private final AtomicInteger branchCalls = new AtomicInteger();
    private final AtomicInteger revisionCalls = new AtomicInteger();

    @Override
    public GitHubRuleMaker createForBranch(String branchName, String githubToken) {
      branchCalls.incrementAndGet();
      return null;
    }

    @Override
    public GitHubRuleMaker createAtRevision(String rspecSha, String githubToken) {
      revisionCalls.incrementAndGet();
      return null;
    }
  }
}
