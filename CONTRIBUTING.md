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
* API design should follow
  * [RESTful API design](https://restfulapi.net/).
  * [Microsoft Azure REST API Guidelines](https://github.com/microsoft/api-guidelines/blob/vNext/azure/Guidelines.md)

## Development Requirement

### Library Usage

* Spring family is the first choice. Whenever a functionality can be achieved by Spring, it should be used. ** No reinventing the wheel.**

### Non-Functional Requirement

* All exceptional behaviour shall be logged with sufficient details.
* Feign client should think of holding its own data model, rather than directly using the data model from the service it calls.

### Code commit

* Commit message should follow [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/)
* [Github pipeline](https://github.com/hugogu/rt-balance/actions) MUST success before merging.

## Roadmap items to be built

* A frontend portal to view & make transactions.
* Use of [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) to manage API gateway with Authentication & Authorization.
* Use of [Spring Cloud Config](https://spring.io/projects/spring-cloud-config) to manage configurations.
* Replace JSON APIs with [gRPC](https://grpc.io/) with protobuf.

