/*
 * RSPEC Maven Plugin
 * Copyright (C) 2025-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

  @Parameter(defaultValue = "master")
  private String vcsBranchName;

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
        new ApplicationRuleRepository(this.vcsBranchName),
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
    } catch (Exception e) {
      throw new MojoExecutionException(e);
    }
  }
}
