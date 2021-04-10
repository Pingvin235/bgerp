package ru.bgcrm.dao.message;

import java.sql.Connection;
import java.util.List;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitled;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;

public abstract class MessageTypeContactSaver {
    private static final List<IdTitled> STANDARD_MODE_LIST = List.of(
        new IdTitled(0, "Не сохранять"),
        new IdTitled(1, "Cохранить")
    );

    public MessageTypeContactSaver(ParameterMap config) throws BGException {}

    /**
     * Different save modes, e.g E-Mail or domain.
     * @return
     */
    public List<IdTitled> getSaveModeList() {
        return STANDARD_MODE_LIST;
    }

    public abstract void saveContact(DynActionForm form, Connection con, Message message, Process process, int saveMode) throws BGException;
}
