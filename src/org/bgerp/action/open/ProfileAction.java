package org.bgerp.action.open;

import java.util.List;
import java.util.Set;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.bgerp.action.BaseAction;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.servlet.Interface;
import org.bgerp.cache.ParameterCache;
import org.bgerp.cache.UserCache;
import org.bgerp.dao.param.OldParamSearchDAO;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/open/profile")
public class ProfileAction extends BaseAction {
    private static final String PATH_JSP =  PATH_JSP_OPEN + "/profile";

    public static class Config extends org.bgerp.app.cfg.Config implements EventListener<ParamChangedEvent> {
        private static final Log log = Log.getLog();

        private final ConfigMap config;

        /** Parameter type 'list', enabling opening of user with value=1.  */
        private final Parameter openParam;
        private final List<Integer> showParamIds;
        private final Set<Integer> shownUserIds;

        protected Config(ConfigMap config, boolean validate) throws Exception {
            super(null);

            this.config = config;

            int paramId = config.getInt("user.open.enable.paramId");
            initWhen(paramId > 0);

            openParam = ParameterCache.getParameter(paramId);
            showParamIds = Utils.toIntegerList(config.get("user.open.show.paramIds"));

            if (openParam == null)
                throwValidationException("Param not found: {}", paramId);
            if (!Parameter.TYPE_LIST.equals(openParam.getType()))
                throwValidationException("Param type of param {} must be 'list'", paramId);
            if (showParamIds.isEmpty())
                throwValidationException("Param ID list is not defined.");

            try (var con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
                shownUserIds = new OldParamSearchDAO(con).searchObjectByParameterList(openParam.getId(), 1);
            }

            EventProcessor.subscribe(this, ParamChangedEvent.class);
        }

        public List<Integer> getShowParamIds() {
            return showParamIds;
        }

        /**
         * Is a user profile open.
         * @param userId
         * @return
         * @throws Exception
         */
        public boolean isOpen(int userId) throws Exception {
            return shownUserIds.contains(userId);
        }

        /**
         * User profile accessing {@link Interface#OPEN} URL.
         * @param userId
         * @return
         */
        @Dynamic
        public String url(int userId) {
            return Interface.getUrlOpen() + "/profile/" + userId;
        }

        @Override
        public void notify(ParamChangedEvent e, ConnectionSet conSet) throws Exception {
            if (e.getParameter().getId() != openParam.getId()) return;

            log.debug("Reset config");

            EventProcessor.unsubscribe(this);
            config.removeConfig(this.getClass());
        }
    }

    public ActionForward show(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        var config = setup.getConfig(Config.class);
        if (config != null && config.isOpen(form.getId())) {
            form.getHttpRequest().setAttribute("config", config);
            form.setResponseData("user", UserCache.getUser(form.getId()));
        }
        return html(conSet, form, PATH_JSP + "/show.jsp");
    }
}