package ru.bgcrm.model.process.queue;

import java.sql.SQLException;

import org.bgerp.model.process.queue.Column;
import org.bgerp.util.Dynamic;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Displayable process queue column.
 *
 * @author Shamil Vakhitov
 */
public class MediaColumn {
    private final Column column;
    private final int cellIndex;

    public MediaColumn(Column column, int cellIndex) {
        this.column = column;
        this.cellIndex = cellIndex;
    }

    @Dynamic
    public Process getProcess(Process[] processArray) {
        String target = column.getProcess();
        if (target.equals(ProcessDAO.LINKED_PROCESS)) {
            return processArray[1];
        }
        return processArray[0];
    }

    @Dynamic
    public Column getColumn() {
        return column;
    }

    public Object getValue(DynActionForm form, boolean isHtmlMedia, Object[] row) throws SQLException {
        Process process = getProcess((Process[]) row[0]);
        Object obj = row[cellIndex];
        return column.getCellValue(form, isHtmlMedia, process, obj);
    }
}