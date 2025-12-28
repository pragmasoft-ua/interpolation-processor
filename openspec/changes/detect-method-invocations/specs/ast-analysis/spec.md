# ast-analysis Specification

## Purpose

Detect and analyze `Interpolator.str()` method invocations during annotation processing to collect call site metadata required for bytecode transformation.

## ADDED Requirements

### Requirement: AST Scanner Registration

The annotation processor SHALL register a `TaskListener` during initialization to receive AST analysis events via `JavacTask.addTaskListener()`.

#### Scenario: TaskListener registered on processor init

- **WHEN** the annotation processor is initialized via `init(ProcessingEnvironment)`
- **THEN** a `TaskListener` is registered that handles `TaskEvent.Kind.ANALYZE` events

#### Scenario: AST analysis triggered per compilation unit

- **WHEN** the compiler finishes analyzing a compilation unit
- **THEN** the processor receives the `TaskEvent` and scans the compilation unit for `str()` calls

### Requirement: Method Invocation Detection

The processor SHALL detect all calls to `Interpolator.str(String)` in the analyzed source code using a `TreePathScanner`.

#### Scenario: Detect direct static method call

- **WHEN** source code contains `Interpolator.str("template")`
- **THEN** the processor identifies this as an interpolation call site

#### Scenario: Detect statically imported method call

- **WHEN** source code contains `import static interpolation.Interpolator.str;` and calls `str("template")`
- **THEN** the processor identifies this as an interpolation call site

#### Scenario: Ignore unrelated str method calls

- **WHEN** source code contains a call to a method named `str` on a different class
- **THEN** the processor does NOT identify this as an interpolation call site

#### Scenario: Detect multiple calls in same method

- **WHEN** source code contains multiple `str()` calls in the same method
- **THEN** the processor identifies each call as a separate call site with unique index

### Requirement: Template String Extraction

The processor SHALL extract the template string literal from each detected `str()` call site.

#### Scenario: Extract constant string argument

- **WHEN** a `str()` call has a string literal argument like `str("Hello ${name}")`
- **THEN** the processor extracts the template string `"Hello ${name}"`

#### Scenario: Report error for non-constant argument

- **WHEN** a `str()` call has a non-constant argument like `str(someVariable)`
- **THEN** the processor reports a compile error with precise source location

#### Scenario: Report error for null argument

- **WHEN** a `str()` call has a null argument like `str(null)`
- **THEN** the processor reports a compile error indicating null is not allowed

### Requirement: Template Parsing Integration

The processor SHALL parse extracted template strings using `TemplateParserWrapper` and report parsing errors with source location.

#### Scenario: Parse valid template

- **WHEN** a template string is extracted from a `str()` call
- **THEN** the processor parses it using `TemplateParserWrapper.parse()` to extract fragments and variable names

#### Scenario: Report template syntax error

- **WHEN** a template string has invalid syntax (e.g., unclosed `${`)
- **THEN** the processor reports a compile error using `Trees.printMessage()` with source location pointing to the `str()` call

### Requirement: Call Site Metadata Collection

The processor SHALL collect metadata for each detected call site including class name, method context, and call site index.

#### Scenario: Collect class information

- **WHEN** a `str()` call is detected
- **THEN** the processor records the fully qualified class name containing the call

#### Scenario: Collect method information

- **WHEN** a `str()` call is detected inside a method
- **THEN** the processor records the method name and descriptor

#### Scenario: Assign unique call site index

- **WHEN** multiple `str()` calls exist in the same class
- **THEN** each call site receives a unique zero-based index within that class

### Requirement: Diagnostic Reporting

The processor SHALL emit diagnostic messages for detected call sites to enable test verification.

#### Scenario: Emit diagnostic for each call site

- **WHEN** a `str()` call is detected
- **THEN** the processor emits a `Diagnostic.Kind.NOTE` message using `Trees.printMessage()` containing the callSiteIndex in format `[INTERPOLATION:callSiteIndex]`

#### Scenario: Diagnostic points to correct source location

- **WHEN** a diagnostic is emitted for a `str()` call
- **THEN** the diagnostic source location points to the `str()` method invocation element

### Requirement: Metadata Records

The processor SHALL use immutable record types to store collected metadata.

#### Scenario: InterpolatorMetadata record

- **WHEN** a template is parsed
- **THEN** an `InterpolatorMetadata` record stores `String[] fragments` and `List<VarInfo> variables`

#### Scenario: CallSiteInfo record

- **WHEN** a call site is collected
- **THEN** a `CallSiteInfo` record stores `String className`, `String methodName`, `String methodDescriptor`, `int callSiteIndex`, and `InterpolatorMetadata metadata`

#### Scenario: ClassInterpolationData record

- **WHEN** all call sites for a class are collected
- **THEN** a `ClassInterpolationData` record stores `String className` and `List<CallSiteInfo> callSites`
