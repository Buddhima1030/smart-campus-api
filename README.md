# Smart Campus Sensor & Room Management API
## Overview
This project implements a RESTful API for managing Rooms, Sensors, and Sensor Readings within a Smart Campus environment using JAX-RS (Jersey - javax).
The API allows you to:
- Create and manage rooms
- Register and manage sensors inside those rooms
- Record and retrieve sensor readings over time
---
## Technology Stack
| Technology | Details |
|---|---|
| Language | Java 17 |
| JAX-RS Implementation | Jersey (javax) |
| Server | Apache Tomcat |
| Build Tool | Apache Maven |
| Data Storage | In-memory ConcurrentHashMap |
| IDE | Apache NetBeans |
---
## How to Run the Project
### Steps
1. Clone the repository:
```bash
git clone https://github.com/Buddhima1030/smart-campus-api.git
```
2. Open NetBeans IDE → File → Open Project
3. Select the cloned project folder
4. Ensure Apache Tomcat Server is configured under Tools → Servers
5. Right-click the project → Run
The application will be available at:
```
http://localhost:8080/smart-campus/api/v1
```
---
## Sample curl Commands
### 1. Create a Room
```bash
curl -X POST http://localhost:8080/smart-campus/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```
### 2. Get All Rooms
```bash
curl http://localhost:8080/smart-campus/api/v1/rooms
```
### 3. Get Room by ID
```bash
curl http://localhost:8080/smart-campus/api/v1/rooms/LIB-301
```
### 4. Create a Sensor (Valid Room Required)
```bash
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":25.0,"roomId":"LIB-301"}'
```
### 5. Filter Sensors by Type
```bash
curl "http://localhost:8080/smart-campus/api/v1/sensors?type=Temperature"
```
### 6. Add Sensor Reading
```bash
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":26.5}'
```
### 7. Get Sensor Readings History
```bash
curl http://localhost:8080/smart-campus/api/v1/sensors/TEMP-001/readings
```
---
## API Features
### 1. Room Management
- Create, retrieve, and delete rooms
- Prevent deletion if sensors are assigned → `409 Conflict`
### 2. Sensor Management
- Validate room existence before sensor creation → `422 Unprocessable Entity`
- Support filtering using query parameters (`?type=`)
### 3. Sensor Readings — Sub-resource Pattern
- Add and retrieve historical readings per sensor
- Automatically update `currentValue` on the parent sensor when a new reading is posted
### 4. Error Handling
| Exception | HTTP Status |
|---|---|
| `RoomNotEmptyException` | 409 Conflict |
| `LinkedResourceNotFoundException` | 422 Unprocessable Entity |
| `SensorUnavailableException` | 403 Forbidden |
| Unexpected errors (global handler) | 500 Internal Server Error |
### 5. Logging
- Request and response logging implemented using JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter`
- Every incoming request and outgoing response is logged with method, URI, and status code
---
## Report

### Part 1 — Service Architecture & Setup

**Question 01: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures.**

The default lifecycle of a JAX-RS resource class is per-request. This means for each and every incoming HTTP request there will be a new instance of the relevant resource class created to handle the specific request. All shared data types within the application will use a static data structure called ConcurrentHashMap. It is more safe than using plain HashMap because of safe concurrent reads and writes without data loss.

---

**Question 02: Why is the provision of "Hypermedia" considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**

HATEOAS is an important aspect of RESTful APIs to return links to other related data or endpoints. The discovery endpoint `/api/v1` returns links to resources like `/api/v1/rooms` and `/api/v1/sensors` in the response. The main advantage of using hypermedia in this way is clients do not need to do heavy documentation to know about available endpoints. Static documentation are out of date and this makes the API more flexible. It also enhances the developer experience by making the API easier to navigate.

---

### Part 2 — Room Management

**Question 01: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.**

When a list of rooms is returned, it can either return an array containing room IDs or return full room objects. Returning only IDs makes the response smaller and improves network bandwidth. However, the client has to make more requests to get the entire details of each room and it is more inefficient. Returning full room objects provides all the required details in a single response. On the other hand, the results in a larger payload and may be an issue with larger data sets.

---

**Question 02: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**

The DELETE implementation is idempotent and when it is used, it can be executed many times but the result will always be the same. For an example, the first DELETE on a valid room it will be removed and returns `204 No Content` message. After that if the client sends the same request again, response will be `404 Not Found` because there is no room that exists. When there are sensors in the room, the DELETE operation will not be executed and receive a `409 Conflict` because rooms cannot be removed when the sensor is active.

---

### Part 3 — Sensor Operations & Linking

**Question 01: Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**

Using `@Consumes` to tell a method tells JAX-RS that the API expects JSON formatted incoming requests. If a client sends a wrong format other than JSON, Jersey rejects the request and returns a `415 Unsupported Media Type` response.

---

**Question 02: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path. Why is the query parameter approach generally considered superior for filtering and searching collections?**

Using query parameters to filter data is the best option than embedding the filter within the path. For an example to find sensors of type CO2 the URL would be `/sensors?type=CO2`. It makes it possible to use multiple filters together easily. A path based parameter is meant to represent resources and their identity. A path like `/api/v1/sensors/CO2` cannot identify a specific resource clearly and has a high chance to mislead the client.

---

### Part 4 — Deep Nesting with Sub-Resources

**Question 01: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs?**

The sub-resource locator pattern allows resources to be managed through different classes. The `SensorResource` does not handle reading requests itself and `@Path("/{id}/readings")` delegates all reading operations to `SensorReadingResource`. Because the API can grow and expand, adding nested resources becomes easy without cluttering existing classes. Each resource class has its own logic and is specific to only one part of the API.

---

### Part 5 — Advanced Error Handling, Exception Mapping & Logging

**Question 02: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**

`404 Not Found` means that the requested URL does not exist on the server at all. A `422 Unprocessable Entity` means the resource exists and the JSON is valid but the data has logical errors that prevent processing. For an example, if a client attempts to create a sensor using the `/api/v1/sensors` endpoint by POSTing a sensor object, but the `roomId` referenced does not correspond to a room that currently exists, the `/api/v1/sensors` endpoint is still reachable. A `422` error correctly indicates that the request was valid and was understood by the server but could not be fulfilled due to semantic errors.

---

**Question 04: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**

Stack traces can reveal sensitive data like class names, package names, file paths, user configurations, and server data. With that information hackers or any other person can identify weaknesses in the system. By implementing a global exception mapper, the system avoids exposing internal information. Instead of returning stack trace details, it returns a `500 Internal Server Error` message with no business logic exposed.

---

**Question 05: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?**

Using a JAX-RS filter class instead of putting logs in every resource method is more efficient and improves code maintainability by reducing code duplication. Adding logs to every method in every resource class consumes a lot of development time and makes it difficult to confirm that logging is applied consistently. If any changes are required to the logging format, only the `LoggingFilter` class needs to be changed rather than updating every resource method across the entire codebase.

---

## Author
**Buddhima Jothiwansa**
University of Westminster
