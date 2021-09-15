# bnd-jar-maven-plugin

replaces the `jar:jar` and `jar:test-jar` goals of the `maven-jar-plugin`. This new `bnd-jar-maven-plugin` is a full replacement for `maven-jar-plugin` and gives Bnd full control over the making of the jar while still allowing other maven plugins which expect `maven-jar-plugin` behavior still properly function.

Additional Features

- generate OSGi compatible manifest
 - full osgi annotation analysing support.
- generate multiple jars e.g. tests or further sub-build
- generate META-INF/services/* via bnd Annotations
 - TestCases bundle Header
- optional generate module.info
- jar signing
- bnd plugin/exporter/generator? support
