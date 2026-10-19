# Development

Run commands from the checkout with Java 25. Use `heavy` for Gradle and check capacity first:

```sh
heavy status
heavy --mem 6G -- ./gradlew --no-daemon build runAllGameTests \
  -Pfabricmoddingconventions.devClientOptions=/nonexistent
```

The client-only mod has unit tests in `fabric/src/test` and a Fabric production client GameTest in `fabric/src/gametest`. The client scenario starts and stops its own dedicated-server fixture. NeoForge server GameTests are disabled; `build` still compiles and packages both loaders.

## Headless recording

The conventions recorder needs Xvfb, `xdpyinfo`, `pactl`, and ffmpeg with X11 capture, PulseAudio input, H.264 and AAC support. On NixOS, use `ffmpeg-full`, not the smaller ffmpeg package without X11 capture:

```sh
heavy status
nix shell nixpkgs#ffmpeg-full nixpkgs#xdpyinfo --command \
  heavy --mem 6G -- env GTR_RECORDING_PROFILE=showcase \
  ./gradlew --no-daemon :fabric:recordClientGameTest \
  -Pfabricmoddingconventions.devClientOptions=/nonexistent
```

Use a private PulseAudio session through `PULSE_SERVER` on a shared host. The recorder creates its own Xvfb display and audio sink without changing the desktop's default sink. Recordings use a 1920x1080 window at GUI scale 2; the fixture temporarily positions HUDs clear of the title card and restores their settings afterwards. The lighting scene temporarily hides the Reach HUD and waypoint labels so its brightness measurement samples terrain, not overlaid text. Large diagnostic screenshots run only outside recordings so resizing does not crop the video.

MP4s, metadata and kept run directories are written to `build/recordings`. `GTR_RECORDING_DIR` selects a different output directory.
