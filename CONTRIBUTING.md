# Contribution guidelines

## Overall Design

* Each service module is designed to be independent and can be run separately.
* Service module following microservice architecture principles, which means:
  * Exclusively obsess a database. (The DB instance may be shared though.)
  * Communicate with other services via RESTful APIs.
    * RPC like protocol is also acceptable in performance critical scenarios.
  * Follows [12-factor app principles](https://12factor.net/).

## Design Requirement

* Whenever eventually consistency is acceptable, prevent distributed lock which is usually a performance bottleneck.
* Each external dependency should report its health status via `/actuator/health` endpoint. 
  Check [Liveness and Readiness Probes with Spring Boot](https://spring.io/blog/2020/03/25/liveness-and-readiness-probes-with-spring-boot) for more details.

## Code commit

* Commit message should follow [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/)
* [Github pipeline](https://github.com/hugogu/rt-balance/actions) MUST success before merging.


