package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.CommonLinkDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.link.LinkAddedEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.event.link.LinkRemovedEvent;
import ru.bgcrm.event.link.LinkRemovingEvent;
import ru.bgcrm.event.link.LinksToRemovedEvent;
import ru.bgcrm.event.link.LinksToRemovingEvent;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;

public class LinkAction extends BaseAction {
    
    private static final String PARAM_PREFIX = "c:";
    private static final int PARAM_PREFIX_LENGTH = PARAM_PREFIX.length();

    public ActionForward addLink(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = getLink(form);
        if (Utils.isBlankString(link.getObjectType()) || link.getObjectId() == 0 || Utils.isBlankString(link.getLinkedObjectType())
                || link.getLinkedObjectTitle() == null) { // link.getLinkedObjectId() <= 0 || Убрана проверка, так как в мастере < 0
            throw new BGIllegalArgumentException();
        }

        addLink(form, conSet.getConnection(), link);

        return status(conSet, form);
    }

    public static void addLink(DynActionForm form, Connection con, CommonObjectLink link) throws Exception {
        String className = null;
        if (Process.OBJECT_TYPE.equals(link.getObjectType())) {
            Process process = new ProcessDAO(con).getProcess(link.getObjectId());
            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
            className = type.getProperties().getActualScriptName();
        }

        EventProcessor.processEvent(new LinkAddingEvent(form, link), className, new SingleConnectionConnectionSet(con));

        CommonLinkDAO.getLinkDAO(link.getObjectType(), con).addLink(link);
        
        if (Process.OBJECT_TYPE.equals(link.getObjectType())) {
            if (new ProcessLinkDAO(con).checkCycles(link.getObjectId()))
                throw new BGMessageException(form.l.l("Циклическая зависимость"));
        }

        EventProcessor.processEvent(new LinkAddedEvent(form, link), className, new SingleConnectionConnectionSet(con));
    }

    public ActionForward deleteLink(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = getLink(form);
        if (Utils.isBlankString(link.getObjectType()) || link.getObjectId() == 0 || Utils.isBlankString(link.getLinkedObjectType())
                || link.getLinkedObjectId() <= 0) {
            throw new BGIllegalArgumentException();
        }

        Connection con = conSet.getConnection();

        EventProcessor.processEvent(new LinkRemovingEvent(form, link), new SingleConnectionConnectionSet(con));

        CommonLinkDAO.getLinkDAO(link.getObjectType(), con).deleteLink(link);

        EventProcessor.processEvent(new LinkRemovedEvent(form, link), new SingleConnectionConnectionSet(con));

        return status(conSet, form);
    }

    public ActionForward deleteLinksWithType(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = getLink(form);
        if (link.getObjectId() == 0 || Utils.isBlankString(link.getObjectType()) || Utils.isBlankString(link.getLinkedObjectType())) {
            throw new BGIllegalArgumentException();
        }

        //TODO: Может событие сделать.
        CommonLinkDAO.getLinkDAO(link.getObjectType(), conSet.getConnection()).deleteLinksWithType(link);

        return status(conSet, form);
    }

    public ActionForward deleteLinksTo(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = getLink(form);
        if (Utils.isBlankString(link.getObjectType()) || Utils.isBlankString(link.getLinkedObjectType()) || link.getLinkedObjectId() <= 0) {
            throw new BGIllegalArgumentException();
        }

        EventProcessor.processEvent(new LinksToRemovingEvent(form, link), conSet);

        CommonLinkDAO.getLinkDAO(link.getObjectType(), conSet.getConnection()).deleteLinksTo(link);

        EventProcessor.processEvent(new LinksToRemovedEvent(form, link), conSet);

        return status(conSet, form);
    }

    public ActionForward linkList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = getLink(form);
        if (Utils.isBlankString(link.getObjectType()) || link.getObjectId() <= 0) {
            throw new BGIllegalArgumentException();
        }

        Connection con = conSet.getConnection();

        List<CommonObjectLink> list = CommonLinkDAO.getLinkDAO(link.getObjectType(), con).getObjectLinksWithType(link.getObjectId(),
                CommonDAO.getLikePatternStart(link.getLinkedObjectType()));
        form.getResponse().setData("list", list);

        if (Process.OBJECT_TYPE.equals(link.getObjectType())) {
            Process process = new ProcessDAO(con).getProcess(form.getId());
            form.getHttpRequest().setAttribute("process", process);
        }

        return data(conSet, mapping, form);
    }

    private CommonObjectLink getLink(DynActionForm form) {
        CommonObjectLink link = new CommonObjectLink();

        link.setObjectId(form.getId());
        link.setObjectType(form.getParam("objectType"));
        link.setLinkedObjectType(form.getParam("linkedObjectType"));
        link.setLinkedObjectId(Utils.parseInt(form.getParam("linkedObjectId")));
        link.setLinkedObjectTitle(form.getParam("linkedObjectTitle"));
        for (Map.Entry<String, Object> me : form.getParam().entrySet()) {
            String key = me.getKey();
            if (key.startsWith(PARAM_PREFIX)) {
                link.getConfigMap().put(key.substring(PARAM_PREFIX_LENGTH), ((String[]) me.getValue())[0]);
            }
        }

        return link;
    }
    
}
