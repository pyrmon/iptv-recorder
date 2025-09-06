# Contributing to IPTV Recording Scheduler

Thank you for your interest in contributing! We welcome contributions from the community.

## How to Contribute

1. **Fork the repository** and create your branch from `main`
2. **Make your changes** with clear, descriptive commit messages
3. **Add tests** for any new functionality
4. **Ensure all tests pass** by running `mvn test`
5. **Submit a pull request** with a clear description of your changes

## Versioning

Versioning is handled automatically by CI/CD:
- Feature branches get snapshot versions: `1.0.0-branch-name-commit-SNAPSHOT`
- Main branch merges create release versions and tags
- No manual version changes needed in pull requests

## Development Setup

1. Ensure you have Java 21+ and Maven 3.6+ installed
2. Clone your fork: `git clone https://github.com/your-username/iptv-recorder.git`
3. Copy `.env.example` to `.env` and set your API key
4. Run tests: `mvn test`
5. Start the application: `mvn spring-boot:run`

## Code Style

- Follow existing code formatting and naming conventions
- Write clear, self-documenting code
- Add JavaDoc comments for public methods
- Keep methods focused and concise

## Reporting Issues

- Use the GitHub issue tracker
- Provide clear reproduction steps
- Include relevant logs and system information
- Check for existing issues before creating new ones

## Questions?

Feel free to open an issue for questions or discussions about potential contributions.