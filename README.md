# Golf Course API

A Spring Boot REST API for a golf club's membership and tournament system.

It keeps track of members and the tournaments they are signed up for.

A member can be in more than one tournament and a tournament can have more than one member.

## Built With

Java 17, Spring Boot, PostgreSQL and Docker.

## How It Works

The project uses a layered setup.

The models are the Member and Tournament classes.

They are linked many to many through a join table called member_tournament.

The repositories talk to the database.

The services hold the logic and the rules.

The controllers handle the web requests at /api/members and /api/tournaments.

Requests and responses use DTO classes instead of the entities. If I sent the entity straight
back the JSON would go Member to Tournament to Member and never stop.

## Endpoints

POST /api/members adds a member.

GET /api/members returns all members.

GET /api/members/{id} returns one member by its id.

POST /api/tournaments adds a tournament.

GET /api/tournaments returns all tournaments.

GET /api/tournaments/{id} returns one tournament by its id.

POST /api/tournaments/{tournamentId}/members/{memberId} registers a member into a tournament.

## Search Endpoints

These are all GET requests.

Text searches match part of the value and ignore capitals. Searching alice finds both Alice
Morrison and Frank Alice Delgado.

Dates go in as yyyy-MM-dd.

GET /api/members/search/by-name?name=alice

GET /api/members/search/by-type?type=ANNUAL

GET /api/members/search/by-phone?phone=782

GET /api/members/search/by-tournament-date?startDate=2026-08-14

GET /api/tournaments/search/by-start-date?startDate=2026-08-14

GET /api/tournaments/search/by-location?location=glen

The type has to be ANNUAL, MONTHLY or LIFETIME. Anything else gives back a 400.

Searching members by tournament start date was the tricky one. Start date is on Tournament and
not on Member, so the query has to join across the two tables. I wrote that one out instead of
using a method name.

The seed data has two tournaments starting on 2026-08-14 on purpose, so that search comes back
with members from two different tournaments.

## Running It In Docker

You only need Docker. Java and Maven are not needed.

```bash
git clone https://github.com/Britten66/golf-course-api.git
cd golf-course-api
docker compose up --build
```

That starts two containers, golf-postgres and golf-api.

Then open http://localhost:8080/api/members

The Swagger page is at http://localhost:8080/swagger-ui.html

The database gets seeded with 6 members and 4 tournaments when the app starts, and some of them
are already registered, so every endpoint returns something right away.

docker compose down stops it.

## Connecting To RDS

I made a free tier PostgreSQL instance in RDS called golf-club-db.

The two settings that mattered were Public access set to Yes and the initial database name set
to golfclub.

I also made a security group called golf-db-sg with an inbound rule for port 5432 from my IP.

No code changed to point it at RDS. The datasource reads environment variables and falls back
to a local default after the colon.

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/golfclub}
spring.datasource.username=${DB_USERNAME:golfadmin}
spring.datasource.password=${DB_PASSWORD:golfpassword}
```

So I ran the same Docker image with different values passed in.

```bash
read -s "?RDS password: " PGPW

docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://golf-club-db.cc9a4kg4ilu8.us-east-1.rds.amazonaws.com:5432/golfclub?sslmode=require" \
  -e DB_USERNAME="golfadmin" \
  -e DB_PASSWORD="$PGPW" \
  -e DDL_AUTO="update" \
  golf-course-api-api
```

I read the password into a variable first so it does not sit in my shell history or in this file.

sslmode=require is needed because RDS will not take a connection that is not encrypted.

DDL_AUTO=update stops Hibernate dropping the tables when the container stops. Locally it is set
to create-drop so the demo data resets every run.

I checked it two ways. The API returned all 6 seeded members while it was pointed at RDS, and
connecting with psql showed the three tables Hibernate had made in RDS.

Screenshots of the RDS setup and the connection are in the screenshots folder.

## Problems I Ran Into

The Docker image would not build. I used eclipse-temurin:17-jre-alpine and it failed with no
match for platform in manifest. That tag has no arm64 build and I am on an Apple Silicon Mac.
I switched to 17-jre-jammy which works on both.

I did not have JDK 17 installed. I had 11, 21 and 26. Instead of installing another one I set
java.version to 17 in the pom, and the Docker build uses a real JDK 17 image, so the jar that
ships is built on the right version.

LazyInitializationException. The tournaments list on a Member loads lazily and the database
session was already closed by the time the response was being built. Putting @Transactional on
the service methods fixed it.

The seed data quietly did not load. Every endpoint came back empty and there was nothing in the
logs at all. It was a missing spring.sql.init.mode=always. Spring only runs data.sql on in
memory databases by default and skips it on a real Postgres. That one was hard to find because
nothing actually failed, the tables were just empty.

The RDS certificate I downloaded was not a certificate. The URL I used gave back a 111 byte
AccessDenied page and curl saved it without complaining because I forgot -L. psql then said no
certificate or crl found. The path that works is /global/global-bundle.pem.

Password authentication failed on my first RDS connection. I reset the master password in the
console under Modify and applied it right away.

RDS gave me PostgreSQL 18 and my psql client is 15. Running \l failed with column
d.daticulocale does not exist because the newer server renamed it. I used a plain SELECT
instead.

The golfclub database did not exist. I missed the initial database name field when I made the
instance, so I had to connect to the default postgres database and run CREATE DATABASE
golfclub myself.

## Assumptions

Membership duration is a whole number of months, stored as an int.

Membership type is an enum saved as text so the rows stay readable.

Registering a member into a tournament is a plain join with no extra fields on it.

Email has to be unique across members or you cannot tell two members apart.

Searches match partial text and ignore capitals, because needing the exact full name would
make them close to useless.

## Testing

All of the endpoints were tested in Postman.

Screenshots of the requests and responses are in the screenshots folder.

There is also a screenshot of the two containers running in Docker.

The design patterns used are the Repository pattern for the Spring Data interfaces, a service
layer with an interface and an implementation, and DTOs so the entities never get sent over
HTTP.
