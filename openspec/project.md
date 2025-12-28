# Project Context

## Purpose

Compile-time string interpolation for Java, providing a JEP 465-like developer experience using annotation processing and bytecode transformation. The goal is to enable syntax like `str("Hello ${name}")` that gets transformed into optimized bytecode during compilation.

### Key Goals

1. Provide inline string interpolation syntax similar to JEP 465 String Templates using `${var}` syntax
2. Parse templates at compile-time using ANTLR, not runtime (zero parsing overhead)
3. Use only standard and stable APIs (JSR 269, Classfile API)
4. Support Java 17+ using the Classfile API backport
5. Achieve zero runtime performance overhead through optimized bytecode generation

## Tech Stack

- **Java 17+** (currently building with Java 25)
- **Maven** - Multi-module build with Maven Wrapper
- **JSR 269** - Standard Annotation Processing API
- **Classfile API** - Official bytecode transformation (via backport for Java 17-22)
- **ANTLR 4** - Parser generator for template syntax parsing
- **JUnit 4** - Unit testing
- **Hamcrest** - Test assertions
- **Mockito** - Test mocking
- **compile-testing** - Annotation processor testing (Google)

## Project Structure

```
interpolation-processor/
├── interpolation-api/          # Runtime API module
│   └── src/main/java/interpolation/
│       ├── Interpolator.java   # Core record with str() and process()
│       └── VarInfo.java        # Variable metadata record
├── annotation-processor/       # Processor implementation module
│   └── src/main/java/interpolation/processor/
│       └── InterpolationProcessor.java
├── integration-test/           # Integration tests module
│   └── src/
│       ├── main/java/          # Example usage code
│       └── test/java/          # Integration tests
├── openspec/                   # Spec-driven development artifacts
├── pom.xml                     # Parent POM
└── mvnw, mvnw.cmd              # Maven wrapper scripts
```

### Module Dependencies

```
interpolation-api  <--  annotation-processor  <--  integration-test
```

## Project Conventions

### Code Style

- Follow Oracle Java Code Conventions
- Use Java records for immutable data structures
- Prefer immutability throughout
- Use meaningful names that describe intent
- Keep methods focused and single-purpose
- Checkstyle configuration in `config/sun_checks.xml` (optional profile)

### Naming Conventions

- Packages: `interpolation`, `interpolation.processor`
- Classes: PascalCase (e.g., `InterpolationProcessor`, `VarInfo`)
- Methods: camelCase (e.g., `str()`, `process()`)
- Constants: UPPER_SNAKE_CASE (e.g., `INTERPOLATORS`)
- Generated fields: suffix with `_$` to avoid conflicts

### Architecture Patterns

- **Two-phase processing**: AST analysis during compilation, bytecode transformation in final round
- **Immutable data structures**: All metadata stored in Java records
- **Standard APIs only**: No internal compiler dependencies or AST manipulation
- **Clean separation**: Compile-time logic vs runtime execution

### Key Design Decisions

1. **No AST Manipulation** - Unlike Lombok/Manifold, we avoid fragile internal compiler APIs
2. **Bytecode Transformation** - Use Classfile API to transform compiled `.class` files
3. **Static Caching** - Pre-parsed templates stored in static field arrays
4. **Valid Java Syntax** - `str("template")` is valid Java, ensuring IDE compatibility

## Testing Strategy

### Unit Tests

- Located in `src/test/java` of each module
- Use JUnit 4 with Hamcrest matchers
- Test template parsing edge cases
- Test variable resolution
- Test slot calculation
- Use `compile-testing` for annotation processor unit tests

### Integration Tests

- Located in `integration-test` module
- Test full compilation and execution flow
- Cover all variable types (primitives, objects, fields)
- Test error cases (unknown variables, malformed templates)
- Run with `mvn verify` or during normal build

### Running Tests

```bash
# Run all tests (unit + integration)
./mvnw clean install

# Run only unit tests
./mvnw test

# Run integration tests
./mvnw verify
```

## Git Workflow

### Branching Strategy

- `main` - Primary development branch
- Feature branches for new work
- Pull requests for code review

### Commit Conventions

- Use descriptive commit messages
- Reference issues when applicable
- Keep commits focused and atomic

## Domain Context

### Annotation Processing Concepts

- **Round-based processing**: Processors run in multiple rounds
- **processingOver()**: Final round for bytecode transformation
- **Messager**: Error reporting with source location
- **Trees API**: Access to AST for analysis

### Bytecode Concepts

- **Local variable slots**: 0 = `this` (instance methods), parameters, then locals
- **Wide types**: `long` and `double` occupy 2 slots
- **Type descriptors**: JVM format (`I`, `Ljava/lang/String;`, etc.)
- **Static initializer**: `<clinit>` method for field initialization

### Template Syntax

- `${varName}` - Variable interpolation (industry standard: Spring, Velocity, shell)
- `$${` - Escaped dollar-brace (literal `${` in output)
- `$` - Single dollar sign (no escaping needed unless followed by `{`)
- Variables resolved from: parameters, locals, instance fields, static fields

## Important Constraints

### Technical Constraints

- Must support Java 17+ (use Classfile API backport for Java 17-22)
- No dependencies in runtime API module (interpolation-api)
- Annotation processor shaded into single JAR (maven-shade-plugin)
- All processor dependencies must be `provided` scope when used

### Design Constraints

- Only simple variable references initially (no expressions like `${a + b}`)
- No property navigation (no `${obj.field}`)
- Template string must be a compile-time constant

### Build Constraints

- Maven 3.9.12+ required
- enforcer plugin validates dependencies (no transitive runtime deps)
- integration-test module not published (skip deploy/install)

## External Dependencies

### Runtime (interpolation-api)

- None (zero dependencies)

### Compile-time (annotation-processor)

- Classfile API Backport (planned): https://github.com/dmlloyd/jdk-classfile-backport
- interpolation-api (shaded)

### Test

- JUnit 4.13.2
- Hamcrest 2.2
- Mockito 2.28.2
- compile-testing 0.23.0

## Development Commands

```bash
# Build everything
./mvnw

# Clean build
./mvnw clean install

# Skip tests
./mvnw clean install -DskipTests

# Run with checkstyle
./mvnw clean install -Pcheckstyle

# Update dependency versions
./mvnw versions:display-dependency-updates
./mvnw versions:display-plugin-updates
```
