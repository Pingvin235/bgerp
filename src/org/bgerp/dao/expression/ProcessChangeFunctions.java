package org.bgerp.dao.expression;

import java.sql.Connection;

import org.bgerp.util.Log;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

@Deprecated
public class ProcessChangeFunctions extends ProcessChangeExpressionObject {
    private static final Log log = Log.getLog();

    public ProcessChangeFunctions(Process process, DynActionForm form, Connection con) {
        super(process, form, con);
        log.warndClass(ProcessChangeFunctions.class, ProcessChangeExpressionObject.class);
    }
}
