package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.List;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.model.Pageable;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.plugin.bgbilling.proto.dao.InetDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetSessionLog;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Bean
public class ShowInetSessionStep extends BaseStep {
    private final int inetModuleId;

    public ShowInetSessionStep(ConfigMap config) {
        super(config);
        inetModuleId = config.getInt("inetModuleId");
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_show_inet_session.jsp";
    }

    @Override
    public StepData<?> data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<ShowInetSessionStep> {
        private List<InetSessionLog> sessions;

        private Data(ShowInetSessionStep step, WizardData data) {
            super(step, data);
        }

        public List<InetSessionLog> getSessions() {
            return sessions;
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) throws Exception {
            CommonObjectLink contractLink = Utils.getFirst(new ProcessLinkDAO(con).getObjectLinksWithType(data.getProcess().getId(), Contract.OBJECT_TYPE + "%"));
            if (contractLink == null)
                return false;

            var contract = new Contract(contractLink);

            var dao = new InetDAO(form.getUser(), contract.getBillingId(), step.inetModuleId);

            var result = new Pageable<InetSessionLog>(20);
            dao.getSessionAliveContractList(result, contract.getId());
            sessions = result.getList();

            return true;
        }
    }
}
