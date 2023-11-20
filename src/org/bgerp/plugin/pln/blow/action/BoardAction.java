package org.bgerp.plugin.pln.blow.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;
import org.bgerp.plugin.pln.blow.Plugin;
import org.bgerp.plugin.pln.blow.dao.BoardDAO;
import org.bgerp.plugin.pln.blow.model.Board;
import org.bgerp.plugin.pln.blow.model.BoardConfig;
import org.bgerp.plugin.pln.blow.model.BoardsConfig;

@Action(path = "/user/plugin/blow/board")
public class BoardAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/board";

    public ActionForward board(DynActionForm form, ConnectionSet conSet) throws Exception {
        BoardsConfig boardsConf = setup.getConfig(BoardsConfig.class);
        form.setResponseData("boards", boardsConf.getBoards(form.getUser()));
        return html(conSet, form, PATH_JSP + "/board.jsp");
    }

    public ActionForward show(DynActionForm form, Connection con) throws Exception {
        BoardConfig boardConf = setup.getConfig(BoardsConfig.class).getBoard(form.getId());
        if (boardConf != null) {
            // первичные процессы
            List<Pair<Process, Map<String, Object>>> processes = new BoardDAO(con, form).getProcessList(boardConf);

            Set<Integer> processIds = processes.stream().map(Pair::getFirst).map(p -> p.getId()).collect(Collectors.toSet());

            // связи между процессами, пока используем только родительское отношение
            Collection<CommonObjectLink> links = new ProcessLinkDAO(con, form).getLinksOver(processIds);

            Board board = new Board(boardConf, processes, links);

            form.setResponseData("board", board);
            form.setResponseData("processIds", processIds);

            updatePersonalization(form, con, map -> map.put("blowBoardLastSelected", String.valueOf(form.getId())));
        }

        return html(con, form, PATH_JSP + "/show.jsp");
    }

    public ActionForward move(DynActionForm form, Connection con) throws Exception {
        int processId = form.getParamInt("processId");
        int parentProcessId = form.getParamInt("parentProcessId");
        int fromParentProcessId = form.getParamInt("fromParentProcessId");

        ProcessLinkDAO linkDao = new ProcessLinkDAO(con, form);
        // remove link
        if (fromParentProcessId > 0)
            linkDao.deleteLink(new CommonObjectLink(fromParentProcessId, Process.LINK_TYPE_MADE, processId, ""));

        // add link
        if (parentProcessId > 0) {
            linkDao.addLink(new CommonObjectLink(parentProcessId, Process.LINK_TYPE_MADE, processId, ""));

            if (linkDao.checkCycles(parentProcessId))
                throw new BGMessageException(l.l("Циклическая зависимость"));
        }

        return json(con, form);
    }

    public static class SearchItem {
        private final int processId;
        private final String processDescription;
        private final List<String> hits = new ArrayList<>();

        private SearchItem(int processId, String processDescription) {
            this.processId = processId;
            this.processDescription = processDescription;
        }

        private void addHit(Message message, String filter) {
            hits.add(message.getText());
        }

        public int getProcessId() {
            return processId;
        }

        public String getProcessDescription() {
            return processDescription;
        }

        public List<String> getHits() {
            return hits;
        }
    }

    public ActionForward search(DynActionForm form, ConnectionSet conSet) throws Exception {
        var dao = new MessageDAO(conSet.getSlaveConnection(), form);

        var filter = form.getParam("filter");

        var result = new LinkedHashMap<Integer, SearchItem>();
        for (Message m : dao.getProcessMessageList(form.getParamValues("processId"), filter)) {
            int processId = m.getProcessId();
            result
                .computeIfAbsent(processId, key -> new SearchItem(processId, m.getProcess().getDescription()))
                .addHit(m, filter);
        }

        // backwards sorting by hits count
        form.setResponseData("list", result.values().stream()
            .sorted((i1, i2) -> {
                return i2.getHits().size() - i1.getHits().size();
            })
            .collect(Collectors.toList()));

        return html(conSet, form, PATH_JSP + "/search.jsp");
    }

}
