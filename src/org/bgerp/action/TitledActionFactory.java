package org.bgerp.action;

import java.util.Collections;
import java.util.List;

import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

/**
 * Factory of list of titled actions.
 *
 * @author Shamil Vakhitov
 */
public interface TitledActionFactory {
    @Dynamic
    public List<TitledAction> create();

    public static List<TitledAction> create(String actionFactory) {
        try {
            var factory = (TitledActionFactory) Class.forName(actionFactory).getConstructor().newInstance();
            return factory.create();
        } catch (Exception e) {
            Log.getLog().error(e);
        }
        return Collections.emptyList();
    }
}
