# Golf Course API

A REST API for a golf club's membership and tournament system. Built with Spring Boot,
using JPA and Hibernate to talk to a PostgreSQL database.

Members join the club, tournaments track who is signed up, and the two are linked with a
many to many relationship through a join table called member_tournament.

## How to run it in Docker

You only need Docker. You do not need Java or Maven installed.

```bash
git clone https://github.com/Britten66/golf-course-api.git
cd golf-course-api
docker compose up --build
```

That starts two containers, golf-postgres and golf-api. Then open:

- Swagger UI: http://localhost:8080/swagger-ui.html
- All members: http://localhost:8080/api/members

The database gets seeded with 6 members and 4 tournaments when the app starts, and some of
them are already registered for tournaments, so every endpoint returns something right away.

To stop it:

```bash
docker compose down
```

## API endpoints

Add and get members:

```
POST /api/members
GET  /api/members
GET  /api/members/{id}
```

Add and get tournaments:

```
POST /api/tournaments
GET  /api/tournaments
GET  /api/tournaments/{id}
```

Register a member into a tournament:

```
POST /api/tournaments/{tournamentId}/members/{memberId}
```

## Search endpoints and how to use them

All of these are GET requests. Text searches match part of the value and ignore upper or
lower case, so searching for "alice" finds both Alice Morrison and Frank Alice Delgado.
Dates are in yyyy-MM-dd format.

```
GET /api/members/search/by-name?name=alice
GET /api/members/search/by-type?type=ANNUAL
GET /api/members/search/by-phone?phone=782
GET /api/members/search/by-tournament-date?startDate=2026-08-14
GET /api/tournaments/search/by-start-date?startDate=2026-08-14
GET /api/tournaments/search/by-location?location=glen
```

For the membership type search the value has to be ANNUAL, MONTHLY or LIFETIME. Anything
else gives back a 400.

The one that took the most thinking was searching members by tournament start date. Start
date belongs to Tournament, not Member, so it has to join across the many to many. I wrote
that query out instead of using a method name:

```java
@Query("select distinct m from Member m join m.tournaments t where t.startDate = :startDate")
List<Member> findByTournamentStartDate(@Param("startDate") LocalDate startDate);
```

The seed data has two tournaments starting on 2026-08-14 on purpose, so that search returns
members from two different tournaments and you can actually see it working.

## How I connected to RDS

I made a free tier PostgreSQL instance in RDS called golf-club-db. The settings that
mattered were Public access set to Yes, and the initial database name set to golfclub. I
also made a new security group called golf-db-sg with an inbound rule allowing port 5432
from my IP.

I did not have to change any code to connect to it. The datasource in
application.properties reads environment variables with a local default after the colon:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/golfclub}
spring.datasource.username=${DB_USERNAME:golfadmin}
spring.datasource.password=${DB_PASSWORD:golfpassword}
```

So I ran the exact same Docker image and just passed in different values:

```bash
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://your-endpoint.rds.amazonaws.com:5432/golfclub?sslmode=require" \
  -e DB_USERNAME="golfadmin" \
  -e DB_PASSWORD="your-password" \
  -e DDL_AUTO="update" \
  golf-course-api-api
```

The sslmode=require part is needed because RDS will not accept a connection that is not
encrypted. DDL_AUTO=update is there so Hibernate leaves the tables alone when the container
stops. Locally it is set to create-drop so the demo data resets every run.

I checked it worked two ways. The API returned all 6 seeded members while pointed at RDS,
and connecting with psql showed the three tables that Hibernate had created in RDS:

```
public | member_tournament | table | golfadmin
public | members           | table | golfadmin
public | tournaments       | table | golfadmin
```

Screenshots of the RDS setup and the connection are in the screenshots folder.

## Problems I ran into

The Docker image would not build. My first Dockerfile used
eclipse-temurin:17-jre-alpine and the build failed with "no match for platform in
manifest" because that tag has no arm64 version and I am on an Apple Silicon Mac. I
switched to eclipse-temurin:17-jre-jammy which is multi architecture.

No JDK 17 on my machine. I had 11, 21 and 26 installed but not 17. Instead of installing
another JDK I set java.version to 17 in the pom so it targets Java 17 bytecode, and the
Docker build uses a real JDK 17 image, so the jar that actually ships is built correctly.

LazyInitializationException. The tournaments list on a Member loads lazily, so building the
response object after the database session closed threw an error. I put @Transactional on
the service methods so the session stays open until the method finishes.

Seed data silently did not load. After I switched to create-drop, every endpoint started
returning an empty list and there was no error anywhere in the logs. The cause was a missing
spring.sql.init.mode=always. By default Spring only runs data.sql on in memory databases
like H2 and skips it on a real Postgres. That one was hard to find because nothing failed,
the tables were just empty.

The RDS certificate download gave me a file that was not a certificate. The AWS docs URL I
used returned a 111 byte AccessDenied page instead, and curl saved it without complaining
because I did not use -L. psql then said "no certificate or crl found". The path that
actually works is /global/global-bundle.pem.

Password authentication failed on the first RDS connection. I reset the master password in
the RDS console under Modify and applied it immediately, which fixed it.

RDS gave me PostgreSQL 18 but my local psql client is 15. Running \l failed with "column
d.daticulocale does not exist" because the newer server renamed that column. I used a plain
SELECT query instead of the psql shortcut.

The golfclub database did not exist. I missed the Initial database name field when creating
the instance, so I had to connect to the default postgres database and run
CREATE DATABASE golfclub by hand.

## Assumptions

- The database is PostgreSQL.
- Membership duration is a whole number of months, stored as an int.
- Membership type is an enum saved as text, so the rows are readable and reordering the enum
  cannot break existing data.
- Registering a member into a tournament is a plain join with no extra fields on it.
- Email has to be unique across members, otherwise you cannot tell two members apart.
- Searches match partial text and ignore case, because needing an exact full name would make
  the search close to useless.

## Project structure

```
src/main/java/com/golfclub/api/
├── GolfCourseApiApplication.java
├── controller/
│   ├── MemberController.java
│   └── TournamentController.java
├── service/
│   ├── MemberService.java
│   ├── TournamentService.java
│   └── impl/
│       ├── MemberServiceImpl.java
│       └── TournamentServiceImpl.java
├── repository/
│   ├── MemberRepository.java
│   └── TournamentRepository.java
├── domain/
│   ├── Member.java
│   ├── Tournament.java
│   └── MembershipType.java
├── dto/
│   ├── MemberDto.java
│   └── TournamentDto.java
└── exception/
    ├── ApiException.java
    └── GlobalExceptionHandler.java
```

A request goes controller, then service, then repository, then the database. The response
comes back as a DTO instead of the entity itself, which stops the JSON looping from Member
to Tournament and back forever.

The design patterns in here are the Repository pattern for the Spring Data interfaces, a
service layer with an interface and an implementation, and DTOs so the entities never get
sent over HTTP.
