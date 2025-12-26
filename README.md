# interpolation-processor

Compile-time string interpolation for Java 17+ using annotation processing and bytecode transformation, providing a JEP 465-like developer experience without requiring language modifications.

## Features

- **Inline string interpolation syntax** similar to JEP 465 String Templates
- **Compile-time template parsing** - templates are parsed once during compilation, not at runtime
- **Standard APIs only** - uses JSR 269 Annotation Processing and Classfile API (backport)
- **Zero runtime overhead** - optimized bytecode generation
- **Valid Java syntax** - no custom syntax that breaks IDE support

## Quick Start

### Maven Dependency

Add the annotation processor to your project:

```xml
<dependencies>
    <!-- Runtime API - required at compile and runtime -->
    <dependency>
        <groupId>pragmasoft</groupId>
        <artifactId>interpolation-api</artifactId>
        <version>2025.12.1-SNAPSHOT</version>
    </dependency>

    <!-- Annotation processor - only needed at compile time -->
    <dependency>
        <groupId>pragmasoft</groupId>
        <artifactId>interpolation-processor</artifactId>
        <version>2025.12.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Usage Example

```java
import static interpolation.Interpolator.str;

public class Example {
    public void greet(String name, int count) {
        // Use \{varName} syntax for variable interpolation
        String msg = str("Hello \{name}, you have \{count} items");
        System.out.println(msg);
    }
}
```

The `str()` method is a placeholder that gets replaced during compilation with optimized bytecode that performs the string concatenation.

## How It Works

1. **AST Analysis** - During compilation, the annotation processor scans for `str()` calls and extracts template strings
2. **Template Parsing** - Templates are parsed to extract fragments and variable references
3. **Variable Resolution** - Variables are resolved in scope to determine their types and slot numbers
4. **Bytecode Transformation** - After compilation, the processor transforms `.class` files to replace `str()` calls with optimized concatenation code

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed design documentation.

## Project Structure

This is a multi-module Maven project:

```
interpolation-processor/
├── interpolation-api/          # Runtime API (Interpolator, VarInfo records)
├── annotation-processor/       # Annotation processor implementation
├── integration-test/           # Integration tests and usage examples
├── openspec/                   # Spec-driven development artifacts
├── pom.xml                     # Parent POM
└── mvnw, mvnw.cmd              # Maven wrapper
```

### Modules

| Module | Artifact ID | Description |
|--------|-------------|-------------|
| `interpolation-api` | `interpolation-api` | Runtime API containing `Interpolator` and `VarInfo` records. Required as a compile and runtime dependency. |
| `annotation-processor` | `interpolation-processor` | The annotation processor that performs AST analysis and bytecode transformation. Used as a `provided` scope dependency. |
| `integration-test` | `interpolation-integration-test` | Integration tests demonstrating usage patterns. Not published. |

## Building

The project uses Maven Wrapper and requires Java 17+:

```bash
# Build and run tests (default goals: clean install)
./mvnw

# Or explicitly
./mvnw clean install

# Skip tests
./mvnw clean install -DskipTests
```

## Requirements

- **Java**: 17 or higher
- **Maven**: 3.6.3 or higher (wrapper included)

## Template Syntax

| Syntax | Description |
|--------|-------------|
| `\{varName}` | Insert variable value |
| `\\{` | Escaped brace (literal `{`) |

### Supported Variable Types

- Method parameters
- Local variables
- Instance fields
- Static fields
- All primitive types (with automatic boxing)
- Object types

## Current Limitations

- Simple variable references only (`\{var}`, not `\{obj.method()}`)
- No format specifiers yet
- Expression evaluation not supported (planned for future)

## Development Status

This project is under active development. See [TODO.md](TODO.md) for the implementation roadmap.

## Contributing

Contributions are welcome! Please see our guidelines:

### Building and Developing

1. Clone the repository
2. Run `./mvnw clean install` to build
3. Import as a Maven project in your IDE

### Pull Request Guidelines

- Use feature branches
- Include tests for new functionality
- Follow [Oracle Java Code Conventions](http://www.oracle.com/technetwork/java/javase/documentation/codeconvtoc-136057.html)

## License

This project is released under the [GNU General Public License v3.0](LICENSE).

This project includes and repackages the [Annotation-Processor-Toolkit](https://github.com/holisticon/annotation-processor-toolkit) released under the [MIT License](/3rdPartyLicenses/annotation-processor-toolkit/LICENSE.txt).

## References

- [JEP 465: String Templates (Third Preview)](https://openjdk.org/jeps/465)
- [JEP 457: Class-File API (Preview)](https://openjdk.org/jeps/457)
- [JSR 269: Pluggable Annotation Processing API](https://jcp.org/en/jsr/detail?id=269)
- [Classfile API Backport](https://github.com/dmlloyd/jdk-classfile-backport)
