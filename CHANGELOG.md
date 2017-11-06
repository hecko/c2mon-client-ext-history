# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

All issues referenced in parentheses can be consulted under [CERN GitLab](https://gitlab.cern.ch/c2mon/c2mon/issues).
For more details on a given release, please check also the [Milestone planning](https://gitlab.cern.ch/c2mon/c2mon/milestones?state=all).

## [Unreleased]
### Added
- Added support for `ServerSupervisionEvent` browsing (c2mon-web-ui#16)

### Changed

### Fixed


## [1.8.6] - 2017-08-03
### Added
- Alarm history queries to search by fault family, member and code (#8)

### Fixed
- Fixed bug in helper method to convert from LocalDateTime to Timestamp

## [1.8.5] - 2017-07-18
### Fixed
- Fixed all `AlarmHistoryService` queries by only using JPA (#6)


[Unreleased]: %4
[1.8.6]: %3
[1.8.5]: %2
