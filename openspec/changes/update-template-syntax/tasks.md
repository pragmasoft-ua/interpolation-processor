## 1. ANTLR Setup

- [x] 1.1 Add ANTLR 4 dependency to `annotation-processor/pom.xml`, and to the `/pom.xml` to the `dependencyManagement` section.
- [x] 1.2 Configure `antlr4-maven-plugin` for grammar compilation
- [x] 1.3 Create `src/main/antlr4/interpolation/parser/` directory structure

## 2. Grammar Implementation

- [x] 2.1 Create `TemplateLexer.g4` with lexer modes (DEFAULT_MODE, EXPRESSION)
- [x] 2.2 Create `TemplateParser.g4` with template and expression rules
- [x] 2.3 Verify grammar compiles and generates Java sources

## 3. Parser Integration

- [x] 3.1 Create `TemplateParserWrapper.java` wrapper class with `parse(String template)` method
- [x] 3.2 Create `ParsedTemplate` record with `String[] fragments` and `String[] varNames`
- [x] 3.3 Implement ANTLR visitor/listener to extract fragments and variables
- [x] 3.4 Implement error handling with position information

## 4. Unit Tests

- [x] 4.1 Test simple variable: `Hello ${name}`
- [x] 4.2 Test multiple variables: `${a} and ${b}`
- [x] 4.3 Test adjacent variables: `${a}${b}`
- [x] 4.4 Test escape sequence: `$${literal}`
- [x] 4.5 Test no variables: `plain text`
- [x] 4.6 Test empty template: ``
- [x] 4.7 Test Unicode: `это ${name}`
- [x] 4.8 Test error: unclosed `${name`
- [x] 4.9 Test error: empty `${}`
- [x] 4.10 Test error: invalid identifier `${123}`

## 5. Documentation Updates

- [x] 5.1 Update `ARCHITECTURE.md`: change `\{var}` to `${var}` throughout
- [x] 5.2 Update `README.md`: usage examples and syntax table
- [x] 5.3 Update `TODO.md`: template parser section references
- [x] 5.4 Update `openspec/project.md`: template syntax section

## 6. Validation

- [x] 6.1 Run `./mvnw` to verify build
- [x] 6.2 Run all unit tests
- [x] 6.3 Run integration tests (when available)
