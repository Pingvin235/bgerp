package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGMessageExceptionTransparent;
import org.bgerp.model.base.IdTitle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.Pair;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.UnsupportedBillingVersion;
import ru.bgcrm.util.Utils;

public class CashCheckDAO extends BillingDAO {
    public CashCheckDAO(User user, String billingId) {
        super(user, billingId);
    }

    public static final String CASHCHECK_MODULE_ID = "ru.bitel.bgbilling.plugins.cashcheck";

    public Pair<String, String> printCheck(int registratorId, int paymentId, String summa, String pswd) throws BGMessageException {
        if (dbInfo.versionCompare("6.1") >= 0) {
            if (Utils.notBlankString(pswd) && registratorId > 0) {
                RequestJsonRpc req = new RequestJsonRpc(CASHCHECK_MODULE_ID, "CashcheckService", "bindPrinter");
                req.setParam("registratorId", registratorId);
                req.setParam("password", pswd);

                transferData.postData(req, user);
            }

            RequestJsonRpc req = new RequestJsonRpc(CASHCHECK_MODULE_ID, "CashcheckService", "printCheck");
            req.setParam("paymentIds", Collections.singleton(paymentId));
            req.setParam("summa", summa);

            JsonNode result = transferData.postDataReturn(req, user);

            return new Pair<>(result.path("summa").textValue(), result.path("submit").textValue());
        } else if (dbInfo.versionCompare("5.1") <= 0) {
            if (paymentId > 0) {
                try {
                    Request request;
                    if (Utils.notBlankString(pswd) && registratorId > 0) {
                        request = new Request();
                        request.setModule(CASHCHECK_MODULE_ID);
                        request.setAction("BindPrinter");
                        request.setAttribute("registrator", registratorId);
                        request.setAttribute("password", pswd);
                        transferData.postData(request, user);
                    }

                    request = new Request();
                    request.setModule(CASHCHECK_MODULE_ID);
                    request.setAction("PrintCheck");
                    request.setAttribute("id", paymentId);
                    request.setAttribute("summa", summa);
                    Document doc = transferData.postData(request, user);

                    NodeList nodeList = doc.getElementsByTagName("data");
                    if (nodeList.getLength() > 0) {
                        Element element = (Element) nodeList.item(0);

                        return new Pair<>(element.getAttribute("summa"), element.getAttribute("submit"));
                    }

                } catch (Exception e) {
                    throw new BGMessageExceptionTransparent("Чек не напечатан. " + e.getMessage());
                }
            } else {
                throw new BGMessageExceptionTransparent("Чек не напечатан.Ошибка.");
            }

            return new Pair<>("", "");
        } else {
            throw new UnsupportedBillingVersion("5.1 и с 6.1");
        }
    }

    /**
     * Возвращает список регистраторов с выбранным первым.
     * @return
     */
    public List<IdTitle> getRegistratorList() {
        List<IdTitle> registratorList = new ArrayList<>();

        int selectedRegistratorId = 0;

        if (dbInfo.versionCompare("6.1") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(CASHCHECK_MODULE_ID, "CashcheckService", "registratorList");

            JsonNode result = transferData.postDataReturn(req, user);

            selectedRegistratorId = result.path("registratorId").intValue();
            List<IdTitle> list = readJsonValue(result.path("list").traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
            for (IdTitle registrator : list) {
                if (registrator.getId() == selectedRegistratorId) {
                    registratorList.add(0, registrator);
                } else {
                    registratorList.add(registrator);
                }
            }
        } else if (dbInfo.versionCompare("5.1") <= 0) {
            Request request = new Request();
            request.setModule(CASHCHECK_MODULE_ID);
            request.setAction("RegistratorList");

            Document doc = transferData.postData(request, user);

            //registratorId="0"
            selectedRegistratorId = Utils.parseInt(((Element) doc.getElementsByTagName("data").item(0)).getAttribute("registratorId"));

            NodeList nodeList = doc.getElementsByTagName("item");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    int id = Utils.parseInt(element.getAttribute("id"));
                    if (id == selectedRegistratorId) {
                        registratorList.add(0, new IdTitle(id, element.getAttribute("title") + " подключен"));
                    } else {
                        registratorList.add(new IdTitle(id, element.getAttribute("title")));
                    }
                }
            }

        } else {
            throw new UnsupportedBillingVersion("5.1 и с 6.1");
        }

        if (selectedRegistratorId == 0) {
            registratorList.add(0, new IdTitle(0, "~ не выбран ~"));
        }

        return registratorList;
    }

    public IdTitle getCurrentPrinter() {
        if (dbInfo.versionCompare("6.1") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(CASHCHECK_MODULE_ID, "CashcheckService", "getCurrentPrinter");

            JsonNode result = transferData.postDataReturn(req, user);

            return new IdTitle(result.path("registratorId").intValue(), result.path("registratorName").textValue());
        } else {
            return null;
        }
    }
}