package org.bgerp.app.exception;

import org.bgerp.app.l10n.Localizer;

/**
 * Message exception without localization of the message.
 *
 * @author Shamil Vakhitov
 */
public class BGMessageExceptionTransparent extends BGMessageException {
    public BGMessageExceptionTransparent(String pattern, Object... args) {
        super(Localizer.TRANSPARENT, pattern, args);
    }
}
