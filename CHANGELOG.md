# Changelog

## [1.7.1](https://github.com/Tugamer89/autogex/compare/v1.7.0...v1.7.1) (2026-05-25)


### Bug Fixes

* **security:** [HIGH] resolve ReDoS vulnerability in DFA conversion ([#94](https://github.com/Tugamer89/autogex/issues/94)) ([2de9ee9](https://github.com/Tugamer89/autogex/commit/2de9ee97281883e171158fe3f3a492b897675562))
* **ui:** improve Javadoc header accessibility and UX ([#95](https://github.com/Tugamer89/autogex/issues/95)) ([1f80e0f](https://github.com/Tugamer89/autogex/commit/1f80e0f5e7301520811faa08ebc8fe5bb3cd4c19))

## [1.7.0](https://github.com/Tugamer89/autogex/compare/v1.6.0...v1.7.0) (2026-05-24)


### Features

* **ui:** improve javadoc header ux and accessibility ([#77](https://github.com/Tugamer89/autogex/issues/77)) ([f99ccc1](https://github.com/Tugamer89/autogex/commit/f99ccc14192b0a190fa8da0fe5745b04ca7effde))


### Bug Fixes

* **exporter:** escape HTML and newlines in GraphvizExporter to prevent XSS ([#87](https://github.com/Tugamer89/autogex/issues/87)) ([20c79c8](https://github.com/Tugamer89/autogex/commit/20c79c8b5e088884b81069b4d295f426889a352a))
* **exporter:** prevent ReDoS vulnerability in MermaidExporter sanitization ([#86](https://github.com/Tugamer89/autogex/issues/86)) ([40b3981](https://github.com/Tugamer89/autogex/commit/40b3981adabdd4d1efa325db4a46a56334aec107))
* **security:** [HIGH] resolve XSS in MermaidExporter ([#78](https://github.com/Tugamer89/autogex/issues/78)) ([2ac78ba](https://github.com/Tugamer89/autogex/commit/2ac78ba9d09d1297bb3595d873ada96e9b18880d))


### Performance Improvements

* optimize DFA Minimizer partition lookup with O(1) mapping ([#79](https://github.com/Tugamer89/autogex/issues/79)) ([c214eca](https://github.com/Tugamer89/autogex/commit/c214eca5d5af6d36d6b40d23eede2aa2e94429c8))

## [1.6.0](https://github.com/Tugamer89/autogex/compare/v1.5.0...v1.6.0) (2026-05-23)


### Features

* **ui:** add loading state to documentation version switcher ([#73](https://github.com/Tugamer89/autogex/issues/73)) ([d8dc14a](https://github.com/Tugamer89/autogex/commit/d8dc14a4e810be40dc959da7c675e9b8612fe66e))


### Bug Fixes

* **security:** resolve potential DoS vulnerability in RegexParser ([#75](https://github.com/Tugamer89/autogex/issues/75)) ([60cabb2](https://github.com/Tugamer89/autogex/commit/60cabb21cf33b72eb95a4e4e6538b7ca529fff4b))


### Performance Improvements

* optimize checking final states using Collections.disjoint ([#74](https://github.com/Tugamer89/autogex/issues/74)) ([c93a202](https://github.com/Tugamer89/autogex/commit/c93a20275461e8d7070cd83a994c656258e8278b))

## [1.5.0](https://github.com/Tugamer89/autogex/compare/v1.4.0...v1.5.0) (2026-04-26)


### Features

* **regex:** implement advanced syntax operators (+, ?, ., []) ([#63](https://github.com/Tugamer89/autogex/issues/63)) ([fd7973e](https://github.com/Tugamer89/autogex/commit/fd7973e8b8e2aa318448e32e73ce2249c09206d7))


### Bug Fixes

* fixed build badge link ([9369951](https://github.com/Tugamer89/autogex/commit/9369951a70fae4116ba7b2436c31fbaa9711cc03))
* fixed sonarcloud error java:S7158 ([0c2ca18](https://github.com/Tugamer89/autogex/commit/0c2ca1873a62c53414402c4480eab7144358e50a))

## [1.4.0](https://github.com/Tugamer89/autogex/compare/v1.3.12...v1.4.0) (2026-04-22)


### Features

* enhance test suite, coverage checks and module visibility ([5758fb9](https://github.com/Tugamer89/autogex/commit/5758fb91069449095e0deb2598727e4b6e13471a))


### Bug Fixes

* **javadoc:** resolve named and unnamed modules conflict ([003b603](https://github.com/Tugamer89/autogex/commit/003b60391f3b31e75126ed12ff9c008a05ab759e))

## [1.3.12](https://github.com/Tugamer89/autogex/compare/v1.3.11...v1.3.12) (2026-04-21)


### Bug Fixes

* added concurrency options to release please ([b4826f4](https://github.com/Tugamer89/autogex/commit/b4826f48d093a20796c6c345f1dca4e7a41bd9e2))
* release description ([a7847dc](https://github.com/Tugamer89/autogex/commit/a7847dc15ea02f700e0f46f61e26484d25cf8513))

## [1.3.11](https://github.com/Tugamer89/autogex/compare/v1.3.10...v1.3.11) (2026-04-21)


### Bug Fixes

* changed dev option position & improved readme sync ([1c4d0cf](https://github.com/Tugamer89/autogex/commit/1c4d0cf44b601d11289175eaafdce2735118dbba))
* improved spotless trigger ([cef79ea](https://github.com/Tugamer89/autogex/commit/cef79ea4b2f50d7756dc1ec8a8dbd6cff75b3779))
* release phase & readme sync ([12c181b](https://github.com/Tugamer89/autogex/commit/12c181bbb0141986910560ff519518a260b84487))
* var name ([3734997](https://github.com/Tugamer89/autogex/commit/3734997c16a30a7ff1ccb8d09566f5b4f9b903c6))
* version gathering ([ae9ab3e](https://github.com/Tugamer89/autogex/commit/ae9ab3e2408d5c486d766447eaa16acfe5fa3cbc))
* workflow step name ([3b66d58](https://github.com/Tugamer89/autogex/commit/3b66d58af9d6617eaa9beb1745723d2ad2c1f8c9))

## [1.3.10](https://github.com/Tugamer89/autogex/compare/v1.3.9...v1.3.10) (2026-04-21)


### Bug Fixes

* added Github App tokens ([39ccf8c](https://github.com/Tugamer89/autogex/commit/39ccf8cc47ea9ca33c131d065fed69e2e06cbc59))
* improved CI/CD workflows triggers ([f2a03fd](https://github.com/Tugamer89/autogex/commit/f2a03fdbb68ecde9c1bf050c626d81d730532f61))

## [1.3.9](https://github.com/Tugamer89/autogex/compare/v1.3.8...v1.3.9) (2026-04-21)


### Bug Fixes

* automated CI tests start ([972fd64](https://github.com/Tugamer89/autogex/commit/972fd6453599a3744a62471361dd1e859e1c2262))
* release name and triggers ([a378033](https://github.com/Tugamer89/autogex/commit/a378033fa33475b779175245e69950cf5a54da96))

## [1.3.8](https://github.com/Tugamer89/autogex/compare/v1.3.7...v1.3.8) (2026-04-21)


### Bug Fixes

* `build.yml` path-ignore ([433a8cf](https://github.com/Tugamer89/autogex/commit/433a8cf07d5dae4703959651d4fae0b351054a58))
* release please settings ([fbfe734](https://github.com/Tugamer89/autogex/commit/fbfe734f234b4758d3be069f723d4f1faf04c0e4))
* released please config ([656dda0](https://github.com/Tugamer89/autogex/commit/656dda0a11a0dce8032d34cd12c8cb6440fb50fe))
* spotless format ([663e198](https://github.com/Tugamer89/autogex/commit/663e198ddb175e9597b6df12382f1876bf4dc6a7))
* translated to english some comments ([7b4bcb7](https://github.com/Tugamer89/autogex/commit/7b4bcb7c61ed7db8a60c75c89d876c31b1f46fb0))
