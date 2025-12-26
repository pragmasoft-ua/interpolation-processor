## 1. ANTLR Setup

- [ ] 1.1 Add ANTLR 4 dependency to `annotation-processor/pom.xml`, and to the `/pom.xml` to the `dependencyManagement` section.
- [ ] 1.2 Configure `antlr4-maven-plugin` for grammar compilation
- [ ] 1.3 Create `src/main/antlr4/interpolation/parser/` directory structure

## 2. Grammar Implementation

- [ ] 2.1 Create `TemplateLexer.g4` with lexer modes (DEFAULT_MODE, EXPRESSION)
- [ ] 2.2 Create `TemplateParser.g4` with template and expression rules
- [ ] 2.3 Verify grammar compiles and generates Java sources

## 3. Parser Integration

- [ ] 3.1 Create `TemplateParser.java` wrapper class with `parse(String template)` method
- [ ] 3.2 Create `ParsedTemplate` record with `String[] fragments` and `String[] varNames`
- [ ] 3.3 Implement ANTLR visitor/listener to extract fragments and variables
- [ ] 3.4 Implement error handling with position information

## 4. Unit Tests

- [ ] 4.1 Test simple variable: `Hello ${name}`
- [ ] 4.2 Test multiple variables: `${a} and ${b}`
- [ ] 4.3 Test adjacent variables: `${a}${b}`
- [ ] 4.4 Test escape sequence: `$${literal}`
- [ ] 4.5 Test no variables: `plain text`
- [ ] 4.6 Test empty template: ``
- [ ] 4.7 Test Unicode: `это ${name}`
- [ ] 4.8 Test error: unclosed `${name`
- [ ] 4.9 Test error: empty `${}`
- [ ] 4.10 Test error: invalid identifier `${123}`

## 5. Documentation Updates

- [ ] 5.1 Update `ARCHITECTURE.md`: change `\{var}` to `${var}` throughout
- [ ] 5.2 Update `README.md`: usage examples and syntax table
- [ ] 5.3 Update `TODO.md`: template parser section references
- [ ] 5.4 Update `openspec/project.md`: template syntax section

## 6. Validation

- [ ] 6.1 Run `./mvnw` to verify build
- [ ] 6.2 Run all unit tests
- [ ] 6.3 Run integration tests (when available)
