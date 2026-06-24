# qa-automation

Selenium Grid test automation for the `shopping-cart` application.
Runs via Jenkins Job B (`run-qa-tests`), which is triggered either by:
- The upstream `build-shopping-cart` job, on a successful build
- A nightly cron schedule (`H 2 * * *`), independent of pushes

## What this spins up
`docker-compose.yml` brings up, on a shared Docker network:
- `shopping-cart-app` — the app under test (image tag from `APP_TAG`, defaults to `latest`)
- `selenium-hub` — Selenium Grid 4 hub
- `chrome` / `firefox` — browser nodes registered to the hub

Tests run from outside the compose network via Maven, pointing at the hub
on `localhost:4444`, with the app addressed as `http://shopping-cart-app:8080`
(resolvable because the browser nodes are on the same Docker network).

## Local run
```
docker-compose up -d
# wait for grid + app to be ready, then:
mvn clean test -Dgrid.url=http://localhost:4444/wd/hub -Dapp.url=http://shopping-cart-app:8080
docker-compose down -v
```

## Test reliability features included
- `RetryAnalyzer.java` — retries a failing test once before marking it failed,
  to absorb normal Selenium flakiness (timing, element-not-ready, etc.)
- Screenshot-on-failure — saved to `target/screenshots/`, archived as Jenkins
  build artifacts

## Known gaps not yet addressed (by design, deferred)
- Browser image versions are not pinned beyond the major release — they can
  drift over time as `selenium/node-chrome:4.21.0` etc. get rebuilt upstream
- No separate "staging URL" job — this suite currently always tests a
  freshly-built container, not a deployed environment
- Resource limits aren't set on Selenium nodes; on constrained machines,
  running both browser nodes simultaneously can hit memory limits

## Jenkins setup required
- Maven tool named `Maven3` configured in Global Tool Configuration
- JDK tool named `JDK17` configured in Global Tool Configuration
- Docker must be accessible to the Jenkins agent
- GitHub credentials configured in Jenkins if this repo is private
- This job must be named exactly `run-qa-tests` for the upstream trigger
  from `shopping-cart`'s Jenkinsfile to work
