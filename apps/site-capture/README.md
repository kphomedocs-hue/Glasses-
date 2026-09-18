# Site Capture App

Future production application.

Legacy reference implementation:
- `archive/site-capture/v0.4/`

Target behavior:
- glasses act as capture source,
- phone silently imports new media,
- one chronological daily counter across supported media,
- preserve capture timestamps,
- never overwrite originals,
- robust dedup/retry/integrity verification,
- minimal UI.

Development should consume modular components under `modules/` rather than reviving the legacy monolithic transport directly.
