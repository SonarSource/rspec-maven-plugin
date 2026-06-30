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
import domain.RegistrarsGenerator;
import infrastructure.JVMHost;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generate-registrars")
public class GenerateRegistrarsMojo extends AbstractMojo {

  @Parameter(required = true)
  private String languageKey;

  @Parameter(required = true)
  private String targetDirectory;

  /**
   * Optional RSPEC branch name. When omitted, the plugin uses master unless rspecSha is set. If both
   * are provided, rspecSha takes precedence.
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
   * Optional RSPEC commit SHA used to pin registrar generation.
   */
  @Parameter(property = "rspec.sha")
  private String rspecSha;

  @Parameter(defaultValue = "Sonar way")
  private String profileName;

  @Parameter(required = true)
  private String packageName;

  @Parameter(required = true)
  private String compatibleLanguageKey;

  @Parameter(required = true)
  private String repositoryKey;

  @Override
  public void execute() throws MojoExecutionException {
    var host = new JVMHost();
    var logger = this.getLog();

    try {
      var generator = new RegistrarsGenerator(
        logger::info,
        ApplicationRuleRepository.createFromConfiguration(
          this.vcsBranchName,
          this.githubToken,
          this.rspecSha
        ),
        new ApplicationFileSystem(host)
      );

      generator.execute(
        this.packageName,
        this.languageKey,
        this.compatibleLanguageKey,
        this.repositoryKey,
        this.targetDirectory,
        this.profileName
      );
    } catch (IllegalArgumentException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    } catch (Exception e) {
      throw new MojoExecutionException(e);
    }
  }
}
