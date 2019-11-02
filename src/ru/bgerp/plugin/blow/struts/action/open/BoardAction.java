package ru.bgerp.plugin.blow.struts.action.open;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgerp.plugin.blow.dao.BoardDAO;
import ru.bgerp.plugin.blow.model.Board;
import ru.bgerp.plugin.blow.model.BoardConfig;
import ru.bgerp.plugin.blow.model.BoardsConfig;

public class BoardAction extends BaseAction {
    
    public ActionForward show(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        BoardConfig boardConf = setup.getConfig(BoardsConfig.class).getBoard(form.getId());
        if (boardConf != null) {
            // первичные процессы
            List<Pair<Process, Map<String, Object>>> processes = new BoardDAO(con).getProcessList(boardConf);
            
            Set<Integer> processIds = processes.stream().map(Pair::getFirst).map(p -> p.getId()).collect(Collectors.toSet());
            
            // связи между процессами, пока используем только родительское отношение
            Collection<CommonObjectLink> links = new ProcessLinkDAO(con).getLinksOver(processIds);
            
            form.setResponseData("boardConf", boardConf);
            form.setResponseData("board", new Board(boardConf, processes, links));
        }
     
        return processUserTypedForward(con, mapping, form, "show");
    }

}
