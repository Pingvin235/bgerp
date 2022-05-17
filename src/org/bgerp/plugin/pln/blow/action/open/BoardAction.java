package org.bgerp.plugin.pln.blow.action.open;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import org.bgerp.plugin.pln.blow.Plugin;
import org.bgerp.plugin.pln.blow.dao.BoardDAO;
import org.bgerp.plugin.pln.blow.model.Board;
import org.bgerp.plugin.pln.blow.model.BoardConfig;
import org.bgerp.plugin.pln.blow.model.BoardsConfig;

@Action(path = "/open/plugin/blow/board")
public class BoardAction extends BaseAction {

    public ActionForward show(DynActionForm form, Connection con) throws Exception {
        BoardConfig boardConf = setup.getConfig(BoardsConfig.class).getBoard(form.getId());
        if (boardConf != null) {
            // all processes
            List<Pair<Process, Map<String, Object>>> processes = new BoardDAO(con).getProcessList(boardConf);

            Set<Integer> processIds = processes.stream().map(Pair::getFirst).map(p -> p.getId()).collect(Collectors.toSet());

            // links over processes
            Collection<CommonObjectLink> links = new ProcessLinkDAO(con).getLinksOver(processIds);

            form.setResponseData("boardConf", boardConf);
            form.setResponseData("board", new Board(boardConf, processes, links));
        }

        return html(con, null, Plugin.PATH_JSP_OPEN + "/show.jsp");
    }

}
