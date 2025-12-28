package interpolation.parser;

import java.util.List;

/**
 * Immutable record holding parsed template data.
 * Contains the text fragments and variable names extracted from a template string.
 *
 * <p>For example, parsing the template {@code "Hello ${name}, you have ${count} items"}
 * produces:
 * <ul>
 *   <li>fragments: ["Hello ", ", you have ", " items"]</li>
 *   <li>varNames: ["name", "count"]</li>
 * </ul>
 *
 * <p>The fragments array always has one more element than the varNames array.
 * If the template starts with a variable, the first fragment is empty string.
 * If the template ends with a variable, the last fragment is empty string.
 *
 * @param fragments the text fragments between variables
 * @param varNames the variable names extracted from the template
 */
public record ParsedTemplate(String[] fragments, String[] varNames) {

    /**
     * Creates a ParsedTemplate from lists of fragments and variable names.
     *
     * @param fragments the text fragments
     * @param varNames the variable names
     * @return a new ParsedTemplate
     */
    public static ParsedTemplate of(List<String> fragments, List<String> varNames) {
        return new ParsedTemplate(
                fragments.toArray(String[]::new),
                varNames.toArray(String[]::new)
        );
    }
}
