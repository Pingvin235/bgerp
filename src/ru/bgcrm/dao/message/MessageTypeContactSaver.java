package ru.bgcrm.dao.message;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;

public abstract class MessageTypeContactSaver {
    private static final List<IdTitle> STANDARD_MODE_LIST = new ArrayList<IdTitle>();
    static {
        STANDARD_MODE_LIST.add(new IdTitle(0, "Не сохранять"));
        STANDARD_MODE_LIST.add(new IdTitle(1, "Cохранить"));
    }

    public MessageTypeContactSaver(ParameterMap config) throws BGException {}

    // разные режимы сохранения, например домен либо EMail
    public List<IdTitle> getSaveModeList() {
        return STANDARD_MODE_LIST;
    }

    public abstract void saveContact(DynActionForm form, Connection con, Message message, Process process, int saveMode) throws BGException;
}
