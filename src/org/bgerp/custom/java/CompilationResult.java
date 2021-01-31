package org.bgerp.custom.java;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kirill Berezin
 * @author Shamil Vakhitov
 */
public class CompilationResult {
    private List<CompilationMessage> errors = new ArrayList<>();
    private List<CompilationMessage> warnings = new ArrayList<>();
    private List<String> log = new ArrayList<>();

    public void addWarning(CompilationMessage warning) {
        warnings.add(warning);
    }

    public void addError(CompilationMessage error) {
        errors.add(error);
    }

    public void addLog(String message) {
        log.add(message);
    }

    public String getLogString() {
        StringBuilder result = new StringBuilder(1000);

        addMessages(errors, "\nErrors", result);
        addMessages(warnings, "\nWarnings", result);
        addMessages(log, "\nLog", result);

        return result.toString();
    }

    private void addMessages(List<?> log, String prefix, StringBuilder result) {
        result.append(prefix);
        result.append(" (");
        result.append(log.size());
        result.append("):\n");

        for (Object message : log) {
            result.append(message);
            result.append("\n");
        }
    }
}
