# BCI Security User Service

Spring Boot 2.5.14 (Java 11) microservice with in-memory H2 database that exposes user-related endpoints: health check, sign-up, login, and an extra list endpoint used internally for testing. JWT is used to generate access tokens. Validation rules for email and password are configurable via application.properties.

## Quick start

Prerequisites:
- Java 11
- Gradle Wrapper (included)

Run the service:
- Mac/Linux: `./gradlew bootRun`
- Windows: `gradlew.bat bootRun`

Service URL: `http://localhost:8081`

H2 Console (optional):
- URL: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:userdb`
- User: `test-bci-user-serv-db`
- Password: `t3st-bci-4lm0st`

## Postman: Try it yourself

You can import the Postman collection and environment to exercise all endpoints quickly.

Import files from this repository:
- Collection file (download and import via Postman > Import > File):
  - src/main/resources/collections/bci-security-api.postman_collection.json
- Environment file (download and import via Postman > Import > File):
  - src/main/resources/collections/bci-user-security-env.postman_environment.json

Postman environment variables used:
- baseUrl: defaults to `http://localhost:8081`
- token: will be set automatically after running the “Sign Up - Valid User” request (script extracts token from the response). You can also paste a token manually if needed.

How to use:
1) Start the app (`bootRun`).
2) In Postman, select the imported environment: “BCI User API - Local”. Ensure baseUrl points to your server (default is localhost:8081).
3) Run the requests in order:
   - Health Check -> should return 200 with { status: "UP", ... }
   - Sign Up - Valid User -> creates a new user and sets {{token}} in the environment automatically
   - Login - Valid Token -> uses Authorization: Bearer {{token}}; it refreshes token and returns user
   - Optional: Get all Users (extra endpoint for internal test)
4) Explore alternative scenarios from the collection: Invalid Email, Invalid Password, Duplicate Email, Invalid Token.

## API Endpoints (focus from Postman collection)

Base path: `/api`

- GET `/health`
  - Simple health-check endpoint to verify the service is running.

- POST `/sign-up`
  - Request body example:
    {
      "name": "Carlos ROMERO",
      "email": "carlos.romero@example.com",
      "password": "a2asfGfdfdf4",
      "phones": [ { "number": 123456789, "citycode": 1, "contrycode": "+1" } ]
    }
  - Success: 201 Created with user fields including id, created, lastLogin, token, isActive, name, email, password (as per requirement), phones.
  - Errors (shape):
    {
      "error": [ { "timestamp": "...", "codigo": 400|409|..., "detail": "..." } ]
    }
    - 400 for validations (email format, password rules)
    - 409 if email already exists

- POST `/login`
  - Headers: Authorization: Bearer <token>
  - Behavior: validates the token and refreshes it; returns the user with password (per requirement).

- GET `/users/all-with-passwords` (internal/testing)
  - Returns list of users including password hashes; not recommended for production exposure.

## Validation configuration (application.properties)
You can change the validation regex and messages without code changes:
- app.validation.email-regex
- app.validation.email-message
- app.validation.password-regex
- app.validation.password-message

Current defaults:
- Email regex: `^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$`
- Password regex: `^(?=.{8,12}$)(?=(?:.*[A-Z]){1})(?!.*[A-Z].*[A-Z])(?=(?:.*\\d){2})(?!.*\\d.*\\d.*\\d)(?=.*[a-z])[A-Za-z\\d]+$`
- Messages are also configurable via the `-message` properties.

Password rule summary:
- Exactly one uppercase letter
- Exactly two digits (not necessarily consecutive)
- Lowercase letters allowed
- Length 8–12

## cURL quick tests

Health:
- curl --request GET "http://localhost:8081/api/health"

Sign up (valid):
- curl --request POST \
  --url http://localhost:8081/api/sign-up \
  --header 'Content-Type: application/json' \
  --data '{"name":"Test User","email":"test.user@example.com","password":"a2asfGfdfdf4","phones":[{"number":123456789,"citycode":1,"contrycode":"+1"}]}'

Login:
- curl --request POST \
  --url http://localhost:8081/api/login \
  --header 'Authorization: Bearer <paste-token-from-signup>'

## Running tests
- All tests: `./gradlew test`
- Only controller tests: `./gradlew test --tests com.jromax.bcisecurityuserservice.controller.UserControllerTest`
- Only JwtUtil tests: `./gradlew test --tests com.jromax.bcisecurityuserservice.util.JwtUtilTest`
- Integration tests that hit Spring context (guarded by app.env.enable-integration-test=true in application.properties):
  - `./gradlew test --tests com.jromax.bcisecurityuserservice.controller.UserControllerIntegrationTest`

## Notes
- DB is in-memory (H2) and initialized from `src/main/resources/import.sql` with seed users and phones.
- JWT token is generated on sign-up and refreshed on login.
- For demo purposes and per requirements, password hashes are returned in some responses; do not do this in production.

## Diagrams (PlantUML)
The architecture and flows are documented as PlantUML (.puml) files located under:
- src/main/resources/documentation/

Available diagrams:
- Component: src/main/resources/documentation/component-diagram.puml
- Sequence (Login): src/main/resources/documentation/login-sequence-diagram.puml
- Sequence (Sign Up): src/main/resources/documentation/signup-sequence-diagram.puml
- Sequence (Health Check): src/main/resources/documentation/healthcheck-sequence-diagram.puml

Viewing on GitHub:
- GitHub shows .puml files as text. Click the links above to read the source.

Quick ways to render the diagrams visually:
- IntelliJ IDEA plugin
  - Install "PlantUML Integration" (Settings > Plugins), open the .puml file; a preview panel will render the diagram.
- Option C: Local CLI using Docker
  - docker run --rm -v "$(pwd)":/data plantuml/plantuml:jetty -tpng src/main/resources/documentation/*.puml
