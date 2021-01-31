package ru.bgerp.plugin.blow.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgerp.plugin.blow.Plugin;
import ru.bgerp.plugin.blow.dao.BoardDAO;
import ru.bgerp.plugin.blow.model.Board;
import ru.bgerp.plugin.blow.model.BoardConfig;
import ru.bgerp.plugin.blow.model.BoardsConfig;

public class BoardAction extends BaseAction {
    private static final String JSP_PATH = Plugin.PATH_JSP_USER + "/board";
    
    public ActionForward board(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        BoardsConfig boardsConf = setup.getConfig(BoardsConfig.class);
        form.setResponseData("boardsConf", boardsConf);
        return data(conSet, form, JSP_PATH + "/board.jsp");
    }
        
    public ActionForward show(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        BoardConfig boardConf = setup.getConfig(BoardsConfig.class).getBoard(form.getId());
        if (boardConf != null) {
            // первичные процессы
            List<Pair<Process, Map<String, Object>>> processes = new BoardDAO(con, form.getUser()).getProcessList(boardConf);
            
            Set<Integer> processIds = processes.stream().map(Pair::getFirst).map(p -> p.getId()).collect(Collectors.toSet());
            
            // связи между процессами, пока используем только родительское отношение
            Collection<CommonObjectLink> links = new ProcessLinkDAO(con, form.getUser()).getLinksOver(processIds);
            
            Board board = new Board(boardConf, processes, links);
           
            form.setResponseData("board", board);
            form.setResponseData("processIds", processIds);
            
            updatePersonalization(form, con, persMap -> persMap.put("blowBoardLastSelected", String.valueOf(form.getId())));
        }
     
        return data(con, form, JSP_PATH + "/show.jsp");
    }
    
    public ActionForward move(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int processId = form.getParamInt("processId");
        int parentProcessId = form.getParamInt("parentProcessId");
        int fromParentProcessId = form.getParamInt("fromParentProcessId");
        
        ProcessLinkDAO linkDao = new ProcessLinkDAO(con, form.getUser());
        // remove link
        if (fromParentProcessId > 0)
            linkDao.deleteLink(new CommonObjectLink(fromParentProcessId, Process.LINK_TYPE_MADE, processId, ""));
        
        // add link
        if (parentProcessId > 0) {
            linkDao.addLink(new CommonObjectLink(parentProcessId, Process.LINK_TYPE_MADE, processId, ""));
        
            if (linkDao.checkCycles(parentProcessId))
                throw new BGMessageException(l.l("Циклическая зависимость"));
        }
        
        return status(con, form);
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

    public ActionForward search(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        var dao = new MessageDAO(conSet.getSlaveConnection(), form.getUser());

        var filter = form.getParam("filter");

        var result = new LinkedHashMap<Integer, SearchItem>();
        for (Message m : dao.getProcessMessageList(form.getSelectedValues("processId"), filter)) {
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

        return data(conSet, form, JSP_PATH + "/search.jsp");
    }

}
