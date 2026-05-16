# Research Report

## Canvas OAuth2 Authentication and API Integration

### Summary of Work

I researched how to authenticate with the Canvas LMS API in order to integrate our backend with Canvas for our project. The goal was to understand how Canvas OAuth2 works, what credentials are required (developer keys, client ID, client secret), and what limitations exist when testing the Canvas API during development. I investigated the official Canvas API documentation and tested whether manual access tokens could be generated from the Canvas user interface. After analyzing the documentation and attempting to generate tokens through my Canvas account, I identified that our institution restricts manual token generation, which affects how we must approach API development.

### Motivation

Our team is building a Canvas-integrated application that needs to retrieve student data such as courses, assignments, and other course information. To access these resources, the backend must authenticate with the Canvas API. Initially, we attempted to test the API using manual access tokens, but this approach failed because the university Canvas instance restricts token generation for student accounts. Because of this limitation, I needed to research the correct authentication flow and determine what development steps we can take before obtaining proper OAuth credentials from the Canvas administrators.

### Time Spent

Time was primarily spent reviewing the Canvas OAuth2 documentation, exploring the Canvas user interface to attempt generating manual tokens, and analyzing how Canvas developer keys and OAuth flows work. I also examined our backend architecture to determine whether our current codebase could support Canvas DTO models and mock API endpoints while the OAuth access issue is being resolved. Additional time was spent identifying which components (controllers, services, DTO models, and API clients) will be needed to properly integrate Canvas once authentication is available.

### Results

From this research, I learned that Canvas uses the OAuth2 Authorization Code flow for third-party applications. In order to authenticate, an application must obtain a Canvas Developer Key, which provides a client_id and client_secret. The OAuth process involves redirecting users to Canvas for authentication, receiving an authorization code through a callback endpoint, and exchanging that code for an access token through the Canvas token endpoint. These tokens are then used to authenticate API requests via the Authorization: Bearer header.

Since OAuth credentials are not yet available, I identified an alternative development strategy. The backend can continue development by defining Canvas DTO models that match the expected Canvas API response structures and by creating a mock Canvas API server that returns sample JSON data. This approach allows the frontend and Chrome extension to continue development and testing while the authentication system is being resolved. Once OAuth credentials are obtained, the mock API can be replaced with real Canvas API calls through a dedicated Canvas API client. This staged approach allows the project to progress without blocking development on authentication issues.

### Sources

- OAuth2 Overview [^1](https://developerdocs.instructure.com/services/canvas/oauth2/file.oauth)
- OAuth2 Endpoints[^2](https://developerdocs.instructure.com/services/canvas/oauth2/file.oauth_endpoints)
- Developer Keys[^3](https://developerdocs.instructure.com/services/canvas/oauth2/file.developer_keys)
