package ru.bgerp.l10n;

import ru.bgerp.util.Log;

/**
 * Translator to a wanted language.
 * @author Shamil
 */
public class Localizer {
    private static final Log log = Log.getLog();

    private final Localization[] localizations;
    private final String toLang;
    
    public Localizer(Localization[] localizations, String toLang) {
        this.localizations = localizations;
        this.toLang = toLang;
    }

    public String l(String pattern, Object... args) {
        for (Localization localization : localizations) {
            if (localization == null) break;
            
            String translation = localization.getTranslation(pattern, toLang);
            if (translation != null)
                return String.format(translation, args);
        }

        log.debug("Missing translation for pattern: '%s'", pattern);

        return String.format(pattern, args);
    }    
}
