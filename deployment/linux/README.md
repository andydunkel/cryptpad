# Linux AppImage deployment

The AppImage contains the native Linux release binaries, translations, updater
configuration and the GTK runtime files. The Linux updater only checks for new
releases; users download and replace the AppImage themselves.

Build from the repository root:

```bash
deployment/linux/build-appimage.sh
```

The version defaults to the `version`, `subversion` and `bugfix` values in
`cryptpad_laz/out/updater.ini`. It can be overridden for a packaging test:

```bash
APP_VERSION=1.2.1 deployment/linux/build-appimage.sh
```

Generated files are written to `dist/`. Packaging tools are cached below
`build/appimage/tools/`; neither directory is committed.

## Release checklist

1. Update the version manually in `cryptpad_laz/out/updater.ini`, the Lazarus
   project settings and the `<release>` entry in the AppStream metadata.
2. Place the matching executable Linux updater at `cryptpad_laz/out/updater`.
3. Run `deployment/linux/build-appimage.sh` from the repository root.
4. Verify the generated checksum from the output directory:

   ```bash
   cd dist
   sha256sum -c DA-CryptPad-<version>-x86_64.AppImage.sha256
   ```

The updater is packaged beside the main executable. This is intentional: both
programs resolve `updater.ini`, `updlang.ini` and `lang/` relative to their
executable directory.

For public releases, run this build on the oldest supported Linux base system
and test the resulting AppImage on every supported distribution.
