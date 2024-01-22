package ru.bgcrm.dao.message;

import java.sql.Connection;
import java.util.List;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.base.IdTitled;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Bean
public class MessageTypeContactSaverEmail extends MessageTypeContactSaver {
    private static final List<IdTitled> MODE_LIST = List.of(
        new IdTitled(0, "Не сохранять"),
        new IdTitled(1, "Cохранить EMail"),
        new IdTitled(2, "Cохранить EMail домен")
    );

    private int paramId;

    public MessageTypeContactSaverEmail(ConfigMap config) throws Exception {
        super(config);

        this.paramId = config.getInt("paramId", -1);
        if (paramId <= 0) {
            throw new BGMessageException("paramId not defined!");
        }
    }

    @Override
    public List<IdTitled> getSaveModeList() {
        return MODE_LIST;
    }

    @Override
    public void saveContact(DynActionForm form, Connection con, Message message, Process process, int saveMode) throws Exception {
        CommonObjectLink customerLink = Utils.getFirst(new ProcessLinkDAO(con).getObjectLinksWithType(process.getId(), Customer.OBJECT_TYPE));
        if (customerLink == null) {
            return;
        }

        String email = message.getFrom();

        String domainName = StringUtils.substringAfter(email, "@");

        ParamValueDAO paramDao = new ParamValueDAO(con);

        SortedMap<Integer, ParameterEmailValue> currentValue = paramDao.getParamEmail(customerLink.getLinkObjectId(), paramId);

        boolean exists = false;
        for (ParameterEmailValue value : currentValue.values()) {
            if (exists = value.getValue().equals(email) || value.getValue().equals(domainName)) {
                break;
            }
        }

        if (!exists) {
            paramDao.updateParamEmail(customerLink.getLinkObjectId(), paramId, 0, new ParameterEmailValue(saveMode == 1 ? email : domainName));
        }
    }
}
