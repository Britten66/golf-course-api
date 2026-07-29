# Golf Course API

A REST API for a golf club's membership and tournament system, built with Spring Boot and talking to PostgreSQL through JPA/Hibernate.

Members register with the club, tournaments track who is participating, and the two are linked by a many-to-many relationship.

##USER STEPS
```bash
git clone
cd golf-course-api
docker compose up --build
```

That starts two containers: `golf-postgres` and `golf-api`. Then open:

- Swagger UI: http://localhost:8080/swagger-ui.html
- All members: http://localhost:8080/api/members

The database is seeded on startup with 6 members and 4 tournaments, already registered against each other, so every endpoint returns something straight away.

 here are the endpoints 
```
 
GET /api/members/search/by-name?name=alice
GET /api/members/search/by-type?type=ANNUAL
GET /api/members/search/by-phone?phone=782
GET /api/members/search/by-tournament-date?startDate=2026-08-14
GET /api/tournaments/search/by-start-date?startDate=2026-08-14
GET /api/tournaments/search/by-location?location=glen

```

## Issues hit while building, and how they were handled

**Docker image had no ARM build.** The first Dockerfile used `eclipse-temurin:17-jre-alpine`, and the build failed on an Apple Silicon Mac with `no match for platform in manifest`. That tag has no arm64 variant. Fixed by switching to `eclipse-temurin:17-jre-jammy`, which is multi-architecture and therefore builds on both an M-series Mac and an x86 CI runner.

**No JDK 17 on the build machine.** The Mac had JDK 11, 21 and 26 but not 17. Rather than installing another JDK, the build targets Java 17 bytecode via `<java.version>17</java.version>` and compiles under JDK 21 locally. The Docker image uses a genuine JDK 17, so the artifact that actually ships is built on the right version regardless of what is installed on the host.

**`LazyInitializationException`.** The `tournaments` list on a Member is loaded lazily, so building the DTO after the database session had closed threw an exception. Fixed by putting `@Transactional` on every service method, which keeps the session open until the method finishes and the DTO is fully built.

## Project structure
```
src/main/java/com/golfclub/api/
├── GolfCourseApiApplication.java
├── controller/
│   ├── MemberController.java          REST endpoints, HTTP only
│   └── TournamentController.java
├── service/
│   ├── MemberService.java             interfaces
│   ├── TournamentService.java
│   └── impl/
│       ├── MemberServiceImpl.java     business rules
│       └── TournamentServiceImpl.java
├── repository/
│   ├── MemberRepository.java          Spring Data JPA interfaces
│   └── TournamentRepository.java
├── domain/
│   ├── Member.java                    @Entity classes
│   ├── Tournament.java
│   └── MembershipType.java
├── dto/
│   ├── MemberDto.java                 Request/Response/Summary nested
│   └── TournamentDto.java             in one file per entity
└── exception/
    ├── ApiException.java              carries its own status code
    └── GlobalExceptionHandler.java    every error, one place
```
