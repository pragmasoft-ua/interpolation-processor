package interpolation.parser;

/**
 * Exception thrown when a template string cannot be parsed.
 * Includes position information for error reporting.
 */
public class TemplateParseException extends RuntimeException {

    private final int line;
    private final int column;

    /**
     * Creates a new TemplateParseException.
     *
     * @param message the error message
     * @param line the line number (1-based)
     * @param column the column number (0-based)
     */
    public TemplateParseException(String message, int line, int column) {
        super(formatMessage(message, line, column));
        this.line = line;
        this.column = column;
    }

    /**
     * Creates a new TemplateParseException with a cause.
     *
     * @param message the error message
     * @param line the line number (1-based)
     * @param column the column number (0-based)
     * @param cause the underlying cause
     */
    public TemplateParseException(String message, int line, int column, Throwable cause) {
        super(formatMessage(message, line, column), cause);
        this.line = line;
        this.column = column;
    }

    private static String formatMessage(String message, int line, int column) {
        return String.format("at position %d:%d: %s", line, column, message);
    }

    /**
     * Returns the line number where the error occurred (1-based).
     *
     * @return the line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the column number where the error occurred (0-based).
     *
     * @return the column number
     */
    public int getColumn() {
        return column;
    }
}
