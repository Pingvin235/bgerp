package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.proto.model.npay.NPayService;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class NPayDAO extends BillingModuleDAO {
	private static final String NPAY_MODULE_ID = "npay";

	public NPayDAO(User user, String billingId, int moduleId) throws BGException {
		super(user, billingId, moduleId);
	}

	public NPayDAO(User user, DBInfo dbInfo, int moduleId) throws BGException {
		super(user, dbInfo.getId(), moduleId);
	}

	/**
	 * Возвращает список абонплат договора.
	 * @param contractId
	 * @return
	 * @throws BGException
	 */
	public List<NPayService> getServiceList(int contractId) throws BGException {
		Request req = new Request();
		req.setModule(NPAY_MODULE_ID);
		req.setAction("ServiceObjectTable");
		req.setModuleID(moduleId);
		req.setContractId(contractId);
		req.setAttribute("object_id", 0);

		Document document = transferData.postData(req, user);

		List<NPayService> serviceList = new ArrayList<NPayService>();

		if (document != null) {
			Element dataElement = document.getDocumentElement();
			NodeList nodeList = dataElement.getElementsByTagName("row");

			for (int index = 0; index < nodeList.getLength(); index++) {
				NPayService service = new NPayService();
				Element rowElement = (Element) nodeList.item(index);

				service.setComment(rowElement.getAttribute("comment"));
				service.setDateFrom(TimeUtils.parse(rowElement.getAttribute("date1"), TimeUtils.PATTERN_DDMMYYYY));
				service.setDateTo(TimeUtils.parse(rowElement.getAttribute("date2"), TimeUtils.PATTERN_DDMMYYYY));
				service.setId(Utils.parseInt(rowElement.getAttribute("id")));
				service.setObjectId(Utils.parseInt(rowElement.getAttribute("objectId")));
				service.setObjectTitle(rowElement.getAttribute("object"));
				service.setServiceTitle(rowElement.getAttribute("service"));
				service.setServiceId(Utils.parseInt(rowElement.getAttribute("sid")));
				service.setCount(Utils.parseInt(rowElement.getAttribute("col")));
				service.setContractId(contractId);

				serviceList.add(service);
			}
		}
		return serviceList;
	}

	/**
	 * Возвращает абонплату договора по коду записи.
	 * @param id
	 * @return
	 * @throws BGException
	 */
	public NPayService getService(int id) throws BGException {
		//http://billing:8081/executer?id=3004&module=npay&action=ServiceObjectGet&mid=6&BGBillingSecret=B6bysmW9pPPyBJxGsMFq6zEz&
		//[ length = 211 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="6A6380BBFC43D59F44428773F111ADC8" status="ok"><object col="1" date1="01.05.2008" date2="" entityId="0" entityMid="0" oid="0" sid="31"/><comment/></data>

		NPayService result = null;

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

		return result;
	}

	/**
	 * Изменяет либо добавляет абонплату договора.
	 * @param service
	 * @throws BGException
	 */
	public void updateService(NPayService service) throws BGException {
		updateService(service.getId(), service.getContractId(), service.getServiceId(), service.getDateFrom(),
				service.getDateTo(), service.getObjectId(), service.getCount(), service.getComment());
	}

	/**
	 * Изменяет либо добавляет абонплату договора.
	 * @param id 0 - добавление, иначе - изменение
	 * @param contractId
	 * @param serviceId
	 * @param dateFrom
	 * @param dateTo
	 * @param objectId
	 * @param count
	 * @param comment
	 * @throws BGException
	 */
	public void updateService(int id, int contractId, int serviceId, Date dateFrom, Date dateTo,
			int objectId, int count, String comment) throws BGException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

		Request req = new Request();
		req.setModule(NPAY_MODULE_ID);
		req.setAction("ServiceObjectUpdate");
		req.setModuleID(String.valueOf(moduleId));
		req.setContractId(contractId);
		req.setAttribute("object_id", 0);
		req.setAttribute("id", id <= 0 ? "new" : id);
		req.setAttribute("sid", serviceId);
		req.setAttribute("oid", objectId);
		req.setAttribute("col", count);
		req.setAttribute("comment", comment);
		if (dateFrom != null) {
			req.setAttribute("date1", formatter.format(dateFrom));
		}
		if (dateTo != null) {
			req.setAttribute("date2", formatter.format(dateTo));
		}
		transferData.postData(req, user);
	}

	/**
	 * Удаляет абонплату договора.
	 * @param contractId
	 * @param id
	 * @throws BGException
	 */
	public void deleteService(int contractId, int id) throws BGException {
		Request req = new Request();
		req.setModule(NPAY_MODULE_ID);
		req.setAction("ServiceObjectDelete");
		req.setModuleID(moduleId);
		req.setAttribute("id", id);

		transferData.postData(req, user);
	}
}