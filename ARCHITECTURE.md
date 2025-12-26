# Architecture Design Document

## Overview

This project implements compile-time string interpolation for Java 17+ using annotation processing and bytecode transformation, providing a JEP 465-like developer experience without requiring language modifications.

## Goals

1. **Provide inline string interpolation syntax** similar to JEP 465 String Templates
2. **Compile-time template parsing** - parse templates once during compilation, not at runtime
3. **Use only standard and stable APIs** - avoid discouraged AST manipulation practices
4. **Support Java 17+** - using the Classfile API backport
5. **Zero runtime performance overhead** - optimized bytecode generation

## Why This Approach?

### What We Avoid

**AST Manipulation (Lombok/Manifold Style):**

- Heavily criticized for using internal compiler APIs
- Fragile across Java versions
- Requires deep compiler hooks
- Maintenance burden

**Inline Code Modification:**

- Standard annotation processors cannot modify existing source code
- Would require compiler plugins with version-specific implementations

### What We Use Instead

1. **Standard JSR 269 Annotation Processing** - Read AST without modifying it
2. **Classfile API** (via backport) - Official bytecode transformation API
3. **Compile-time data collection** - Build metadata during AST analysis
4. **Post-compilation bytecode transformation** - Transform .class files in final round

## API Design

### User-Facing API

```java
import static interpolation.Interpolator.str;

public class Example {
    public void greet(String name, int count) {
        // User writes valid Java - no custom syntax
        String msg = str("Hello \{name}, you have \{count} items");
    }
}
```

**Key points:**

- `str("Hello \{name}")` is **valid Java** - `\\{` is an escaped brace
- `str()` is a `static` placeholder method - its calls are replaced during compilation. Its implementation throws `UnsupportedOperationException` explaining that annotation processor likely was not invoked to process this file and how to add annotation processor to the java compiler.
- Template variables use `\{varName}` syntax.
- Template variables can reference method's arguments, local variables, class fields. Initially will **not** support arbitrary java expressions like `myVar + 10`, dot property navigation like `myVar.a` or references to imported or fully qualified static variables, but the template string parser should support implementing these features at a later stages, so idea is to use ANTLR or JavaCC parser generator with some existing grammar, maybe JSTL (requires further research).

### Core Types

```java
/**
 * Immutable record holding parsed template data.
 * Instances are created at compile-time and cached in bytecode.
 */
public record Interpolator(
    String[] fragments,      // Template split by variables: ["Hello ", ", you have ", " items"]
    VarInfo[] varInfos      // Compile-time metadata records
) {
    /**
     * Placeholder method - replaced by annotation processor.
     * Never actually called at runtime.
     */
    public static String str(String template) { throws new UnsupportedOperationException("⚠️Interpolator annotation processor is not properly installed!");}

    /**
     * Runtime interpolation method.
     * Combines fragments with provided values.
     */
    public String process(Object... values) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fragments.length; i++) {
            sb.append(fragments[i]);
            if (i < values.length) {
                sb.append(values[i]);
            }
        }
        return sb.toString();
    }
}

/**
 * Variable metadata for compile-time analysis.
 */
record VarInfo(
    String name,              // Variable name from template
    int slot,                 // Local variable slot number as in java bytecode
    boolean isWide,            // true for long/double (occupy 2 slots)
    String typeDescriptor,    // JVM type descriptor of the variable itself
    String fieldOwner        // For fields: owner class (internal name), null for local variables
) {}
```

## How It Works

### Phase 1: Compile-Time (Annotation Processing)

#### Step 1.1: AST Analysis

```java
@SupportedAnnotationTypes("*")
public class InterpolationProcessor extends AbstractProcessor {
    // Instance variables survive across all annotation processing rounds
    private final Map<String, ClassInterpolationData> collectedData = new HashMap<>();

    @Override
    public void init(ProcessingEnvironment env) {
        Trees trees = Trees.instance(env);
        Messager messager = env.getMessager();
        JavacTask task = JavacTask.instance(env);

        // Register listener to analyze AST
        task.addTaskListener(new TaskListener() {
            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ANALYZE) {
                    analyzeCompilationUnit(e.getCompilationUnit());
                }
            }
        });
    }
}
```

**What we collect:**

1. Find all calls to `Interpolator.str("....")`
2. Parse template string → extract fragments and variable names
3. Resolve variables in scope → map to local variable slots or fields
4. Determine variable types using AST type information
5. Store metadata in the `List<CallSiteInfo> callSites` instance variable of the `InterpolationMethodProcessor` class. Call sites in the list will be naturally sorted by the className.

#### Step 1.2: Variable Resolution

For each variable in template `\{varName}`:

1. **Search scope** using `Trees.getScope(TreePath)`
2. **Find VariableElement** for the name
3. **Calculate slot number:**
   - Instance methods: slot 0 = `this`
   - Static methods: slot 0 = first parameter
   - Parameters come first (in order)
   - Local variables follow (in declaration order)
   - `long` and `double` occupy 2 slots
4. **Extract type descriptor** from TypeMirror
5. **Handle fields:** Set `fieldOwner`, track owner class

#### Step 1.3: Error Reporting

Use standard `Messager` API:

```java
messager.printMessage(
    Diagnostic.Kind.ERROR,
    "Variable 'unknownVar' not found in scope",
    element  // Points to exact source location
);
```

Errors appear in IDE and compiler output with precise location.

### Phase 2: Bytecode Transformation

#### Step 2.1: Trigger (processingOver)

```java
@Override
public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
        // Final round - transform bytecode using collected metadata
        transformAllClasses(collectedData);
    }
    return false;
}
```

#### Step 2.2: Transform Each Class

For each class with interpolation calls:

1. **Read compiled .class file** from output directory
2. **Add static field:**
   ```java
   private static final Interpolator[] INTERPOLATORS_$;
   ```
3. **Generate static initializer:**
   ```java
   static {
       INTERPOLATORS_$ = new Interpolator[] {
           new Interpolator(
               new String[] {"Hello ", ", you have ", " items"},
               new VarInfo[] { /* metadata */ }
           ),
           // ... one per call site
       };
   }
   ```
4. **Replace each `str()` call:**

   ```java
   // Original bytecode:
   ldc "Hello \\{name}, you have \\{count} items"
   invokestatic Interpolator.str(String)String

   // Transformed bytecode:
   getstatic MyClass.INTERPOLATORS_$:[LInterpolator;
   iconst_0                           // Call site index
   aaload                             // Get Interpolator instance
   iconst_2                           // Number of variables
   anewarray Object                   // Create Object[] for varargs
   dup
   iconst_0
   aload_1                            // Load 'name' from slot 1
   aastore
   dup
   iconst_1
   iload_2                            // Load 'count' from slot 2
   invokestatic Integer.valueOf(I)LInteger;  // Box primitive
   aastore
   invokevirtual Interpolator.process([Object)String
   ```

5. **Write transformed .class file** back to output directory

### Data Structures

```java
// Parsed template with variable metadata
record InterpolatorMetadata(
    String[] fragments,
    List<VarInfo> variables
) {}

// Information about a specific str() call site
record CallSiteInfo(
    String className,              // Fully qualified name
    String methodName,             // Containing method
    String methodDescriptor,       // Method signature
    int callSiteIndex,            // Index within this method (0, 1, 2...)
    InterpolatorMetadata metadata
) {}

// All interpolation data for a class
record ClassInterpolationData(
    String className,
    List<CallSiteInfo> callSites
) {}
```

## Example Transformation

### Input (User Code)

```java
package com.example;

public class Greeter {
    public String greet(String name, int messageCount) {
        String msg = Interpolator.str("Hello \\{name}, you have \\{messageCount} messages");
        return msg;
    }
}
```

### Output (Generated Bytecode - Conceptual)

```java
package com.example;

public class Greeter {
    // Generated field
    private static final Interpolator[] INTERPOLATORS_$;

    // Generated static initializer
    static {
        INTERPOLATORS_$ = new Interpolator[] {
            new Interpolator(
                new String[] {"Hello ", ", you have ", " messages"},
                new VarInfo[] {
                    new VarInfo("name", 1, "Ljava/lang/String;", false, null, false),
                    new VarInfo("messageCount", 2, "I", false, null, false)
                }
            )
        };
    }

    public String greet(String name, int messageCount) {
        // Transformed call
        String msg = INTERPOLATORS_$[0].process(name, messageCount);
        return msg;
    }
}
```

## Benefits

### Performance

- ✅ **Template parsed once** at compile-time, not every execution
- ✅ **Cached in bytecode** as constant data
- ✅ **Zero parsing overhead** at runtime
- ✅ **Efficient concatenation** in `process()` method
- ✅ **Future optimization potential** (invokedynamic, etc.)

### Maintainability

- ✅ **Standard APIs only** - JSR 269 + Classfile API
- ✅ **No internal compiler dependencies**
- ✅ **Forward compatible** with Java version updates
- ✅ **Clear separation** between compile-time and runtime

### Developer Experience

- ✅ **Valid Java syntax** - no custom syntax errors
- ✅ **IDE support** - code compiles normally
- ✅ **Compile-time errors** with precise source locations
- ✅ **Type-safe** - variables validated at compile-time

### Design Quality

- ✅ **Immutable data structures** using records
- ✅ **Similar to JEP 430/465** design philosophy
- ✅ **Clean architecture** - well-separated concerns

## Trade-offs

### Limitations

- ⚠️ **Not inline syntax** - requires `str("template")` wrapper
- ⚠️ **Java 17+ only** (using Classfile API backport)
- ⚠️ **Compile-time dependency** - annotation processor must run
- ⚠️ **Simple expressions only** (initially) - `\{var}` not `\{obj.method()}`

### Complexity

- ⚠️ **Two-phase processing** - AST analysis + bytecode transformation
- ⚠️ **Slot calculation** - must track variable locations precisely
- ⚠️ **Scope analysis** - proper variable resolution required

### Memory

- ⚠️ **Static array per class** - minimal overhead
- ⚠️ **One-time initialization cost** - in `<clinit>`, negligible

## Future Enhancements

### Expression Support

Support complex expressions beyond simple variables:

```java
str("User: \{user.getName()}")
str("Sum: \{a + b}")
str("Formatted: \{String.format("%d", value)}")
```

Requires:

- Expression AST parsing
- Bytecode generation for expression evaluation
- More complex but achievable

### Optimization: invokedynamic

Replace `Object[]` varargs with `invokedynamic` for zero-allocation:

```java
invokedynamic process(String,Object,int)String [
    BootstrapMethod: StringConcatFactory.makeConcat
]
```

Benefits:

- No Object[] allocation
- Potentially inlined by JIT
- Even better performance

### IDE Plugin

Syntax highlighting for `\{variables}` inside templates

- IntelliJ IDEA plugin
- VS Code extension
- Show inline hints for variable types

## Technology Stack

- **Java 17+** - Target platform
- **JSR 269** - Standard Annotation Processing API
- **Classfile API Backport** - https://github.com/dmlloyd/jdk-classfile-backport
- **APTK** - Annotation Processing Toolkit (existing in project)
- **TreePathScanner** - AST traversal
- **Trees/Scope API** - Variable resolution

## References

- **JEP 465**: String Templates (Third Preview) - https://openjdk.org/jeps/465
- **JEP 457**: Class-File API (Preview) - https://openjdk.org/jeps/457
- **JSR 269**: Pluggable Annotation Processing API
- **Classfile API Backport**: https://github.com/dmlloyd/jdk-classfile-backport
