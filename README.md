# ktor-starter
Starter project with jwt auth and exposed. Find the associated blog post at: https://www.thebookofjoel.com/blog/kotlin-ktor-exposed-postgres

This is a project to help get started quickly with Ktor, Exposed, Postgres and Flywaydb.

## Running the project

1) clone the repo
2) if you don't have postgres installed, run `docker-compose up` to bring up a postgres container
3) set the DB_URL, DB_USER and DB_PASSWORD environment variables (can be found in the compose file if you are using that)
4) build with gradle and run the project
