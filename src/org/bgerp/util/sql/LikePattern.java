package org.bgerp.util.sql;

import ru.bgcrm.util.Utils;

/**
 * SQL LIKE patterns generators for a substring.
 *
 * @author Shamil Vakhitov
 */
public enum LikePattern {
    /**
     * Pattern {@code %substr%}
     */
    SUB {
        @Override
        public String get(String substr) {
            if (Utils.isBlankString(substr))
                return substr;

            StringBuilder result = new StringBuilder(substr.length() + 2);

            if (!substr.startsWith(ANY))
                result.append(ANY);

            result.append(substr);

            if (!substr.endsWith(ANY))
                result.append(ANY);

            return result.toString();
        }
    },
    /**
     * Pattern {@code %substr}
     */
    END {
        @Override
        public String get(String substr) {
            if (Utils.isBlankString(substr))
                return substr;

            StringBuilder result = new StringBuilder(substr.length() + 1);

            if (!substr.startsWith(ANY))
                result.append(ANY);

            result.append(substr);

            return result.toString();
        }
    },
    /**
     * Pattern {@code substr%}
     */
    START {
        @Override
        public String get(String substr) {
            if (Utils.isBlankString(substr))
                return substr;

            StringBuilder result = new StringBuilder(substr.length() + 1);

            result.append(substr);

            if (!substr.endsWith(ANY))
                result.append(ANY);

            return result.toString();
        }
    };

    private static final String ANY = "%";

    /**
     * Generates pattern for a given strategy.
     * @param substr substring.
     * @return {@code substr} for {@code null} or empty string or generated pattern.
     */
    public abstract String get(String substr);
}
