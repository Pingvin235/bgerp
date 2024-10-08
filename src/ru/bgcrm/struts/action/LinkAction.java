package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.dao.CommonLinkDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.link.LinkAddedEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.event.link.LinkRemovedEvent;
import ru.bgcrm.event.link.LinkRemovingEvent;
import ru.bgcrm.event.link.LinksToRemovedEvent;
import ru.bgcrm.event.link.LinksToRemovingEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;


@Action(path = "/user/link")
public class LinkAction extends BaseAction {
    private static final String PARAM_PREFIX = "c:";
    private static final int PARAM_PREFIX_LENGTH = PARAM_PREFIX.length();

    public ActionForward addLink(DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = getLink(form);
        if (Utils.isBlankString(link.getObjectType()) || link.getObjectId() == 0 || Utils.isBlankString(link.getLinkObjectType())
                || link.getLinkObjectTitle() == null) { // link.getLinkedObjectId() <= 0 || Убрана проверка, так как в мастере < 0
            throw new BGIllegalArgumentException();
        }

        addLink(form, conSet.getConnection(), link);

        return json(conSet, form);
    }

    public static void addLink(DynActionForm form, Connection con, CommonObjectLink link) throws Exception {
        EventProcessor.processEvent(new LinkAddingEvent(form, link), new SingleConnectionSet(con));

        CommonLinkDAO.getLinkDAO(link.getObjectType(), con).addLink(link);

        EventProcessor.processEvent(new LinkAddedEvent(form, link), new SingleConnectionSet(con));
    }

    public ActionForward deleteLink(DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = getLink(form);
        if (Utils.isBlankString(link.getObjectType()) || link.getObjectId() == 0 || Utils.isBlankString(link.getLinkObjectType())
                || link.getLinkObjectId() <= 0) {
            throw new BGIllegalArgumentException();
        }

        Connection con = conSet.getConnection();

        EventProcessor.processEvent(new LinkRemovingEvent(form, link), new SingleConnectionSet(con));

        CommonLinkDAO.getLinkDAO(link.getObjectType(), con).deleteLink(link);

        EventProcessor.processEvent(new LinkRemovedEvent(form, link), new SingleConnectionSet(con));

        return json(conSet, form);
    }

    public ActionForward deleteLinksWithType(DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = getLink(form);
        if (link.getObjectId() == 0 || Utils.isBlankString(link.getObjectType()) || Utils.isBlankString(link.getLinkObjectType())) {
            throw new BGIllegalArgumentException();
        }

        //TODO: Может событие сделать.
        CommonLinkDAO.getLinkDAO(link.getObjectType(), conSet.getConnection()).deleteLinksWithType(link);

        return json(conSet, form);
    }

    public ActionForward deleteLinksTo(DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = getLink(form);
        if (Utils.isBlankString(link.getObjectType()) || Utils.isBlankString(link.getLinkObjectType()) || link.getLinkObjectId() <= 0) {
            throw new BGIllegalArgumentException();
        }

        EventProcessor.processEvent(new LinksToRemovingEvent(form, link), conSet);

        CommonLinkDAO.getLinkDAO(link.getObjectType(), conSet.getConnection()).deleteLinksTo(link);

        EventProcessor.processEvent(new LinksToRemovedEvent(form, link), conSet);

        return json(conSet, form);
    }

    public ActionForward linkList(DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = getLink(form);
        if (Utils.isBlankString(link.getObjectType()) || link.getObjectId() <= 0) {
            throw new BGIllegalArgumentException();
        }

        Connection con = conSet.getConnection();

        List<CommonObjectLink> list = CommonLinkDAO.getLinkDAO(link.getObjectType(), con).getObjectLinksWithType(link.getObjectId(),
                LikePattern.START.get(link.getLinkObjectType()));
        form.setResponseData("list", list);

        if (Process.OBJECT_TYPE.equals(link.getObjectType())) {
            Process process = new ProcessDAO(con).getProcess(form.getId());
            form.setRequestAttribute("process", process);
        }

        return html(conSet, form, PATH_JSP_USER + "/process/process/link/list.jsp");
    }

    private CommonObjectLink getLink(DynActionForm form) {
        CommonObjectLink link = new CommonObjectLink();

        link.setObjectId(form.getId());
        link.setObjectType(form.getParam("objectType"));
        link.setLinkObjectType(form.getParam("linkedObjectType"));
        link.setLinkObjectId(Utils.parseInt(form.getParam("linkedObjectId")));
        link.setLinkObjectTitle(form.getParam("linkedObjectTitle"));
        // store parameters with c: prefix in name to link configuration
        for (Map.Entry<String, Object> me : form.getParam().entrySet()) {
            String key = me.getKey();
            if (key.startsWith(PARAM_PREFIX)) {
                link.getConfigMap().put(key.substring(PARAM_PREFIX_LENGTH), ((String[]) me.getValue())[0]);
            }
        }

        return link;
    }

}
