# Golf Course API

A REST API for a golf club's membership and tournament system, built with Spring Boot and talking to PostgreSQL through JPA/Hibernate.

Members register with the club, tournaments track who is participating, and the two are linked by a many-to-many relationship.

---

## Tech stack

| Piece | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 |
| Build | Maven (wrapper included, no install needed) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Docs | springdoc-openapi (Swagger UI) |
| Packaging | Jar, in a multi-stage Docker image |

---

## Quick start

You need Docker. Nothing else, not even Java or Maven.

```bash
git clone <your-repo-url>
cd golf-course-api
docker compose up --build
```

That starts two containers: `golf-postgres` and `golf-api`. Then open:

- Swagger UI: http://localhost:8080/swagger-ui.html
- All members: http://localhost:8080/api/members

The database is seeded on startup with 6 members and 4 tournaments, already registered against each other, so every endpoint returns something straight away.

To stop:

```bash
docker compose down      # keeps the data
docker compose down -v   # wipes the data too
```

### Running without Docker

Requires a PostgreSQL server on `localhost:5432` with database `golfclub`, user `golfadmin`, password `golfpassword`.

```bash
./mvnw spring-boot:run     # Mac / Linux
mvnw.cmd spring-boot:run   # Windows
```

---

## Design patterns used

The assignment title mentions design patterns but does not say which. These three are the ones this project demonstrates.

### 1. Repository pattern

`MemberRepository` and `TournamentRepository` are **interfaces**. No implementation is written anywhere. Spring Data JPA reads each method name and generates the SQL at runtime:

```java
List<Member> findByPhoneContaining(String phone);
```

becomes `SELECT * FROM members WHERE phone LIKE '%?%'`.

The database access is kept completely separate from the rest of the code, and swapping the database would not change a single service or controller.

### 2. Service layer

Every service is an **interface plus an implementation**:

```
MemberService          (interface: what it does)
MemberServiceImpl      (class: how it does it)
```

Controllers depend only on the interface, and Spring injects the implementation. Business rules (duplicate email checks, "end date cannot be before start date", "already registered") live in the service, never in the controller and never in the repository. Each layer has one job.

### 3. DTO pattern

Entities are never sent over HTTP. Requests come in as `MemberDto.Request`, responses go out as `MemberDto.Response`. Each entity's DTOs are nested in a single file (`MemberDto`, `TournamentDto`) to keep the file count down.

This is not decoration. It solves three real problems:

- **Infinite JSON loops.** A Member links to Tournaments, which link back to Members, forever. `MemberDto.Response` holds `TournamentDto.Summary` objects, and a summary holds no members, so serialisation always ends.
- **Lazy loading crashes.** Returning an entity with a lazily-loaded list after the transaction closes throws `LazyInitializationException`.
- **Clients cannot send an `id`.** `MemberDto.Request` has no id field, so nobody can pick their own primary key.

---

## Data model

**Member**: `id`, `name`, `address`, `email`, `phone`, `membershipStartDate`, `membershipDuration`, `membershipType`

**Tournament**: `id`, `startDate`, `endDate`, `location`, `entryFee`, `cashPrize`

**Relationship**: many-to-many through a join table called `member_tournament`, holding just `member_id` and `tournament_id`.

`Member` is the **owning side** (it declares the `@JoinTable`), `Tournament` is the inverse side (`mappedBy`). Only changes to the owning side get written to the join table, which is why registration goes through `Member.addTournament()`.

---

## API endpoints

Base URL: `http://localhost:8080`

### Members

| Method | Path | What it does |
|---|---|---|
| POST | `/api/members` | Add a member |
| GET | `/api/members` | Get all members |
| GET | `/api/members/{id}` | Get one member |

### Tournaments

| Method | Path | What it does |
|---|---|---|
| POST | `/api/tournaments` | Add a tournament |
| GET | `/api/tournaments` | Get all tournaments |
| GET | `/api/tournaments/{id}` | Get one tournament |

### Registration

| Method | Path | What it does |
|---|---|---|
| POST | `/api/tournaments/{tournamentId}/members/{memberId}` | Register a member into a tournament |

---

## Search endpoints and how to use them

All six required searches. Text searches are **partial and case-insensitive**, so searching `alice` finds both `Alice Morrison` and `Frank Alice Delgado`. Dates are `yyyy-MM-dd`.

### Search members by name

```
GET /api/members/search/by-name?name=alice
```
```bash
curl "http://localhost:8080/api/members/search/by-name?name=alice"
```

### Search members by membership type

```
GET /api/members/search/by-type?type=ANNUAL
```
Valid values: `ANNUAL`, `MONTHLY`, `LIFETIME`. Anything else returns a 400 listing the legal values.
```bash
curl "http://localhost:8080/api/members/search/by-type?type=ANNUAL"
```

### Search members by phone number

```
GET /api/members/search/by-phone?phone=782-555
```
Partial match, so `555` finds `902-555-0134`.
```bash
curl "http://localhost:8080/api/members/search/by-phone?phone=782-555"
```

### Search members by tournament start date

```
GET /api/members/search/by-tournament-date?startDate=2026-08-14
```

This is the interesting one. It finds every member participating in **any** tournament that starts on that date, walking across the many-to-many join. With the seed data, `2026-08-14` returns three members drawn from two different tournaments.

This is the only search that needed a query written out, because it has to join across the two tables:

```java
@Query("select distinct m from Member m join m.tournaments t where t.startDate = :startDate")
List<Member> findByTournamentStartDate(@Param("startDate") LocalDate startDate);
```

`join m.tournaments t` walks the many-to-many through the `member_tournament` table. `distinct` stops a member appearing twice if two of their tournaments start on the same day.

```bash
curl "http://localhost:8080/api/members/search/by-tournament-date?startDate=2026-08-14"
```

### Search tournaments by start date

```
GET /api/tournaments/search/by-start-date?startDate=2026-08-14
```
```bash
curl "http://localhost:8080/api/tournaments/search/by-start-date?startDate=2026-08-14"
```

### Search tournaments by location

```
GET /api/tournaments/search/by-location?location=glen
```
```bash
curl "http://localhost:8080/api/tournaments/search/by-location?location=glen"
```

---

## Example requests

### Add a member

```bash
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Grace Hopper",
    "address": "1 Navy Yard, Halifax NS",
    "email": "grace.hopper@example.com",
    "phone": "902-555-1906",
    "membershipStartDate": "2026-07-01",
    "membershipDuration": 12,
    "membershipType": "ANNUAL"
  }'
```

Returns `201 Created` with the saved member, including the id the database assigned.

### Add a tournament

```bash
curl -X POST http://localhost:8080/api/tournaments \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2026-11-01",
    "endDate": "2026-11-03",
    "location": "Cabot Links",
    "entryFee": 300.00,
    "cashPrize": 15000.00
  }'
```

### Register a member into a tournament

```bash
curl -X POST http://localhost:8080/api/tournaments/5/members/7
```

Returns the tournament with the member now in `participatingMembers`.

---

## Error handling

One `@RestControllerAdvice` class handles errors for every controller, so no controller contains a try/catch. Every failure returns the same JSON shape.

| Situation | Status |
|---|---|
| Id does not exist | 404 |
| Validation failed (blank name, bad email…) | 400 with a field-by-field breakdown |
| Duplicate email | 400 |
| End date before start date | 400 |
| Member already registered | 400 |
| Unknown enum or bad date format | 400 |
| Anything unexpected | 500, with no stack trace leaked |

Example of a validation failure:

```json
{
  "timestamp": "2026-07-27T15:33:52.32",
  "status": 400,
  "error": "Bad Request",
  "message": "One or more fields are invalid",
  "path": "/api/members",
  "fieldErrors": {
    "name": "name is required",
    "email": "email must be a valid address",
    "membershipDuration": "membershipDuration must be at least 1 month"
  }
}
```

---

## Configuration

Nothing is hardcoded. `application.properties` reads environment variables, with a local default after each colon:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/golfclub}
spring.datasource.username=${DB_USERNAME:golfadmin}
spring.datasource.password=${DB_PASSWORD:golfpassword}
```

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/golfclub` | JDBC connection string |
| `DB_USERNAME` | `golfadmin` | Database user |
| `DB_PASSWORD` | `golfpassword` | Database password |
| `SERVER_PORT` | `8080` | Port the API listens on |
| `SHOW_SQL` | `true` | Log the generated SQL |

**No credentials are committed to this repository.** The defaults are throwaway local development values.

---

## Connecting to AWS RDS

The whole point of the env-var setup above: moving from local Postgres to RDS is **only** an environment variable change. No code is edited, no file is changed, the same Docker image is used.

**1. Create the RDS instance**
- RDS → Create database → PostgreSQL
- Templates → Free tier
- DB instance identifier: `golf-club-db`
- Master username: `golfadmin`, set a master password
- Public access: **Yes** (needed to reach it from outside the VPC)
- Additional configuration → Initial database name: `golfclub`

**2. Open the security group**
- Find the instance's VPC security group → Inbound rules → Edit
- Add rule: Type `PostgreSQL`, Port `5432`, Source = your IP (or the EC2 security group)
- Do **not** open it to `0.0.0.0/0`

**3. Copy the endpoint** from the RDS console. It looks like
`golf-club-db.abc123xyz.us-east-1.rds.amazonaws.com`

**4. Point the app at it.** Set the three variables and run the identical image:

```bash
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://golf-club-db.abc123xyz.us-east-1.rds.amazonaws.com:5432/golfclub" \
  -e DB_USERNAME="golfadmin" \
  -e DB_PASSWORD="<your-rds-password>" \
  golf-course-api
```

On first connection Hibernate creates the tables and `data.sql` seeds them, exactly as it does locally.

**Tip:** if the connection hangs rather than failing fast, that is almost always the security group, not the app.

---

## Assumptions made

- **Database** is PostgreSQL.
- **`membershipDuration`** is a whole number of **months**, stored as an `int`.
- **`membershipType`** is an enum (`ANNUAL`, `MONTHLY`, `LIFETIME`), persisted with `EnumType.STRING` so the rows stay readable and reordering the enum cannot corrupt existing data.
- **Registration** is a plain many-to-many join. The link carries no extra fields (no registration date, no paid/unpaid flag).
- **Email is unique** across members. The assignment does not say so, but without it you cannot tell two members apart.
- **Text searches are partial and case-insensitive.** Requiring an exact full name match would make the search close to useless.
- **`ddl-auto=create-drop`** rebuilds the tables from the entity classes every time the app starts, so `data.sql` always loads into a clean database. That keeps the demo repeatable, but it does mean anything added through Postman is gone after a restart. A production system would use Flyway or Liquibase migrations instead.

---

## Assignment ambiguities, and how they were handled

**1. "Search Members by tournament start date" is listed under Member searches, but `startDate` belongs to Tournament.**

Taken to mean *find the members participating in a tournament that starts on date X*. Implemented as a join across the many-to-many rather than by copying a date onto Member. The seed data deliberately includes two tournaments sharing a start date so this endpoint demonstrably returns members from more than one tournament.

**2. The assignment is titled "Design Patterns" but the body does not name any.**

Three patterns were chosen and are documented above with the reasoning: Repository, Service layer, and DTO. Rather than adding patterns for their own sake, these were picked because each solves a concrete problem in this application.

**3. "Membership duration" has no stated unit.**

Assumed months, stored as an `int`. `LIFETIME` members are given a long duration in the seed data, since the type already carries the real meaning.

---

## Issues hit while building, and how they were handled

**Docker image had no ARM build.** The first Dockerfile used `eclipse-temurin:17-jre-alpine`, and the build failed on an Apple Silicon Mac with `no match for platform in manifest`. That tag has no arm64 variant. Fixed by switching to `eclipse-temurin:17-jre-jammy`, which is multi-architecture and therefore builds on both an M-series Mac and an x86 CI runner.

**No JDK 17 on the build machine.** The Mac had JDK 11, 21 and 26 but not 17. Rather than installing another JDK, the build targets Java 17 bytecode via `<java.version>17</java.version>` and compiles under JDK 21 locally. The Docker image uses a genuine JDK 17, so the artifact that actually ships is built on the right version regardless of what is installed on the host.

**`LazyInitializationException`.** The `tournaments` list on a Member is loaded lazily, so building the DTO after the database session had closed threw an exception. Fixed by putting `@Transactional` on every service method, which keeps the session open until the method finishes and the DTO is fully built.

**Infinite JSON recursion.** Serialising a Member walks to its Tournaments, which walk back to their Members, forever. Solved with the nested `Summary` records, which hold no back-reference, so the object graph always terminates.

**Seed data silently did not load.** After switching to `ddl-auto=create-drop`, every endpoint started returning an empty list and all six searches returned nothing, with no error anywhere in the logs. The cause was a missing `spring.sql.init.mode=always`. By default Spring only runs `data.sql` on in-memory databases such as H2, and skips it entirely on a real Postgres. Adding that one property fixed it. This one was hard to spot precisely because nothing failed, the tables were just empty.

**The many-to-many only saved from one side.** Adding a member to `tournament.getParticipatingMembers()` changed memory but wrote nothing to the database, because `Member` owns the join table. Fixed with `Member.addTournament()`, which updates both sides, and registration always goes through it.

**API started before the database was ready.** The API container initially crashed on startup because Postgres was still booting. Fixed with a `healthcheck` on the postgres service plus `depends_on: condition: service_healthy`, so Compose waits until Postgres genuinely accepts connections.

---

## What worked / what didn't

### What worked well

- **Env-var configuration from day one.** Because no credential was ever hardcoded, moving between local Postgres, Docker Compose and RDS never required a code change. This is the single decision that paid off most.
- **`docker compose up --build` really is clone-and-run.** No Java, no Maven, no manual database setup.
- **Derived query methods.** Five of the six required searches needed no query at all, just a method name like `findByPhoneContaining`. Spring Data works out the SQL from the name.
- **Centralised error handling.** One `@RestControllerAdvice` meant not a single try/catch in any controller, and every error returns an identical JSON shape.
- **Seeded data.** Having realistic data present at startup made every endpoint immediately demonstrable in Postman without setting anything up first.

### What didn't work first time

- The `alpine` Docker image, as described above, a genuine dead end that required changing base images.
- Seed data quietly failing to load, described above. Everything looked healthy, the containers were up, the endpoints answered, they were just empty.
- The relationship initially appeared to save correctly in tests and then silently lost registrations, because only the inverse side was being updated.

### What I'd do differently with more time

- **Flyway migrations** instead of `ddl-auto=create-drop`. Right now the database is wiped and rebuilt on every start, which is fine for a demo and useless for anything real.
- **Automated tests.** There are currently none. Verification was done manually with curl against the running container. `@DataJpaTest` for the repository searches and `@WebMvcTest` for the controllers would be the first additions.
- **Pagination** on the list endpoints. `GET /api/members` returns everything, which is fine for 6 rows and wrong for 6,000.
- **Update and delete endpoints.** Only add, retrieve, search and register were required, so only those were built.

---

## Project structure

16 Java files in total.

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

The layering is deliberate and is what demonstrates the three patterns: a request travels `controller → service → repository → database`, and the response travels back as DTOs. No layer ever reaches past the one below it.

**On the file count.** Each DTO file nests its `Request`, `Response` and `Summary` records rather than spreading them across six files, and the entity-to-DTO conversion lives in `from()` methods on those records rather than in a separate mapper class. Errors use a single `ApiException` carrying its own status code instead of one exception class per status. The trade-off: `ApiException` knows about `HttpStatus`, so the service layer is fractionally less HTTP-agnostic than a purist design would be. That was judged worth it for the reduction in files.
