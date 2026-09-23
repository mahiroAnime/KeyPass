# KeyPass — Fabric 1.21.7

Client-side Fabric mod for Minecraft 1.21.7.

## Build on Windows
1. Install Java 21.
2. Extract this ZIP.
3. Run `build.bat`.
4. The first run downloads Gradle 8.14.3. The launcher tries the faster Gradle CDN first and automatically falls back to the standard server.
5. The finished mod JAR appears in `build\libs`.

The project already includes the Mojang Libraries repository needed for Minecraft dependencies.

## Build troubleshooting
If an older copy of this project produced dependency names beginning with `net_fabricmc_yarn_...`, use this package as-is. Its Gradle settings use the standard Fabric Loom repository configuration and do not force project dependency repositories through `dependencyResolutionManagement`.
