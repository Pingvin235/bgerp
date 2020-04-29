package ru.bgerp.plugin.blow.struts.action;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgerp.plugin.blow.dao.BoardDAO;
import ru.bgerp.plugin.blow.model.Board;
import ru.bgerp.plugin.blow.model.BoardConfig;
import ru.bgerp.plugin.blow.model.BoardsConfig;

public class BoardAction extends BaseAction {
    
    public ActionForward board(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        BoardsConfig boardsConf = setup.getConfig(BoardsConfig.class);
        form.setResponseData("boardsConf", boardsConf);
        return data(conSet, mapping, form, "board");
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
            
            updatePersonalization(form, con, persMap -> persMap.put("blowBoardLastSelected", String.valueOf(form.getId())));
        }
     
        return data(con, mapping, form, "show");
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
    
}
