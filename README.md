# rspec-maven-plugin

RSPEC Maven Plugin Project

Set `rspec.sha` to pin generated data to a specific RSPEC commit. This makes generated data reproducible and allows
builds to use a previous RSPEC revision when the current RSPEC state is not desired.

```xml
<configuration>
    <vcsBranchName>dogfood-automerge</vcsBranchName>
    <rspecSha>0123456789abcdef0123456789abcdef01234567</rspecSha>
</configuration>
```

It can also be provided from the command line with `-Drspec.sha=<commit-sha>`.
When `rspec.sha` is set, it takes precedence over `vcsBranchName`.
