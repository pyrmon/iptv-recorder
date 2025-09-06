# IPTV Recording Scheduler

A Spring Boot application that schedules and records IPTV streams using ffmpeg.

## Legal Notice

**This software is intended for personal and educational use only.** Users are responsible for ensuring they have the legal right to record any content and must comply with all applicable laws, copyright regulations, and terms of service of their IPTV providers.

- Only record content you have the right to record
- Respect copyright laws and intellectual property rights
- Comply with your IPTV provider's terms of service
- Users assume all legal responsibility for their use of this software

The developers of this software are not responsible for any misuse or legal issues arising from its use.

## Features

- Schedule recordings from IPTV streams
- RESTful API for managing channels and recordings
- SQLite database for persistence
- Docker support
- Health checks and monitoring

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.6+
- Docker (optional)
- ffmpeg installed on the system

### Running Locally

1. Clone the repository
2. Copy `.env.example` to `.env` and configure your API key
3. Run with Maven:
   ```bash
   mvn spring-boot:run
   ```

### Running with Docker

1. Copy `compose.example.yml` to `compose.yml`
2. Configure your environment variables in `.env`
3. Build and run:
   ```bash
   docker build -t iptv-recorder .
   docker compose up -d
   ```

## API Documentation

The application exposes REST endpoints for:
- Managing channels (`/api/channels`)
- Scheduling recordings (`/api/recordings`)
- Health monitoring (`/actuator/health`)

## Configuration

Key configuration options in `application.yml`:
- `server.port`: Application port (default: 8084)
- `application.security.api-key`: API key for authentication
- `recordingservice.recording-folder-prefix`: Directory for recordings

## Versioning

This project uses automated semantic versioning:
- **Main branch**: Automatically creates release versions and tags
- **Feature branches**: Creates snapshot versions with branch name and commit hash
- **Docker images**: Tagged with version numbers and `latest` for main branch

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License.
