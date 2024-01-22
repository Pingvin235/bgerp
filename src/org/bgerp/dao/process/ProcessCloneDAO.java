package org.bgerp.dao.process;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.bgerp.dao.param.ParamValueDAO;

import javassist.NotFoundException;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.StatusChangeDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Fluent DAO for cloning processes.
 */
public class ProcessCloneDAO extends CommonDAO {
    private final DynActionForm form;

    private boolean params;

    public ProcessCloneDAO(Connection con, DynActionForm form) {
        super(con);
        this.form = form;
    }

    public ProcessCloneDAO withParams(boolean value) {
        this.params = value;
        return this;
    }

    public Process clone(int id) throws SQLException, NotFoundException {
        var dao = new ProcessDAO(con, form);

        var process = dao.getProcessOrThrow(id);

        var type = ProcessTypeCache.getProcessTypeOrThrow(process.getTypeId());

        var clone = new Process();
        clone.setCreateTime(new Date());
        clone.setCreateUserId(form.getUserId());

        clone.setTypeId(process.getTypeId());
        clone.setStatusId(process.getStatusId());
        clone.setPriority(process.getPriority());
        clone.setDescription(process.getDescription());

        dao.updateProcess(clone);

        dao.updateProcessGroups(process.getGroups(), clone.getId());
        dao.updateProcessExecutors(process.getExecutors(), clone.getId());

        StatusChange change = new StatusChange();
        change.setDate(new Date());
        change.setProcessId(process.getId());
        change.setUserId(form.getUserId());
        change.setComment(form.l.l("Process cloned from {}", process.getId()));
        change.setStatusId(process.getStatusId());

        new StatusChangeDAO(con).changeStatus(process, null, change);

        if (params)
            new ParamValueDAO(con).copyParams(process.getId(), clone.getId(), type.getProperties().getParameterIds());

        return clone;
    }
}
