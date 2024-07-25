# Hire API

## Overview

The Hire API is a Spring Boot application designed to interact with the GitHub API. It allows you to fetch repositories and branches for a given GitHub user. The application includes error handling to manage GitHub API issues and provides clear error messages.

## Table of Contents

1. [Installation](#installation)
2. [Configuration](#configuration)
3. [Usage](#usage)
4. [API Endpoints](#api-endpoints)
5. [Error Handling](#error-handling)
6. [Logging](#logging)
7. [Contributing](#contributing)
8. [License](#license)

## Installation

To set up and run the Hire API locally, follow these steps:

1. **Clone the Repository:**

    ```bash
    git clone https://github.com/yourusername/HireApi.git
    ```

2. **Navigate to the Project Directory:**

    ```bash
    cd HireApi
    ```

3. **Build the Project:**

   Ensure you have [Gradle](https://gradle.org/) installed. Run the following command to build the project:

    ```bash
    ./gradlew build
    ```

4. **Run the Application:**

   After building the project, you can run the application using:

    ```bash
    ./gradlew bootRun
    ```
## Configuration

**Explanation of Properties:**

- `server.port`: The port on which the application will run.
- `github.api.url`: The base URL for the GitHub API.
- `repositories.url.suffix`: The suffix to append to the base URL to fetch repositories.
- `user.url.suffix`: The suffix to append to the base URL to fetch user details.
- `branches.url.suffix.template`: The URL template for fetching branches of a repository.

Read more: [GitHub API Documentation](https://developer.github.com/v3)

## Usage

**Start the Application:**

After starting the application, it will be accessible at [http://localhost:2407](http://localhost:2407).

**Fetch Repositories:**

To retrieve repositories for a specific GitHub user, make a GET request to:

```bash
GET /api/github/repositories/{username}
```
**Replace `{username}` with the GitHub username.**

## API Endpoints

### Get Repositories

- **URL:** `/api/github/repositories/{username}`
- **Method:** `GET`

#### Headers:

| Key     | Value           |
|---------|-----------------|
| Accept  | application/json |

#### URL Parameters:

- `username`: The GitHub username for which repositories are to be fetched.

#### Success Response:

- **Code:** `200 OK`
- **Content:** `application/json`

    ```json
    [
      {
        "repositoryName": "repo1",
        "ownerLogin": "owner1",
        "branches": [
          {
            "name": "branch1",
            "commit": {
              "sha": "commitsha"
            }
          }
        ]
      }
    ]
    ```

#### Error Responses:

- **Code:** `404 Not Found`
    - **Content:**

      ```json
      {
        "status": 404,
        "message": "User not found: {username}"
      }
      ```

- **Code:** `406 Not Acceptable`
You should add [Headers](#headers)
## Error Handling

The API handles errors and provides structured responses:

- **UserNotFoundException:**
    - **Status Code:** `404 Not Found`
    - **Message:** "User not found: {username}"

- **GitHubApiException:**
    - **Status Code:** `400 Bad Request`
    - **Message:** "Error while communicating with GitHub API"

- **General Exceptions:**
    - **Status Code:** `500 Internal Server Error`
    - **Message:** "An unexpected error occurred: {error details}"

## Logging

The application uses logging to monitor its behavior. The default logging level is set to INFO, but you can adjust it as needed.

Logging Configuration

Logging is configured in `src/main/resources/log4j2.xml`. You can modify the logging level and settings according to your requirements.

### Example Logs

**UserNotFoundException:**

    WARN 209716 --- [HireApi] [nio-2407-exec-3] a.com.hireapi.service.GitHubClient : User not found: olekmysko2

## License
**free :)**
