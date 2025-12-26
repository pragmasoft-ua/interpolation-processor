## ADDED Requirements

### Requirement: Variable Interpolation Syntax

The template parser SHALL recognize `${varName}` as a variable placeholder, where `varName` is a valid Java identifier.

#### Scenario: Simple variable substitution
- **WHEN** a template contains `${name}`
- **THEN** the parser extracts `name` as a variable reference

#### Scenario: Multiple variables in template
- **WHEN** a template contains `Hello ${firstName} ${lastName}`
- **THEN** the parser extracts fragments `["Hello ", " "]` and variables `["firstName", "lastName"]`

#### Scenario: Adjacent variables
- **WHEN** a template contains `${a}${b}`
- **THEN** the parser extracts fragments `["", ""]` and variables `["a", "b"]`

#### Scenario: Variable at start of template
- **WHEN** a template contains `${greeting} World`
- **THEN** the parser extracts fragments `["", " World"]` and variables `["greeting"]`

#### Scenario: Variable at end of template
- **WHEN** a template contains `Hello ${name}`
- **THEN** the parser extracts fragments `["Hello ", ""]` and variables `["name"]`

### Requirement: Escape Sequence

The template parser SHALL recognize `$${` as an escape sequence that produces a literal `${` in the output. A single `$` not followed by `{` is treated as a literal `$` character and requires no escaping.

#### Scenario: Literal dollar-brace sequence
- **WHEN** a template contains `Use $${varName} syntax`
- **THEN** the output contains literal text `Use ${varName} syntax`

#### Scenario: Escaped dollar-brace before variable content
- **WHEN** a template contains `Price: $${amount}`
- **THEN** the output contains literal text `Price: ${amount}`

#### Scenario: Single dollar without brace
- **WHEN** a template contains `Cost: $100`
- **THEN** the output contains literal text `Cost: $100`

#### Scenario: Double dollar without brace
- **WHEN** a template contains `Pay $$`
- **THEN** the output contains literal text `Pay $$`

### Requirement: Template Text Handling

The template parser SHALL preserve all text outside of `${...}` expressions and escape sequences exactly as written.

#### Scenario: Plain text without variables
- **WHEN** a template contains `Hello World`
- **THEN** the parser produces a single fragment `Hello World` with no variables

#### Scenario: Special characters preserved
- **WHEN** a template contains `Line1\nLine2\t${var}`
- **THEN** the parser preserves the newline and tab characters in the fragment

#### Scenario: Unicode text preserved
- **WHEN** a template contains `Hello ${name} 你好 مرحبا`
- **THEN** the parser preserves all Unicode characters in fragments

### Requirement: Syntax Error Reporting

The template parser SHALL report clear error messages with position information for malformed templates.

#### Scenario: Unclosed variable expression
- **WHEN** a template contains `Hello ${name`
- **THEN** the parser reports an error indicating unclosed `${` at the position

#### Scenario: Empty variable name
- **WHEN** a template contains `Hello ${}`
- **THEN** the parser reports an error indicating empty variable name

#### Scenario: Invalid variable name characters
- **WHEN** a template contains `Hello ${123invalid}`
- **THEN** the parser reports an error indicating invalid identifier

### Requirement: ANTLR-Based Parser Implementation

The template parser SHALL be implemented using ANTLR with lexer modes to handle both template structure and expression parsing in a single pass.

#### Scenario: Nested braces in future expressions
- **WHEN** a future template contains `${map.get("}")}`
- **THEN** the parser correctly identifies the expression boundaries despite the `}` inside the string literal

#### Scenario: Parser extensibility
- **WHEN** the grammar is extended to support expressions like `${a + b}`
- **THEN** existing simple variable templates continue to work unchanged
