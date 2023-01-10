package org.bgerp.l10n;

/**
 * Localizer, returning a parameter string without modification.
 *
 * @author Shamil Vakhitov
 */
public class TransparentLocalizer extends Localizer {
    TransparentLocalizer() {
        super(null);
    }

    @Override
    public String l(String pattern, Object... args) {
        return pattern;
    }
}
