package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;

public class PluginDAO extends BillingDAO {
    public PluginDAO(User user, DBInfo dbInfo) {
        super(user, dbInfo);
    }

    public PluginDAO(User user, String billingId) {
        super(user, billingId);
    }

    public List<String> getInstalledPlugins() {
        List<String> result = new ArrayList<>();
        Request req = new Request();
        if (dbInfo.versionCompare("8.0") > 0) {
            req.setModule("admin");
            req.setAction("MenuAndToolBar");
        } else {

            req.setModule("installer");
            req.setAction("GetInstalledPlugins");
        }
        Document doc = transferData.postData(req, user);
        for (Element el : XMLUtils.selectElements(doc, "/data/plugin_list/plugin")) {
            result.add(el.getAttribute("id"));
        }

        return result;
    }
}
