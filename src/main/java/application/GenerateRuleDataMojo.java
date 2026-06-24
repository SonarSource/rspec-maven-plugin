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

import domain.Exception;
import domain.RuleDataGenerator;
import infrastructure.JVMHost;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generate-rule-data")
public class GenerateRuleDataMojo extends AbstractMojo {

  @Parameter
  private String ruleSubdirectory;

  @Parameter
  private String targetDirectory;

  @Parameter
  private List<RuleDataTargetConfiguration> targets;

  /**
   * Optional RSPEC branch name. When omitted, the plugin uses master unless rspecSha is set.
   */
  @Parameter
  private String vcsBranchName;

  /**
   * Optional GitHub token used to access the private RSPEC repository.
   * When omitted, sonar-rule-api falls back to GITHUB_TOKEN and then to the gh auth token command.
   */
  @Parameter(property = "githubToken")
  private String githubToken;

  /**
   * Optional RSPEC commit SHA used to pin rule data generation.
   */
  @Parameter(property = "rspec.sha")
  private String rspecSha;

  /**
   * Optional path to a file containing the RSPEC commit SHA used to pin rule data generation.
   */
  @Parameter
  private String rspecShaFile;

  @Override
  public void execute() throws MojoExecutionException {
    var host = new JVMHost();
    var logger = this.getLog();

    try {
      var resolvedTargets = GenerateRuleDataMojoConfiguration.resolveTargets(
        this.ruleSubdirectory,
        this.targetDirectory,
        this.targets
      );
      var resolvedRspecSha = GenerateRuleDataMojoConfiguration.resolveRspecSha(this.rspecSha, this.rspecShaFile);
      if (resolvedRspecSha != null) {
        logger.info(
          String.format("Using pinned RSPEC SHA %s from %s", resolvedRspecSha.sha(), resolvedRspecSha.sourceDescription())
        );
      }

      var generator = new RuleDataGenerator(
        logger::info,
        ApplicationRuleRepository.createFromConfiguration(
          this.vcsBranchName,
          this.githubToken,
          resolvedRspecSha == null ? null : resolvedRspecSha.sha()
        ),
        new ApplicationFileSystem(host)
      );

      generator.execute(resolvedTargets);
    } catch (IllegalArgumentException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    } catch (Exception e) {
      throw new MojoExecutionException(e);
    }
  }
}
