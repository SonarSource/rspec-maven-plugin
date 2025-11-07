/*
 * RSPEC Maven Plugin
 * Copyright (C) 2025-2025 SonarSource Sàrl
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

public class ApplicationFileSystem implements domain.FileSystem {

  private final Host host;

  public ApplicationFileSystem(Host host) {
    this.host = host;
  }

  public String resolve(String first, String... more) {
    return this.host.resolve(first, more);
  }

  public void write(String filePath, String content) throws Exception {
    try {
      this.host.write(filePath, content);
    } catch (IOException e) {
      throw new Exception();
    }
  }
}
