# Project Implementation TODO

This document outlines all tasks required to complete the string interpolation annotation processor project.

## Phase 1: Core API & Data Structures

### 1.1 Interpolator Record

- [x] Create `Interpolator` record in `interpolation-api` module
  - [x] Add `String[] fragments` field
  - [x] Add `VarInfo[] varInfos` field (compile-time metadata)
  - [x] Implement `static String str(String template)` placeholder method
  - [x] Implement `String process(Object... values)` runtime method
  - [x] Add proper javadoc comments
  - [ ] Write unit tests for `process()` method

### 1.2 Metadata Records

- [x] Create `VarInfo` record in processor module

  - [x] Fields: `String name`, `int slot`, `String typeDescriptor`, `String fieldOwner`, `boolean isWide`
  - [ ] Add validation (e.g., slot >= 0)
  - [ ] Add javadoc

- [ ] Create `InterpolatorMetadata` record

  - [ ] Fields: `String[] fragments`, `List<VarInfo> variables`
  - [ ] Add factory method for parsing templates
  - [ ] Add javadoc

- [ ] Create `CallSiteInfo` record

  - [ ] Fields: `String className`, `String methodName`, `String methodDescriptor`, `int callSiteIndex`, `InterpolatorMetadata metadata`
  - [ ] Add javadoc

- [ ] Create `ClassInterpolationData` record
  - [ ] Fields: `String className`, `List<CallSiteInfo> callSites`
  - [ ] Add javadoc

## Phase 2: Template Parsing

### 2.1 Template Parser

- [ ] Create `TemplateParser` class
  - [ ] Implement parsing logic to extract `\\{varName}` patterns
  - [ ] Handle escape sequences properly (`\\\\`, `\\{`, `\\}`)
  - [ ] Split template into fragments array
  - [ ] Extract variable names list
  - [ ] Add comprehensive error handling for malformed templates
  - [ ] Write unit tests with edge cases:
    - [ ] Empty template
    - [ ] No variables
    - [ ] Multiple variables
    - [ ] Adjacent variables: `\\{a}\\{b}`
    - [ ] Escaped braces: `\\\\{notavar}`
    - [ ] Unicode characters
    - [ ] Very long templates

### 2.2 Template Validation

- [ ] Validate template syntax
  - [ ] Check for unmatched braces
  - [ ] Check for empty variable names `\\{}`
  - [ ] Check for invalid variable name characters
  - [ ] Report clear error messages with position info

## Phase 3: AST Analysis

### 3.1 AST Scanner Setup

- [ ] Update `InterpolationProcessor` to scan for `Interpolator.str()` calls
  - [ ] Add `Trees trees` field
  - [ ] Add `Messager messager` field
  - [ ] Add `Map<String, ClassInterpolationData> collectedData` instance variable
  - [ ] Implement `init()` method with `JavacTask` listener
  - [ ] Register `TaskListener` for `TaskEvent.Kind.ANALYZE` events

### 3.2 Method Invocation Detection

- [ ] Create `TreePathScanner` to traverse AST
  - [ ] Override `visitMethodInvocation(MethodInvocationTree, Void)`
  - [ ] Implement `isInterpolatorStrCall()` helper
    - [ ] Check if method name is "str"
    - [ ] Check if receiver is `Interpolator` class
    - [ ] Handle static imports
  - [ ] Extract template string argument
  - [ ] Handle non-constant template strings (report error)

### 3.3 Variable Resolution

- [ ] Create `VariableResolver` class
  - [ ] Implement `resolveVariable(String varName, TreePath context)` method
  - [ ] Use `Trees.getScope(TreePath)` to get scope
  - [ ] Search scope for variable by name
  - [ ] Distinguish between local variables, parameters, and fields
  - [ ] Handle shadowing correctly
  - [ ] Report error if variable not found

### 3.4 Slot Calculation

- [ ] Create `SlotCalculator` class
  - [ ] Implement `calculateSlot(VariableElement var, MethodTree method)` method
  - [ ] Handle instance methods (`this` takes slot 0)
  - [ ] Handle static methods (parameters start at slot 0)
  - [ ] Account for parameter order
  - [ ] Account for local variable declaration order
  - [ ] Handle wide types (`long`, `double` take 2 slots)
  - [ ] Write unit tests for various method signatures

### 3.5 Type Information

- [ ] Create `TypeDescriptorBuilder` class
  - [ ] Convert `TypeMirror` to JVM type descriptor
  - [ ] Handle primitives: `int` → `I`, `boolean` → `Z`, etc.
  - [ ] Handle object types: `String` → `Ljava/lang/String;`
  - [ ] Handle arrays: `int[]` → `[I`
  - [ ] Handle generics (erasure)
  - [ ] Write unit tests for all type kinds

### 3.6 Field Handling

- [ ] Support instance field references
  - [ ] Detect when variable is a field
  - [ ] Store field owner class (internal name)
  - [ ] Set `isField = true` in `VarInfo`
- [ ] Support static field references
  - [ ] Same as instance fields, track owner class

### 3.7 Error Reporting

- [ ] Implement comprehensive error messages using `Messager`
  - [ ] Variable not found in scope
  - [ ] Template syntax errors
  - [ ] Non-constant template string
  - [ ] Empty template
  - [ ] All errors point to exact source location using `Element`

## Phase 4: Bytecode Transformation

### 4.1 Classfile API Integration

- [ ] Add dependency on `jdk-classfile-backport`
- [ ] Create `BytecodeTransformer` class
  - [ ] Implement `transformClass(String className, List<CallSiteInfo> callSites)` method
  - [ ] Read compiled .class file from output directory
  - [ ] Use `ClassFile.of().transform()` for transformation
  - [ ] Write transformed bytecode back to .class file

### 4.2 Static Field Generation

- [ ] Generate `private static final Interpolator[] INTERPOLATORS_$` field
  - [ ] Use `ClassBuilder.withField()`
  - [ ] Set correct access flags: `ACC_PRIVATE | ACC_STATIC | ACC_FINAL`
  - [ ] Set type descriptor: `[Linterpolation/Interpolator;`

### 4.3 Static Initializer Generation

- [ ] Generate or modify `<clinit>` method
  - [ ] Create `new Interpolator[]` array
  - [ ] For each call site:
    - [ ] Create `String[]` for fragments
    - [ ] Create `VarInfo[]` for metadata
    - [ ] Call `new Interpolator(fragments, varInfos)`
    - [ ] Store in array at correct index
  - [ ] Assign array to `INTERPOLATORS_$` field
  - [ ] Handle case where `<clinit>` already exists (merge)

### 4.4 Method Call Replacement

- [ ] Transform methods containing `str()` calls
  - [ ] Use `MethodBuilder.transformCode()`
  - [ ] Detect `invokestatic Interpolator.str(String)String` instructions
  - [ ] Replace with:
    - [ ] `getstatic INTERPOLATORS_$` - load array
    - [ ] `iconst_N` or `bipush N` - call site index
    - [ ] `aaload` - get Interpolator instance
    - [ ] `iconst_N` - number of variables
    - [ ] `anewarray Object` - create Object[] for varargs
    - [ ] For each variable:
      - [ ] `dup` array
      - [ ] `iconst_N` - array index
      - [ ] `aload_N` / `iload_N` / etc. - load variable from slot
      - [ ] Box primitives: `invokestatic Integer.valueOf(I)LInteger;`
      - [ ] Handle fields: `getfield` or `getstatic`
      - [ ] `aastore` - store in array
    - [ ] `invokevirtual Interpolator.process([Object)String` - call process
  - [ ] Remove original `ldc "template"` instruction

### 4.5 Instruction Helpers

- [ ] Create helper methods for bytecode generation
  - [ ] `loadConstant(int)` - generate correct constant load instruction
  - [ ] `loadVariable(VarInfo)` - generate load instruction based on type and slot
  - [ ] `boxPrimitive(String typeDescriptor)` - generate boxing call
  - [ ] `loadField(VarInfo)` - generate field access instruction

### 4.6 Edge Cases

- [ ] Handle multiple `str()` calls in same method
  - [ ] Track call site index correctly
  - [ ] Ensure each gets correct Interpolator from array
- [ ] Handle nested method calls
  - [ ] `str("outer \\{str("inner")}")` - should this be allowed? (probably error)
- [ ] Handle `str()` in constructors
  - [ ] Account for uninitialized `this`
- [ ] Handle `str()` in static initializers
  - [ ] May need special handling

## Phase 5: Integration & Testing

### 5.1 Processor Integration

- [ ] Update `InterpolationMethodProcessor.process()` method
  - [ ] Call AST analysis in non-final rounds
  - [ ] Call bytecode transformation in `processingOver()` round
  - [ ] Handle errors gracefully
  - [ ] Skip transformation if compilation errors occurred

### 5.2 Unit Tests

- [ ] Template parser tests (already outlined in 2.1)
- [ ] Variable resolver tests
  - [ ] Local variables
  - [ ] Parameters
  - [ ] Instance fields
  - [ ] Static fields
  - [ ] Shadowing scenarios
- [ ] Slot calculator tests
  - [ ] Instance methods
  - [ ] Static methods
  - [ ] Wide types
  - [ ] Multiple parameters and locals
- [ ] Type descriptor builder tests (already outlined in 3.5)

### 5.3 Integration Tests

- [ ] Create test cases in `interpolation-processor-integrationTest`
  - [ ] Simple variable interpolation: `str("Hello \\{name}")`
  - [ ] Multiple variables: `str("\\{a} + \\{b} = \\{c}")`
  - [ ] All primitive types: `int`, `long`, `double`, `boolean`, etc.
  - [ ] Object types: `String`, custom classes
  - [ ] Instance fields
  - [ ] Static fields
  - [ ] Parameters vs local variables
  - [ ] Multiple calls in same method
  - [ ] Multiple calls in different methods
  - [ ] Static methods
  - [ ] Constructors
  - [ ] Inner classes
  - [ ] Edge case: Empty string `str("")`
  - [ ] Edge case: No variables `str("literal")`
  - [ ] Edge case: Only variable `str("\\{x}")`
  - [ ] Edge case: Adjacent variables `str("\\{a}\\{b}")`
  - [ ] Unicode in template
  - [ ] Escaped braces `str("\\\\{ not a var }")`

### 5.4 Error Case Tests

- [ ] Variable not found
- [ ] Malformed template
- [ ] Non-constant template string
- [ ] Empty variable name `\\{}`
- [ ] Unmatched braces

### 5.5 Example Project

- [ ] Update `interpolation-processor-example` with real use cases
  - [ ] Simple greeting example
  - [ ] Logging with multiple variables
  - [ ] Complex template with many variables
  - [ ] Show performance characteristics
  - [ ] Create README with usage instructions

## Phase 6: Documentation

### 6.1 Code Documentation

- [ ] Add comprehensive javadoc to all public APIs
- [ ] Add package-info.java with overview
- [ ] Document thread safety (immutable records are thread-safe)
- [ ] Document performance characteristics

### 6.2 User Documentation

- [ ] Create/update README.md

  - [ ] Quick start guide
  - [ ] Installation instructions (Maven/Gradle)
  - [ ] Basic usage examples
  - [ ] Advanced examples
  - [ ] Troubleshooting section
  - [ ] FAQ
  - [ ] Performance notes
  - [ ] Comparison with alternatives

- [ ] Create USAGE.md with detailed guide
  - [ ] Template syntax reference
  - [ ] Supported variable types
  - [ ] Escape sequences
  - [ ] Error messages and solutions
  - [ ] IDE setup
  - [ ] Build tool configuration

### 6.3 Developer Documentation

- [ ] Update ARCHITECTURE.md if design changes
- [ ] Create CONTRIBUTING.md
  - [ ] How to build the project
  - [ ] How to run tests
  - [ ] Code style guidelines
  - [ ] How to contribute

## Phase 7: Build & Release

### 7.1 Build Configuration

- [ ] Configure Maven/Gradle build
  - [ ] Ensure annotation processor is properly registered
  - [ ] Configure compiler arguments if needed
  - [ ] Set up source/target Java versions (17+)
  - [ ] Configure dependencies correctly

### 7.2 Testing Infrastructure

- [ ] Set up CI/CD (GitHub Actions or similar)
  - [ ] Run tests on multiple Java versions (17, 21, 22+)
  - [ ] Run on multiple platforms (Linux, Windows, macOS)
  - [ ] Code coverage reporting

### 7.3 Release Preparation

- [ ] Version numbering scheme
- [ ] CHANGELOG.md
- [ ] License verification (GPL v3 confirmed)
- [ ] Maven Central publishing setup (if applicable)

## Phase 8: Future Enhancements (Optional)

### 8.1 Expression Support

- [ ] Design expression syntax: `\\{user.getName()}`
- [ ] Parse expression AST
- [ ] Generate bytecode for expression evaluation
- [ ] Handle method calls, field access, operators
- [ ] Add comprehensive tests

### 8.2 Performance Optimizations

- [ ] Investigate `invokedynamic` for concatenation
  - [ ] Use `StringConcatFactory.makeConcat()`
  - [ ] Eliminate Object[] allocation
- [ ] Benchmark against alternatives
  - [ ] String.format()
  - [ ] MessageFormat
  - [ ] Manual concatenation
  - [ ] StringBuilder
- [ ] Optimize fragment storage (intern strings?)

### 8.3 IDE Plugin

- [ ] IntelliJ IDEA plugin
  - [ ] Syntax highlighting for `\\{variables}`
  - [ ] Code completion inside templates
  - [ ] Error highlighting
  - [ ] Quick fixes
- [ ] VS Code extension
  - [ ] Similar features

### 8.4 Advanced Features

- [ ] Format specifiers: `\\{value:%.2f}`
- [ ] Null handling options
- [ ] Custom interpolation strategies
- [ ] Multiline templates with indentation handling

## Milestones

### Milestone 1: Core Implementation (Phases 1-4)

Complete basic interpolation with simple variables.

### Milestone 2: Testing & Polish (Phase 5)

All tests passing, integration verified.

### Milestone 3: Documentation & Release (Phases 6-7)

Ready for public use.

### Milestone 4: Advanced Features (Phase 8)

Optional enhancements based on user feedback.

## Notes

- Prioritize correctness over performance initially
- Write tests early and often
- Keep immutable data structures throughout
- Use Java records wherever appropriate
- Ensure all error messages are clear and actionable
