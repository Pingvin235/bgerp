package ru.bgcrm.dao.message;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Bean
public class MessageTypeContactSaverPhone extends ru.bgcrm.dao.message.MessageTypeContactSaver {
    private final int paramId;
    private final String format;

    public MessageTypeContactSaverPhone(ConfigMap config) throws Exception {
        super(config);
        this.paramId = config.getInt("paramId", -1);
        this.format = config.get("format", "13");
        if (paramId <= 0) {
            throw new BGException("paramId incorrect");
        }
    }

    @Override
    public void saveContact(DynActionForm form, Connection con, Message message, Process process, int saveMode) throws Exception {
        CommonObjectLink customerLink = Utils.getFirst(new ProcessLinkDAO(con).getObjectLinksWithType(process.getId(), Customer.OBJECT_TYPE));
        if (customerLink == null) {
            return;
        }

        String phone = message.getFrom();

        ParamValueDAO paramDao = new ParamValueDAO(con);

        List<ParameterPhoneValueItem> values = new ArrayList<ParameterPhoneValueItem>();

        ParameterPhoneValue currentValue = paramDao.getParamPhone(customerLink.getLinkedObjectId(), paramId);
        values = currentValue.getItemList();

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

            paramDao.updateParamPhone(customerLink.getLinkedObjectId(), paramId, currentValue);
        }
    }
}