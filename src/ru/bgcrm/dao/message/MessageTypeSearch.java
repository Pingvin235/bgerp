package ru.bgcrm.dao.message;

import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public abstract class MessageTypeSearch {
    private final String title;

    public MessageTypeSearch(ConfigMap config) throws BGException {
        this.title = config.get("title", "Безымянный тип");
    }

    public String getTitle() {
        return title;
    }

    public String getJsp() {
        return null;
    }

    public abstract void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result) throws BGException;
}
