package ru.bgcrm.plugin.bgbilling.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Bean(oldClasses = "ru.bgcrm.plugin.bgbilling.dao.MessageTypeContactSaverPhone")
public class BGBillingMessageTypeContactSaverPhone extends ru.bgcrm.dao.message.MessageTypeContactSaver {
    private final int paramId;
    private final String format;

    public BGBillingMessageTypeContactSaverPhone(ConfigMap config) throws Exception {
        super(config);
        this.paramId = config.getInt("paramId", -1);
        this.format = config.get("format", "13");
        if (paramId <= 0) {
            throw new BGException("paramId incorrect");
        }
    }

    @Override
    public void saveContact(DynActionForm form, Connection con, Message message, Process process, int saveMode)
            throws BGException {
        CommonObjectLink contractLink = Utils
                .getFirst(new ProcessLinkDAO(con).getObjectLinksWithType(process.getId(), Contract.OBJECT_TYPE + "%"));
        if (contractLink == null) {
            return;
        }

        String phone = message.getFrom();

        String billingId = StringUtils.substringAfterLast(contractLink.getLinkedObjectType(), ":");

        ContractParamDAO paramDao = new ContractParamDAO(form.getUser(), billingId);
        List<ParameterPhoneValueItem> values = new ArrayList<ParameterPhoneValueItem>();
        for (ParameterPhoneValueItem item : paramDao.getPhoneParam(contractLink.getLinkedObjectId(), paramId)) {
            values.add(item);
        }

        boolean exists = false;
        for (ParameterPhoneValueItem value : values) {
            if (exists = phone.equals(value.getPhone())) {
                break;
            }
        }

        if (!exists) {
            ParameterPhoneValueItem item = new ParameterPhoneValueItem();
            item.setPhone(phone);
            item.setFormat(format);

            values.add(item);

            ParameterPhoneValue phoneValue = new ParameterPhoneValue();
            phoneValue.setItemList(values);

            paramDao.updatePhoneParameter(contractLink.getLinkedObjectId(), paramId, phoneValue);
        }
    }
}
