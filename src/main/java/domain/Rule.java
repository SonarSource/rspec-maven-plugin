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
package domain;

import java.util.List;

public interface Rule {
  String name();

  String htmlDocumentation();

  String type();

  String defaultSeverity();

  List<String> tags();

  String scope();

  Remediation remediation();

  String title();

  List<Parameter> parameters();

  List<String> compatibleLanguages();

  List<String> qualityProfiles();

  String status();

  Code code();
}
