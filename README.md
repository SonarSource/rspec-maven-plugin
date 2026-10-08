<!-- Sonar Marketing hosts these approved brand assets on its Kentico Kontent CDN (assets-eu-01.kc-usercontent.com). Shared URLs are intentional; consult Marketing before replacing them. -->
<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://assets-eu-01.kc-usercontent.com/ef593040-b591-0198-9506-ed88b30bc023/a23fc7ba-23f0-489a-829d-ed88c0748521/Sonar_Logo_Dark%20Backgrounds.svg">
    <img src="https://assets-eu-01.kc-usercontent.com/ef593040-b591-0198-9506-ed88b30bc023/82c13eba-d95c-4bb8-8007-7ce77c14e043/Sonar_Logo_Light%20Backgrounds.svg" alt="Sonar logo" width="400">
  </picture>
</p>

<!-- sonar-marketing:start -->
<!-- Marketing maintains this section. For wording changes, consult the relevant Product Marketing Manager (PMM). Repository maintainers review accuracy and merge changes. -->

# RSPEC Maven plugin

This Maven plugin populates analysis-rule metadata from the Sonar rule specifications (RSPEC). It is build tooling for analyzer developers; the configuration below explains how to pin the rule-specification revision used by a build.

To learn more about the SonarQube product family, visit the [Sonar website](https://www.sonarsource.com/products/sonarqube/).

<!-- sonar-marketing:end -->

Set `rspec.sha` to pin generated data to a specific RSPEC commit. This makes generated data reproducible and allows
builds to use a previous RSPEC revision when the current RSPEC state is not desired.

```xml
<configuration>
    <rspecSha>0123456789abcdef0123456789abcdef01234567</rspecSha>
</configuration>
```

It can also be provided from the command line with `-Drspec.sha=<commit-sha>`.
If both `rspecSha` and `vcsBranchName` are set, `rspecSha` takes precedence.
