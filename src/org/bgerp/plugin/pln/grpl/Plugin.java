package org.bgerp.plugin.pln.grpl;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.plugin.pln.grpl.dao.GrplDAO;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "grpl";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    public String getTitle() {
        return "Group Plan";
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
       return Map.of(
            Endpoint.JS, List.of(Endpoint.getPathPluginJS(ID)),
            Endpoint.CSS, List.of(Endpoint.getPathPluginCSS(ID)),
            Endpoint.USER_PROCESS_MENU_ITEMS, List.of(PATH_JSP_USER + "/menu_items.jsp")
        );
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            var config = getConfig(Setup.getSetup());
            var board = config.getBoard(e.getParameter().getId());
            if (board == null)
                return;

            var process = new ProcessDAO(conSet.getSlaveConnection()).getProcessOrThrow(e.getObjectId());
            if (board.processTypeId(process.getTypeId())) {
                var column = board.getColumnOrThrow(conSet, process);
                var duration = board.getProcessDuration(conSet, process);

                new GrplDAO(conSet.getConnection()).updateSlot(board, process, column.getId(), duration);
            }
        }, ParamChangedEvent.class);
    }

    public Config getConfig(Setup setup) {
        return setup.getConfig(Config.class);
    }
}