package ru.bgcrm.plugin.bgbilling.action.proto;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.VoiceDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.voice.PhoneResource;
import ru.bgcrm.plugin.bgbilling.proto.model.voice.VoiceAccount;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/voice", pathId = true)
public class VoiceAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/voice";

    public ActionForward contractInfo(DynActionForm form, ConnectionSet conSet) throws BGIllegalArgumentException {
        return html(conSet, form, PATH_JSP + "/contract_info.jsp");
    }

    public ActionForward accounts(DynActionForm form, ConnectionSet conSet) throws BGIllegalArgumentException {
        var dao = new VoiceDAO(form.getUser(), form.getParam("billingId", Utils::notBlankString), form.getParamInt("moduleId", Utils::isPositive));

        Map<Integer, String> deviceTypeTitles = dao.getDeviceTypes().stream().collect(Collectors.toMap(IdTitle::getId, IdTitle::getTitle));

        List<VoiceAccount> accounts = dao.getAccounts(form.getParamInt("contractId", Utils::isPositive));
        for (var account : accounts) {
            account.setDeviceTitle(deviceTypeTitles.getOrDefault(account.getDeviceId(), IdTitle.unknown(account.getDeviceId())));
        }

        form.setResponseData("list", accounts);

        return html(conSet, form, PATH_JSP + "/account/list.jsp");
    }

    public ActionForward accountEdit(DynActionForm form, ConnectionSet conSet) throws BGIllegalArgumentException {
        var dao = new VoiceDAO(form.getUser(), form.getParam("billingId", Utils::notBlankString), form.getParamInt("moduleId", Utils::isPositive));

        form.setResponseData("types", dao.getAccountTypes());

        VoiceAccount account;
        if (form.getId() > 0) {
            account = dao.getAccount(form.getId());
            // getService не возвращает многие поля сервисов типа deviceTitle
            var device = dao.getDevice(account.getDeviceId());
            if (device != null) {
                account.setDeviceTitle(device.getTitle() + " (" + device.getId() + ')');
            }
        } else  {
            account = new VoiceAccount();
            account.setDateFrom(new Date());
        }

        form.setResponseData("account", account);

        return html(conSet, form, PATH_JSP + "/account/editor.jsp");
    }

    public ActionForward devices(DynActionForm form, ConnectionSet conSet) throws BGIllegalArgumentException {
        var dao = new VoiceDAO(form.getUser(), form.getParam("billingId", Utils::notBlankString), form.getParamInt("moduleId", Utils::isPositive));

        form.setResponseData("rootDevice", dao.getDeviceRoot());

        return html(conSet, form, PATH_JSP + "/account/device_tree.jsp");
    }

    public ActionForward categories(DynActionForm form, ConnectionSet conSet) throws BGIllegalArgumentException {
        var dao = new VoiceDAO(form.getUser(), form.getParam("billingId", Utils::notBlankString), form.getParamInt("moduleId", Utils::isPositive));

        form.setResponseData("rootCategory", dao.getCategoryRoot());

        return html(conSet, form, PATH_JSP + "/account/phone/categories.jsp");
    }

    public ActionForward category(DynActionForm form, ConnectionSet conSet) throws BGIllegalArgumentException {
        var dao = new VoiceDAO(form.getUser(), form.getParam("billingId", Utils::notBlankString), form.getParamInt("moduleId", Utils::isPositive));

        List<PhoneResource> resources = dao.getResources(form.getId());
        if (!resources.isEmpty()) {
            form.setResponseData("resources", resources);

            int resourceId = form.getParamInt("resourceId", Utils.getFirst(resources).getId());
            form.setResponseData("free", dao.getFreeList(form.getParamInt("categoryId", Utils::isPositive), resourceId));
        }

        return html(conSet, form, PATH_JSP + "/account/phone/category.jsp");
    }

    public ActionForward accountUpdate(DynActionForm form, ConnectionSet conSet) throws BGIllegalArgumentException {
        var dao = new VoiceDAO(form.getUser(), form.getParam("billingId", Utils::notBlankString), form.getParamInt("moduleId", Utils::isPositive));

        VoiceAccount account;
        if (form.getId() > 0) {
            account = dao.getAccount(form.getId());
        } else {
            account = new VoiceAccount();
            account.setTypeId(form.getParamInt("typeId", Utils::isPositive));
            account.setContractId(form.getParamInt("contractId", Utils::isPositive));
        }

        account.setDateFrom(form.getParamDate("dateFrom"));
        account.setDateTo(form.getParamDate("dateTo"));
        account.setSessionCountLimit((byte) form.getParamInt("sessions"));
        account.setDeviceId(form.getParamInt("deviceId"));
        account.setLogin(form.getParam("login"));
        String pswd = form.getParam("pswd", "");
        if (!pswd.contains("*"))
            account.setPassword(pswd);
        account.setNumber(form.getParamLong("number"));

        dao.updateAccount(account);

        if (form.getParamBoolean("generatePassword"))
            dao.generateAccountPassword(account.getId());

        return json(conSet, form);
    }

    public ActionForward accountDelete(DynActionForm form, ConnectionSet conSet) throws BGIllegalArgumentException {
        var dao = new VoiceDAO(form.getUser(), form.getParam("billingId", Utils::notBlankString), form.getParamInt("moduleId", Utils::isPositive));

        dao.deleteAccount(form.getId());

        return json(conSet, form);
    }
}
