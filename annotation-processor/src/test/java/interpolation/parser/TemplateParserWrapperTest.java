package interpolation.parser;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

/**
 * Unit tests for {@link TemplateParserWrapper}.
 */
public class TemplateParserWrapperTest {

    // 4.1 Test simple variable: `Hello ${name}`
    @Test
    public void parseSimpleVariable() {
        ParsedTemplate result = TemplateParserWrapper.parse("Hello ${name}");

        assertThat(result.fragments(), is(new String[]{"Hello ", ""}));
        assertThat(result.varNames(), is(new String[]{"name"}));
    }

    // 4.2 Test multiple variables: `${a} and ${b}`
    @Test
    public void parseMultipleVariables() {
        ParsedTemplate result = TemplateParserWrapper.parse("${a} and ${b}");

        assertThat(result.fragments(), is(new String[]{"", " and ", ""}));
        assertThat(result.varNames(), is(new String[]{"a", "b"}));
    }

    // 4.3 Test adjacent variables: `${a}${b}`
    @Test
    public void parseAdjacentVariables() {
        ParsedTemplate result = TemplateParserWrapper.parse("${a}${b}");

        assertThat(result.fragments(), is(new String[]{"", "", ""}));
        assertThat(result.varNames(), is(new String[]{"a", "b"}));
    }

    // 4.4 Test escape sequence: `$${literal}`
    @Test
    public void parseEscapeSequence() {
        ParsedTemplate result = TemplateParserWrapper.parse("Use $${varName} syntax");

        assertThat(result.fragments(), is(new String[]{"Use ${varName} syntax"}));
        assertThat(result.varNames(), is(new String[]{}));
    }

    // 4.5 Test no variables: `plain text`
    @Test
    public void parsePlainText() {
        ParsedTemplate result = TemplateParserWrapper.parse("plain text");

        assertThat(result.fragments(), is(new String[]{"plain text"}));
        assertThat(result.varNames(), is(new String[]{}));
    }

    // 4.6 Test empty template: ``
    @Test
    public void parseEmptyTemplate() {
        ParsedTemplate result = TemplateParserWrapper.parse("");

        assertThat(result.fragments(), is(new String[]{""}));
        assertThat(result.varNames(), is(new String[]{}));
    }

    // 4.7 Test Unicode: `это ${name}`
    @Test
    public void parseUnicode() {
        ParsedTemplate result = TemplateParserWrapper.parse("это ${name} 你好 مرحبا");

        assertThat(result.fragments(), is(new String[]{"это ", " 你好 مرحبا"}));
        assertThat(result.varNames(), is(new String[]{"name"}));
    }

    // 4.8 Test error: unclosed `${name`
    @Test
    public void parseUnclosedExpression() {
        TemplateParseException ex = assertThrows(
                TemplateParseException.class,
                () -> TemplateParserWrapper.parse("Hello ${name")
        );

        // Verify we get a parse exception with position info
        assertThat(ex.getLine(), is(1));
    }

    // 4.9 Test error: empty `${}`
    @Test
    public void parseEmptyVariableName() {
        TemplateParseException ex = assertThrows(
                TemplateParseException.class,
                () -> TemplateParserWrapper.parse("Hello ${}")
        );

        // Verify we get a parse exception with position info
        assertThat(ex.getLine(), is(1));
    }

    // 4.10 Test error: invalid identifier `${123}`
    @Test
    public void parseInvalidIdentifier() {
        TemplateParseException ex = assertThrows(
                TemplateParseException.class,
                () -> TemplateParserWrapper.parse("Hello ${123}")
        );

        // Should fail because 123 is not a valid identifier
        assertThat(ex.getLine(), is(1));
    }

    // Additional edge case tests from spec.md

    @Test
    public void parseVariableAtStart() {
        ParsedTemplate result = TemplateParserWrapper.parse("${greeting} World");

        assertThat(result.fragments(), is(new String[]{"", " World"}));
        assertThat(result.varNames(), is(new String[]{"greeting"}));
    }

    @Test
    public void parseVariableAtEnd() {
        ParsedTemplate result = TemplateParserWrapper.parse("Hello ${name}");

        assertThat(result.fragments(), is(new String[]{"Hello ", ""}));
        assertThat(result.varNames(), is(new String[]{"name"}));
    }

    @Test
    public void parseSingleDollarWithoutBrace() {
        ParsedTemplate result = TemplateParserWrapper.parse("Cost: $100");

        assertThat(result.fragments(), is(new String[]{"Cost: $100"}));
        assertThat(result.varNames(), is(new String[]{}));
    }

    @Test
    public void parseDoubleDollarWithoutBrace() {
        ParsedTemplate result = TemplateParserWrapper.parse("Pay $$");

        // $$ NOT followed by { remains as literal $$
        assertThat(result.fragments(), is(new String[]{"Pay $$"}));
        assertThat(result.varNames(), is(new String[]{}));
    }

    @Test
    public void parseEscapedDollarBraceBeforeVariableContent() {
        ParsedTemplate result = TemplateParserWrapper.parse("Price: $${amount}");

        assertThat(result.fragments(), is(new String[]{"Price: ${amount}"}));
        assertThat(result.varNames(), is(new String[]{}));
    }

    @Test
    public void parseSpecialCharactersPreserved() {
        ParsedTemplate result = TemplateParserWrapper.parse("Line1\nLine2\t${var}");

        assertThat(result.fragments(), is(new String[]{"Line1\nLine2\t", ""}));
        assertThat(result.varNames(), is(new String[]{"var"}));
    }

    @Test
    public void parseComplexTemplate() {
        ParsedTemplate result = TemplateParserWrapper.parse(
                "Hello ${firstName} ${lastName}, you have ${count} messages. Cost: $${price}"
        );

        assertThat(result.fragments(), is(new String[]{
                "Hello ", " ", ", you have ", " messages. Cost: ${price}"
        }));
        assertThat(result.varNames(), is(new String[]{"firstName", "lastName", "count"}));
    }

    @Test
    public void parseOnlyVariable() {
        ParsedTemplate result = TemplateParserWrapper.parse("${x}");

        assertThat(result.fragments(), is(new String[]{"", ""}));
        assertThat(result.varNames(), is(new String[]{"x"}));
    }

    @Test
    public void parseUnderscoreInVariableName() {
        ParsedTemplate result = TemplateParserWrapper.parse("${my_var}");

        assertThat(result.fragments(), is(new String[]{"", ""}));
        assertThat(result.varNames(), is(new String[]{"my_var"}));
    }

    @Test
    public void parseNumbersInVariableName() {
        ParsedTemplate result = TemplateParserWrapper.parse("${var123}");

        assertThat(result.fragments(), is(new String[]{"", ""}));
        assertThat(result.varNames(), is(new String[]{"var123"}));
    }

    @Test
    public void parseNullTemplate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TemplateParserWrapper.parse(null)
        );
    }

    @Test
    public void parseDollarAtEndOfTemplate() {
        ParsedTemplate result = TemplateParserWrapper.parse("Price: $");

        assertThat(result.fragments(), is(new String[]{"Price: $"}));
        assertThat(result.varNames(), is(new String[]{}));
    }

    @Test
    public void parseMultipleEscapes() {
        ParsedTemplate result = TemplateParserWrapper.parse("$${a} and $${b}");

        assertThat(result.fragments(), is(new String[]{"${a} and ${b}"}));
        assertThat(result.varNames(), is(new String[]{}));
    }

    @Test
    public void parseTripleDollar() {
        ParsedTemplate result = TemplateParserWrapper.parse("$$$");

        // $$$ is just three dollar signs, no escape (no brace follows)
        assertThat(result.fragments(), is(new String[]{"$$$"}));
        assertThat(result.varNames(), is(new String[]{}));
    }

    @Test
    public void parseTripleDollarWithBrace() {
        ParsedTemplate result = TemplateParserWrapper.parse("$$${name}");

        // First $ is literal text, then $${ is escape (produces ${), then name} is literal
        // Result: "$${name}" as literal text with no variables
        assertThat(result.fragments(), is(new String[]{"$${name}"}));
        assertThat(result.varNames(), is(new String[]{}));
    }
}
