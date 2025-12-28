# Change: Detect Interpolator.str() Method Invocations

## Why

The annotation processor needs to detect calls to `Interpolator.str()` in user code to collect metadata required for bytecode transformation. This is a foundational capability that enables the compile-time parsing and variable resolution phases outlined in the architecture.

## What Changes

- Add AST scanning infrastructure to `InterpolationProcessor` using `JavacTask` and `TaskListener`
- Create `TreePathScanner` to traverse AST and detect `Interpolator.str()` method invocations
- Parse template strings from detected call sites using `TemplateParserWrapper`
- Emit diagnostic messages with `callSiteIndex` to enable verification in unit tests
- Move `VarInfo` record to processor module (not used in runtime API)
- Create metadata records (`InterpolatorMetadata`, `CallSiteInfo`, `ClassInterpolationData`)

## Impact

- Affected specs: Creates new `ast-analysis` capability
- Affected code:
  - `annotation-processor/src/main/java/interpolation/processor/InterpolationProcessor.java`
  - New: `annotation-processor/src/main/java/interpolation/processor/InterpolatorStrScanner.java`
  - New: `annotation-processor/src/main/java/interpolation/processor/CallSiteInfo.java`
  - New: `annotation-processor/src/main/java/interpolation/processor/ClassInterpolationData.java`
  - New: `annotation-processor/src/main/java/interpolation/processor/InterpolatorMetadata.java`
  - Move: `interpolation-api/src/main/java/interpolation/VarInfo.java` to processor module
  - `annotation-processor/src/test/java/interpolation/processor/InterpolationProcessorTest.java`
  - New test resources for various call site scenarios
