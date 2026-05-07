# PostPulse API — Error Reference

All error responses in the PostPulse API conform to [RFC 7807 — Problem Details for HTTP APIs](https://datatracker.ietf.org/doc/html/rfc7807).

Every error response carries the `Content-Type: application/problem+json` header and a consistent JSON body structure described below.

---

## Standard Error Response Shape

```json
{
  "type": "https://github.com/your-username/postpulse-backend/blob/main/docs/errors.md#resource-not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Post with ID 42 does not exist.",
  "instance": "/api/v1/posts/42",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

| Field       | Type             | Always Present | Description                                                                 |
|-------------|------------------|----------------|-----------------------------------------------------------------------------|
| `type`      | `string` (URI)   | ✅              | URI pointing to documentation for this specific problem type (this page).   |
| `title`     | `string`         | ✅              | Short, human-readable summary of the problem type.                          |
| `status`    | `integer`        | ✅              | HTTP status code.                                                           |
| `detail`    | `string`         | ✅              | Human-readable explanation specific to this occurrence.                     |
| `instance`  | `string` (URI)   | ✅              | The request path that triggered this error.                                 |
| `timestamp` | `string` (ISO 8601) | ✅           | UTC timestamp of when the error occurred.                                   |
| `errors`    | `object`         | ❌              | Field-level validation errors. Present **only** on `400 Validation Failed`. |

---

## Error Type Index

| Error Type Anchor                                   | HTTP Status | Title                  |
|-----------------------------------------------------|-------------|------------------------|
| [#resource-not-found](#resource-not-found)          | `404`       | Resource Not Found     |
| [#api-error](#api-error)                            | `4xx`       | API Error              |
| [#validation-failed](#validation-failed)            | `400`       | Validation Failed      |
| [#constraint-violation](#constraint-violation)      | `400`       | Constraint Violation   |
| [#type-mismatch](#type-mismatch)                    | `400`       | Type Mismatch          |
| [#access-denied](#access-denied)                    | `403`       | Access Denied          |
| [#unauthorized](#unauthorized)                      | `401`       | Unauthorized           |
| [#internal-error](#internal-error)                  | `500`       | Internal Server Error  |

---

## Error Types

---

### `#resource-not-found`

**Title:** Resource Not Found
**HTTP Status:** `404 Not Found`

**When it occurs:**
A requested resource — such as a Post, Category, or User — does not exist in the system. This is typically triggered by supplying an ID that was never created or has been deleted.

**Example triggers:**
- `GET /api/v1/posts/999` — post with ID 999 does not exist
- `GET /api/v1/categories/50` — category with ID 50 does not exist

**Example response:**

```json
{
  "type": "https://github.com/your-username/postpulse-backend/blob/main/docs/errors.md#resource-not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Post not found with id: 999",
  "instance": "/api/v1/posts/999",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

**Client action:** Do not retry. Verify the resource ID before making the request.

---

### `#api-error`

**Title:** API Error
**HTTP Status:** `4xx` (varies — always inspect the `status` field)

**When it occurs:**
A domain-level rule was violated. This is a general-purpose error for business logic violations that do not fit a more specific category — for example, attempting to perform an action that conflicts with current system state (e.g., a user attempting to delete another user's post).

**Example response:**

```json
{
  "type": "https://github.com/your-username/postpulse-backend/blob/main/docs/errors.md#api-error",
  "title": "API Error",
  "status": 400,
  "detail": "You can only update your own posts.",
  "instance": "/api/v1/posts/12",
  "timestamp": "2025-01-15T10:31:00Z"
}
```

**Client action:** Read the `detail` field for a specific, human-readable reason. The `status` field determines the appropriate retry/escalation strategy.

---

### `#validation-failed`

**Title:** Validation Failed
**HTTP Status:** `400 Bad Request`

**When it occurs:**
The request body failed `@Valid` bean validation. One or more fields did not satisfy their constraints (e.g., blank required fields, values exceeding maximum length, invalid format).

This is the **only** error type that includes the `errors` extension field, which contains a map of field names to their specific violation messages.

**Example response:**

```json
{
  "type": "https://github.com/your-username/postpulse-backend/blob/main/docs/errors.md#validation-failed",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields failed validation.",
  "instance": "/api/v1/posts",
  "timestamp": "2025-01-15T10:32:00Z",
  "errors": {
    "title": "must not be blank",
    "content": "size must be between 10 and 10000",
    "categoryId": "must not be null"
  }
}
```

**Client action:** Iterate over the `errors` map. Display each field-level message to the user at the corresponding form field. Do not retry until the input is corrected.

---

### `#constraint-violation`

**Title:** Constraint Violation
**HTTP Status:** `400 Bad Request`

**When it occurs:**
A method-level `@Validated` constraint failed on a path variable or request parameter — for example, supplying a non-positive integer as a resource ID, or a page size outside the allowed range.

Unlike `#validation-failed`, this occurs at the **parameter level** rather than the request body level.

**Example triggers:**
- `GET /api/v1/posts/-1` — `id` must be greater than 0
- `GET /api/v1/posts?pageSize=500` — `pageSize` must be between 1 and 100

**Example response:**

```json
{
  "type": "https://github.com/your-username/postpulse-backend/blob/main/docs/errors.md#constraint-violation",
  "title": "Constraint Violation",
  "status": 400,
  "detail": "id: must be greater than 0",
  "instance": "/api/v1/posts/-1",
  "timestamp": "2025-01-15T10:33:00Z"
}
```

**Client action:** Correct the offending parameter value. The `detail` field identifies the parameter name and its constraint.

---

### `#type-mismatch`

**Title:** Type Mismatch
**HTTP Status:** `400 Bad Request`

**When it occurs:**
A path variable or request parameter could not be converted to its expected Java type. The most common case is supplying a non-numeric string where an integer ID is expected.

**Example triggers:**
- `GET /api/v1/posts/abc` — `abc` cannot be parsed as `Long`
- `GET /api/v1/posts?page=first` — `first` cannot be parsed as `Integer`

**Example response:**

```json
{
  "type": "https://github.com/your-username/postpulse-backend/blob/main/docs/errors.md#type-mismatch",
  "title": "Type Mismatch",
  "status": 400,
  "detail": "Invalid value 'abc' for parameter 'id'. Expected type: Long",
  "instance": "/api/v1/posts/abc",
  "timestamp": "2025-01-15T10:34:00Z"
}
```

**Client action:** Ensure the parameter value matches the expected type documented in the API specification before retrying.

---

### `#access-denied`

**Title:** Access Denied
**HTTP Status:** `403 Forbidden`

**When it occurs:**
The authenticated user does not have the required role or ownership to perform the requested operation. Authentication succeeded (the JWT was valid), but authorization failed.

**Common scenarios:**
- A user with the `ROLE_USER` role attempts an operation restricted to `ROLE_ADMIN`
- A user attempts to update or delete a Post they do not own

**Example response:**

```json
{
  "type": "https://github.com/your-username/postpulse-backend/blob/main/docs/errors.md#access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "You do not have permission to perform this action.",
  "instance": "/api/v1/admin/users",
  "timestamp": "2025-01-15T10:35:00Z"
}
```

**Client action:** Do not retry with the same credentials. Escalate to an account with the appropriate permissions or contact the system administrator.

---

### `#unauthorized`

**Title:** Unauthorized
**HTTP Status:** `401 Unauthorized`

**When it occurs:**
The request did not include a valid JWT Bearer token, or the token has expired. This error is returned by the security filter chain **before** the request reaches a controller.

**Common scenarios:**
- No `Authorization` header was sent
- The JWT has passed its expiry time
- The JWT signature is invalid or tampered with

**Example response:**

```json
{
  "type": "https://github.com/your-username/postpulse-backend/blob/main/docs/errors.md#unauthorized",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Authentication is required to access this resource.",
  "instance": "/api/v1/posts",
  "timestamp": "2025-01-15T10:36:00Z"
}
```

**Client action:** Obtain a fresh JWT by calling `POST /api/v1/auth/login` and resubmit the request with the `Authorization: Bearer <token>` header.

---

### `#internal-error`

**Title:** Internal Server Error
**HTTP Status:** `500 Internal Server Error`

**When it occurs:**
An unexpected server-side error occurred that was not caused by the client request. The full error details are logged internally and are not exposed in the response for security reasons.

**Example response:**

```json
{
  "type": "https://github.com/your-username/postpulse-backend/blob/main/docs/errors.md#internal-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "An unexpected error occurred. Please try again later.",
  "instance": "/api/v1/posts",
  "timestamp": "2025-01-15T10:37:00Z"
}
```

**Client action:** Retry with exponential backoff. If the error persists, report the `instance` path and `timestamp` to the API maintainer for log correlation.

---

## Authentication

Protected endpoints require a JWT Bearer token in the `Authorization` header:

```
Authorization: Bearer <your_jwt_token>
```

Tokens are obtained via:

```
POST /api/v1/auth/login
```

Tokens expire after a configured duration. On expiry, re-authenticate and use the new token.

---

## Pagination Error Behaviour

Paginated endpoints (`/api/v1/posts`, etc.) accept `pageNo`, `pageSize`, `sortBy`, and `sortDir` query parameters. Invalid values for these parameters produce `#constraint-violation` or `#type-mismatch` errors as described above.

---

## OpenAPI / Swagger

Interactive API documentation is available at:

```
GET /swagger-ui.html
```

All error response schemas are documented under the `PostPulseProblemDetail` component in the generated OpenAPI specification.