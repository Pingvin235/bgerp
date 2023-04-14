package org.bgerp.l10n;

import java.util.List;
import java.util.Map;

import org.bgerp.util.Log;

import com.google.common.annotations.VisibleForTesting;

/**
 * Translator to a wanted language using many {@link Localization} sequentially.
 *
 * @author Shamil Vakhitov
 */
public class Localizer {
    private static final Log log = Log.getLog();

    public static final Localizer TRANSPARENT = new TransparentLocalizer();

    private final Localization[] localizations;
    private final String lang;

    @VisibleForTesting
    public Localizer(String lang, Localization... localizations) {
        this.lang = lang;
        this.localizations = localizations;
    }

    /**
     * @return target language.
     */
    public String getLang() {
        return lang;
    }

    /**
     * Translates to target language.
     * @param pattern string message with '{}' placeholders.
     * @param args arguments for replacing placeholders.
     * @return
     */
    public String l(String pattern, Object... args) {
        for (Localization localization : localizations) {
            if (localization == null)
                break;

            String translation = localization.getTranslation(lang, pattern);
            if (translation != null)
                return Log.format(translation, args);
        }

        log.warn("Missing translation for pattern: '{}', localizations: {}", pattern, List.of(localizations));

        return Log.format(pattern, args);
    }

    /**
     * @return a map entry with {@code key} and a found translation for it.
     */
    public Map.Entry<String, String> entry(String key) {
        return Map.entry(key, l(key));
    }
}
