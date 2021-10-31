package ru.bgcrm.plugin.bgbilling.proto.dao.version.v8x;

import com.fasterxml.jackson.databind.JsonNode;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.dao.NPayDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.npay.NPayService;

import java.util.Date;

public class NPayDAO8x extends NPayDAO {
    public NPayDAO8x(User user, String billingId, int moduleId) throws BGException {
        super(user, billingId, moduleId);
    }

    public NPayDAO8x(User user, DBInfo dbInfo, int moduleId) throws BGException {
        super(user, dbInfo, moduleId);
    }

    public NPayService getService(int id) throws BGException {
        //http://billing:8081/executer?id=3004&module=npay&action=ServiceObjectGet&mid=6&BGBillingSecret=B6bysmW9pPPyBJxGsMFq6zEz&
        //[ length = 211 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="6A6380BBFC43D59F44428773F111ADC8" status="ok"><object col="1" date1="01.05.2008" date2="" entityId="0" entityMid="0" oid="0" sid="31"/><comment/></data>

        RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.modules.npay", moduleId,
                "NPayService", "serviceObjectGet");
        req.setParam("id", id);
        JsonNode res = transferData.postDataReturn(req, user);

        NPayService service = jsonMapper.convertValue(res, NPayService.class);
        return service;

        /*NPayService result = null;

        Request req = new Request();
        req.setModule(NPAY_MODULE_ID);
        req.setAction("ServiceObjectGet");
        req.setModuleID(moduleId);
        req.setAttribute("id", id);

        Document doc = transferData.postData(req, user);
        Element serviceEl = XMLUtils.selectElement(doc, "/data/object");
        if (serviceEl != null) {
            result = new NPayService();
            result.setId(id);
            result.setServiceId(Utils.parseInt(serviceEl.getAttribute("sid")));
            result.setObjectId(Utils.parseInt(serviceEl.getAttribute("oid")));
            result.setCount(Utils.parseInt(serviceEl.getAttribute("col")));
            result.setDateFrom(TimeUtils.parse(serviceEl.getAttribute("date1"), TimeUtils.PATTERN_DDMMYYYY));
            result.setDateTo(TimeUtils.parse(serviceEl.getAttribute("date2"), TimeUtils.PATTERN_DDMMYYYY));
            result.setComment(linesToString(XMLUtils.selectElement(doc, "/data/comment/")));
        }

        return result;*/
    }

    public static class ServiceObject8x {
        // наименование объекта (для выборки в гуй)
        public String objTitle;
        // код записи
        private int id;
        // код договора
        private int contractId;
        // код объекта
        private int objectId;
        // код услуги
        private int serviceId;
        // количество
        private int col;
        // наименование услуги (для выборки в гуй)
        private String serviceTitle;
        // код модуля, к сущности которого привязана абонплата
        private int entityMid;
        // код сущности в модуле, к которой привязана абонплата
        private int entityId;
        // дата с
        private Date date1;
        // дата по
        private Date date2;
        // примечание
        private String comment = "";

        public String getObjectTitle() {
            return objTitle;
        }

        public void setObjectTitle(String objectTitle) {
            this.objTitle = objectTitle;
        }

        public Date getDate1() {
            return date1;
        }

        public void setDate1(Date serviceDate1) {
            this.date1 = serviceDate1;
        }

        public Date getDate2() {
            return date2;
        }

        public void setDate2(Date serviceDate2) {
            this.date2 = serviceDate2;
        }

        public String getServiceTitle() {
            return serviceTitle;
        }

        public void setServiceTitle(String serviceTitle) {
            this.serviceTitle = serviceTitle;
        }

        public int getId() {
            return id;
        }

        public void setId(int contractServiceId) {
            this.id = contractServiceId;
        }

        public int getObjectId() {
            return objectId;
        }

        public void setObjectId(int objectId) {
            this.objectId = objectId;
        }

        public int getServiceId() {
            return serviceId;
        }

        public void setServiceId(int serviceId) {
            this.serviceId = serviceId;
        }

        public int getCol() {
            if (col <= 0) {
                col = 1;
            }
            return col;
        }

        public void setCol(int col) {
            this.col = col;
        }

        public int getEntityId() {
            return entityId;
        }

        public void setEntityId(int entityId) {
            this.entityId = entityId;
        }

        public int getEntityMid() {
            return entityMid;
        }

        public void setEntityMid(int entityMid) {
            this.entityMid = entityMid;
        }

        public int getContractId() {
            return contractId;
        }

        public void setContractId(int contractId) {
            this.contractId = contractId;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

    }
}
