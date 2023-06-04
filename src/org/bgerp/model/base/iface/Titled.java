package org.bgerp.model.base.iface;

import org.bgerp.app.l10n.Localizer;

/**
 * Title, provided with a localizer context.
 *
 * @author Shamil Vakhitov
 */
public interface Titled {
    public String getTitle(Localizer l);
}
