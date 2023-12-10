package org.bgerp.model.process;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Utils;

/**
 * Generator a short process description.
 *
 * @author Shamil Vakhitov
 */
public class Reference {
    private final Process process;

    public Reference(Process process) {
        this.process = process;
    }

    public String description() {
        String result = process.getReference();
        if (Utils.isBlankString(result))
            result = Utils.escapeXml(process.getDescription());
        return result;
    }

    public String title() {
        String result = process.getReference();
        if (Utils.isBlankString(result))
            result = "#" + process.getId() + " " + Utils.escapeXml(process.getDescription());
        return result;
    }
}
