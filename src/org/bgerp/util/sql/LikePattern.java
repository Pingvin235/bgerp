package org.bgerp.util.sql;

import ru.bgcrm.util.Utils;

/**
 * SQL LIKE patterns generators for a substring.
 *
 * @author Shamil Vakhitov
 */
public enum LikePattern {
    /**
     * Pattern {@code %value%}
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
     * Pattern {@code %value}
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
     * Pattern {@code value%}
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
    },
    /**
     * Pattern {@code value}
     */
    EQ {
        @Override
        public String get(String substr) {
            return substr;
        }
    };

    private static final String ANY = "%";

    public static LikePattern of(String type) {
        return switch (type) {
            case "sub" -> SUB;
            case "end" -> END;
            case "start" -> START;
            case "eq" -> EQ;
            default -> throw new IllegalArgumentException("Incorrect type: " + type);
        };
    }

    /**
     * Generates pattern for a given strategy.
     * @param substr substring.
     * @return {@code substr} for {@code null} or empty string or generated pattern.
     */
    public abstract String get(String substr);
}
