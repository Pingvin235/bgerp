package ru.bgerp.i18n;

/**
 * Translator to a wanted language.
 * @author Shamil
 */
public class Localizer {
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
        return String.format(pattern, args);
    }    
}
