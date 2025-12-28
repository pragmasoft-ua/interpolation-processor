# Tasks for detect-method-invocations

## 1. Phase 1 Completion: Metadata Records

- [ ] 1.1 Move `VarInfo` record from `interpolation-api` to `annotation-processor` module
  - Copy `VarInfo.java` to `interpolation/processor/VarInfo.java`
  - Update package declaration
  - Remove original from interpolation-api (VarInfo is not used by Interpolator at runtime)
  - Update `Interpolator.java` to use `Object[]` instead of `VarInfo[]` for the varInfos field (since VarInfo metadata is only needed at compile-time)

- [ ] 1.2 Create `InterpolatorMetadata` record in processor module
  - Fields: `String[] fragments`, `List<VarInfo> variables`
  - Add factory method `of(ParsedTemplate template)` for easy creation from parser output
  - Add javadoc documentation

- [ ] 1.3 Create `CallSiteInfo` record in processor module
  - Fields: `String className`, `String methodName`, `String methodDescriptor`, `int callSiteIndex`, `InterpolatorMetadata metadata`
  - Add javadoc documentation

- [ ] 1.4 Create `ClassInterpolationData` record in processor module
  - Fields: `String className`, `List<CallSiteInfo> callSites`
  - Add javadoc documentation

## 2. AST Scanner Infrastructure (Phase 3.1)

- [ ] 2.1 Update `InterpolationProcessor.init()` to set up AST analysis
  - Add `Trees trees` field (obtain via `Trees.instance(processingEnv)`)
  - Add `Map<String, ClassInterpolationData> collectedData` field
  - Obtain `JavacTask` via `JavacTask.instance(processingEnv)`
  - Register `TaskListener` for `ANALYZE` events

- [ ] 2.2 Create `TaskListener` implementation
  - Handle `TaskEvent.Kind.ANALYZE` events
  - Invoke AST scanner on the compilation unit
  - Guard against processing same unit multiple times

## 3. Method Invocation Detection (Phase 3.2)

- [ ] 3.1 Create `InterpolatorStrScanner` class extending `TreePathScanner<Void, Void>`
  - Accept `Trees` instance and collector callback in constructor
  - Store current class name and method context during traversal

- [ ] 3.2 Implement `visitClass()` to track containing class
  - Record fully qualified class name
  - Reset method context for each class

- [ ] 3.3 Implement `visitMethod()` to track containing method
  - Record method name
  - Build method descriptor from parameters and return type

- [ ] 3.4 Implement `visitMethodInvocation()` to detect `str()` calls
  - Check if invoked method is `Interpolator.str` or statically imported `str`
  - Use `Trees.getElement()` to resolve the method being called
  - Compare against `interpolation.Interpolator.str` qualified name

- [ ] 3.5 Implement `isInterpolatorStrCall()` helper method
  - Resolve method element from invocation tree
  - Check if owner is `interpolation.Interpolator`
  - Check if method name is `str`
  - Check if signature matches `str(String)`

- [ ] 3.6 Extract template string from method invocation
  - Get first argument from `MethodInvocationTree.getArguments()`
  - Check if argument is a `LiteralTree` with String kind
  - Report error if not a compile-time constant string

## 4. Template Parsing and Metadata Collection

- [ ] 4.1 Parse extracted template using `TemplateParserWrapper`
  - Call `TemplateParserWrapper.parse(templateString)`
  - Catch `TemplateParseException` and report via `Trees.printMessage()`

- [ ] 4.2 Build `InterpolatorMetadata` from parsed template
  - Create record with fragments and empty variables list (variable resolution comes later)

- [ ] 4.3 Build `CallSiteInfo` for each detected call site
  - Combine class name, method name, method descriptor, call site index, and metadata
  - Increment call site index for each call in the class

- [ ] 4.4 Aggregate call sites into `ClassInterpolationData`
  - Group call sites by class name
  - Store in `collectedData` map

## 5. Diagnostic Reporting

- [ ] 5.1 Emit diagnostic message for each detected call site
  - Use `Trees.printMessage(Diagnostic.Kind.NOTE, message, element)`
  - Message format: `[INTERPOLATION:N]` where N is the call site index
  - Element should point to the method invocation expression

## 6. Unit Tests

- [ ] 6.1 Create test resource `TestcaseMultipleStrCalls.java`
  - Include multiple `str()` calls in same method
  - Include calls in different methods
  - Include static import usage

- [ ] 6.2 Create test resource `TestcaseNonConstantTemplate.java`
  - Include `str()` call with variable argument (should produce error)

- [ ] 6.3 Create test resource `TestcaseMalformedTemplate.java`
  - Include `str()` call with malformed template like `str("Hello ${name")`

- [ ] 6.4 Create test resource `TestcaseNoStrCalls.java`
  - File with no `str()` calls (should compile without issues)

- [ ] 6.5 Create test resource `TestcaseUnrelatedStrMethod.java`
  - Include call to `str()` method on different class (should not be detected)

- [ ] 6.6 Write test `test_detects_single_str_call`
  - Verify diagnostic message `[INTERPOLATION:0]` is emitted

- [ ] 6.7 Write test `test_detects_multiple_str_calls`
  - Verify diagnostic messages `[INTERPOLATION:0]`, `[INTERPOLATION:1]`, etc. are emitted

- [ ] 6.8 Write test `test_detects_statically_imported_str`
  - Verify static import `import static interpolation.Interpolator.str` is detected

- [ ] 6.9 Write test `test_reports_error_for_non_constant_template`
  - Verify compilation fails with appropriate error message

- [ ] 6.10 Write test `test_reports_error_for_malformed_template`
  - Verify compilation fails with template syntax error

- [ ] 6.11 Write test `test_ignores_unrelated_str_methods`
  - Verify no diagnostics for calls to `SomeOtherClass.str()`

- [ ] 6.12 Write unit tests for `Interpolator.process()` method
  - Test with empty fragments/values
  - Test with multiple variables
  - Test with null values in array
