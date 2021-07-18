package ru.bgerp.l10n;

import java.util.List;

import ru.bgerp.util.Log;

/**
 * Translator to a wanted language using many {@link Localization} sequentially.
 * 
 * @author Shamil Vakhitov
 */
public class Localizer {
    private static final Log log = Log.getLog();

    private final Localization[] localizations;
    private final String toLang;

    public Localizer(String toLang, Localization... localizations) {
        this.toLang = toLang;
        this.localizations = localizations;
    }

    public String l(String pattern, Object... args) {
        for (Localization localization : localizations) {
            if (localization == null) break;

            String translation = localization.getTranslation(pattern, toLang);
            if (translation != null)
                return Log.format(translation, args);
        }

        log.warn("Missing translation for pattern: '{}', localizations: {}", pattern, List.of(localizations));

        return Log.format(pattern, args);
    }
}
