package interpolation.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for parsing template strings using the ANTLR-generated parser.
 * Provides a simple {@link #parse(String)} method that returns a {@link ParsedTemplate}.
 */
public final class TemplateParserWrapper {

    private TemplateParserWrapper() {
        // Utility class
    }

    /**
     * Parses a template string and extracts fragments and variable names.
     *
     * <p>Template syntax:
     * <ul>
     *   <li>{@code ${varName}} - variable interpolation</li>
     *   <li>{@code $${} - escaped literal {@code ${}</li>
     *   <li>Any other text - literal text</li>
     * </ul>
     *
     * @param template the template string to parse
     * @return a ParsedTemplate containing fragments and variable names
     * @throws TemplateParseException if the template is malformed
     */
    public static ParsedTemplate parse(String template) {
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }

        if (template.isEmpty()) {
            return new ParsedTemplate(new String[]{""}, new String[0]);
        }

        TemplateLexer lexer = new TemplateLexer(CharStreams.fromString(template));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ThrowingErrorListener());

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TemplateParser parser = new TemplateParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ThrowingErrorListener());

        TemplateParser.TemplateContext tree = parser.template();

        TemplateVisitor visitor = new TemplateVisitor();
        visitor.visit(tree);

        return ParsedTemplate.of(visitor.fragments, visitor.varNames);
    }

    /**
     * Visitor that extracts fragments and variable names from the parse tree.
     */
    private static class TemplateVisitor extends TemplateParserBaseVisitor<Void> {
        final List<String> fragments = new ArrayList<>();
        final List<String> varNames = new ArrayList<>();
        private final StringBuilder currentFragment = new StringBuilder();

        @Override
        public Void visitTemplate(TemplateParser.TemplateContext ctx) {
            for (TemplateParser.PartContext part : ctx.part()) {
                visit(part);
            }
            fragments.add(currentFragment.toString());
            return null;
        }

        @Override
        public Void visitText(TemplateParser.TextContext ctx) {
            currentFragment.append(ctx.getText());
            return null;
        }

        @Override
        public Void visitEscape(TemplateParser.EscapeContext ctx) {
            // $${ becomes literal ${
            currentFragment.append("${");
            return null;
        }

        @Override
        public Void visitExpression(TemplateParser.ExpressionContext ctx) {
            fragments.add(currentFragment.toString());
            currentFragment.setLength(0);

            TemplateParser.ExprContext exprCtx = ctx.expr();
            if (exprCtx != null) {
                Token idToken = exprCtx.ID().getSymbol();
                String varName = idToken.getText();
                if (varName == null || varName.isEmpty()) {
                    throw new TemplateParseException(
                            "Empty variable name",
                            idToken.getLine(),
                            idToken.getCharPositionInLine()
                    );
                }
                varNames.add(varName);
            }
            return null;
        }
    }

    /**
     * Error listener that throws TemplateParseException on syntax errors.
     *
     * <p>Note: Error detection relies on token types rather than message text
     * to avoid coupling to ANTLR's error message format.
     */
    private static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            String betterMsg = improveSyntaxErrorMessage(offendingSymbol);
            throw new TemplateParseException(
                    betterMsg != null ? betterMsg : msg,
                    line,
                    charPositionInLine
            );
        }

        private String improveSyntaxErrorMessage(Object offendingSymbol) {
            if (!(offendingSymbol instanceof Token token)) {
                return null;
            }

            // Detect unclosed expression: EOF while in expression mode
            if (token.getType() == Token.EOF) {
                return "Unclosed ${...} expression";
            }

            // Detect empty variable: closing brace immediately after opening
            if (token.getType() == TemplateLexer.EXPR_END) {
                return "Empty variable name in ${}";
            }

            return null;
        }
    }
}
