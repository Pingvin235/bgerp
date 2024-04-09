package org.bgerp.app.l10n;

import org.bgerp.util.Log;

/**
 * Localizer, returning a pattern string without translation.
 * Only with applied substitutions.
 *
 * @author Shamil Vakhitov
 */
public class TransparentLocalizer extends Localizer {
    TransparentLocalizer() {
        super(null);
    }

    @Override
    public String l(String pattern, Object... args) {
        return Log.format(pattern, args);
    }
}
