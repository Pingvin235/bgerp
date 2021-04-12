package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.ParamList;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.address.AddressSearchedObject;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO.SearchOptions;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractHierarchyDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractObjectDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractObjectParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractScriptDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractServiceDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractStatusDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.CrmDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.DialUpDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.PhoneDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.PhoneDAO.FindPhoneMode;
import ru.bgcrm.plugin.bgbilling.proto.dao.PhoneDAO.FindPhoneSortMode;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractFace;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractMode;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractParameter;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractService;
import ru.bgcrm.plugin.bgbilling.proto.model.DeviceInfo;
import ru.bgcrm.plugin.bgbilling.proto.model.ParamAddressValue;
import ru.bgcrm.plugin.bgbilling.proto.model.ParameterType;
import ru.bgcrm.plugin.bgbilling.proto.model.dialup.DialUpLogin;
import ru.bgcrm.plugin.bgbilling.proto.model.limit.LimitChangeTask;
import ru.bgcrm.plugin.bgbilling.proto.model.limit.LimitLogItem;
import ru.bgcrm.plugin.bgbilling.proto.model.phone.ContractPhoneRecord;
import ru.bgcrm.plugin.bgbilling.proto.model.script.ContractScriptLogItem;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.struts.form.Response;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ContractAction extends BaseAction {

	public ActionForward searchContract(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String searchBy = form.getParam("searchBy");
		String billingId = form.getParam("billingId");
		
		String searchBySuffix = form.getParam("searchBySuffix");
		if (Utils.notBlankString(searchBySuffix)) {
			searchBy += searchBySuffix;
		}

		boolean showClosed = form.getParamBoolean("show_closed", false);
		boolean showSub = form.getParamBoolean("show_sub", false);
		boolean showDel = form.getParamBoolean("show_invisible", false);
		SearchOptions searchOptions = new SearchOptions(showDel, showClosed, showSub);

		User user = form.getUser();

		if (Utils.notBlankString(searchBy)) {
			ContractDAO contractDAO = new ContractDAO(user, billingId);

			if ("address".equals(searchBy)) {
				SearchResult<AddressSearchedObject<Contract>> res = new SearchResult<>(form);
				contractDAO.searchContractByAddressParam(res, searchOptions, null, 
						form.getParamInt("streetId"), form.getParam("house"), form.getParam("flat"), form.getParam("room"));
			} else if ("addressObject".equals(searchBy)) {
				SearchResult<ParameterSearchedObject<Contract>> result = new SearchResult<>(form);
				contractDAO.searchContractByObjectAddressParam(result, searchOptions, null,
						form.getParamInt("streetId"), form.getParam("house"), form.getParam("flat"), form.getParam("room"));
			} else if ("id".equals(searchBy)) {
				SearchResult<IdTitle> result = new SearchResult<IdTitle>(form);

				Contract contract = contractDAO.getContractById(form.getParamInt("id"));
				if (contract != null) {
					result.getList().add(new IdTitle(contract.getId(), contract.getTitle()));
				}
			} else if ("title".equals(searchBy) || "comment".equals(searchBy)) {
				SearchResult<IdTitle> result = new SearchResult<IdTitle>(form);
				contractDAO.searchContractByTitleComment(result, form.getParam("title"), form.getParam("comment"),
						searchOptions);
			} else if (searchBy.startsWith("dialUpLogin_")) {
				int moduleId = Utils.parseInt(StringUtils.substringAfterLast(searchBy, "_"));
				String login = form.getParam("login_" + billingId + "_" + moduleId, "");
				if (login.length() >= 3) {
					DialUpDAO dialUpDao = new DialUpDAO(user, billingId, moduleId);
					List<DialUpLogin> result = dialUpDao.findLogin(login,
							Utils.parseInt(login) > 0 ? DialUpDAO.FIND_MODE_LOGIN : DialUpDAO.FIND_MODE_ALIAS);
					form.getResponse().setData("list", result);
				}
            } else if (searchBy.equals("parameter_text")) {
                SearchResult<Contract> result = new SearchResult<Contract>(form);
                contractDAO.searchContractByTextParam(result, searchOptions, 
                        getParasmIdsSet(form), form.getParam("value"));
			} else if (searchBy.equals("phone")) {
				PhoneDAO phoneDAO = new PhoneDAO(user, billingId, form.getParamInt("moduleId"));

				phoneDAO.findPhone(new SearchResult<ContractPhoneRecord>(form),
						FindPhoneMode.fromString(form.getParam("mode")),
						FindPhoneSortMode.fromString(form.getParam("sort")), form.getParam("phone"),
						form.getParamDate("dateFrom"), form.getParamDate("dateTo"));
            } else if (searchBy.equals("parameter_date")) {
                SearchResult<Contract> result = new SearchResult<Contract>(form);
                contractDAO.searchContractByDateParam(result, searchOptions, 
                        getParasmIdsSet(form),
                        form.getParamDate("date_from"), form.getParamDate("date_to"));
            } else if (searchBy.equals("parameter_phone")) {
                SearchResult<Contract> result = new SearchResult<Contract>(form);
                contractDAO.searchContractByPhoneParam(result, searchOptions, 
                        getParasmIdsSet(form), form.getParam("value"));
            }
		}

		return html(conSet, mapping, form, "searchContractResult");
	}
	
    private Set<Integer> getParasmIdsSet(DynActionForm form) throws BGException {
        String[] vals = form.getParamArray("paramIds");
        if (vals == null) {
            return getParamListImpl(form).stream()
                    .map(i -> i.getId())
                    .collect(Collectors.toSet());
        }
        return Arrays.stream(vals).map(i -> Utils.parseInt(i, -1)).collect(Collectors.toSet());
    }

	private List<ContractParameter> filterParameterList(List<ContractParameter> contractParameterList,
			Set<Integer> requiredParameterIds) {
		if (requiredParameterIds.isEmpty()) {
			return contractParameterList;
		}

		List<ContractParameter> filteredContractParameterList = new ArrayList<ContractParameter>();

		for (ContractParameter contractParameter : contractParameterList) {
			if (requiredParameterIds.contains(contractParameter.getParamId())) {
				filteredContractParameterList.add(contractParameter);
			}
		}

		return filteredContractParameterList;
	}

	public ActionForward parameterList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		Set<Integer> requiredParameterIds = Utils.toIntegerSet(form.getParam("requiredParameterIds", ""));

		ContractParamDAO paramDAO = new ContractParamDAO(form.getUser(), billingId);

		Pair<ParamList, List<ContractParameter>> parameterListWithDir = paramDAO.getParameterListWithDir(contractId,
				true, form.getParamBoolean("onlyFromGroup", false));

		form.getResponse().setData("group", parameterListWithDir.getFirst());
		form.getResponse().setData("contractParameterList",
				filterParameterList(parameterListWithDir.getSecond(), requiredParameterIds));

		return html(conSet, mapping, form, "contractParameterList");
	}

	public ActionForward parameterGet(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		Integer paramId = form.getParamInt("paramId");
		int paramType = form.getParamInt("paramType");

		ContractParamDAO paramDAO = new ContractParamDAO(form.getUser(), billingId);

		Response resp = form.getResponse();

		if (paramType <= 0) {
			throw new BGMessageException("Параметр не поддерживается для редактирования");
		}

		switch (paramType) {
			case ParameterType.ContractType.TYPE_TEXT: {
				break;
			}
			case ParameterType.ContractType.TYPE_ADDRESS: {
				ParameterAddressValue addressValue = ContractParamDAO
						.toCrmObject(paramDAO.getAddressParam(contractId, paramId), conSet.getConnection());
				if (addressValue != null) {
					int houseId = addressValue.getHouseId();
	
					AddressHouse house = new AddressDAO(conSet.getConnection()).getAddressHouse(houseId, true, true, true);
					if (house != null) {
						resp.setData("house", house);
					}
				}
				resp.setData("address", addressValue);
				break;
			}
			case ParameterType.ContractType.TYPE_DATE: {
				/*
				 * if( Utils.notBlankString( parameter.getValue() ) ) {
				 * resp.setData( "dateValue", new SimpleDateFormat( "yyyy-MM-dd"
				 * ).format( TimeUtils.parseDateWithPattern(
				 * parameter.getValue(), TimeUtils.PATTERN_DDMMYYYY ) ) ); }
				 */
				break;
			}
			case ParameterType.ContractType.TYPE_LIST: {
				resp.setData("value", paramDAO.getListParamValue(contractId, paramId));
				break;
			}
			case ParameterType.ContractType.TYPE_PHONE: {
				ParameterPhoneValue phoneValue = new ParameterPhoneValue(paramDAO.getPhoneParam(contractId, paramId));
				if (phoneValue != null) {
					List<ParameterPhoneValueItem> itemList = phoneValue.getItemList();
	
					int i = 1;
					for (ParameterPhoneValueItem item : itemList) {
						resp.setData("parts" + i, item.getPhoneParts());
						resp.setData("comment" + i, item.getComment());
						i++;
					}
				}
				break;
			}
			case ParameterType.ContractType.TYPE_EMAIL: {
				resp.setData("emails", paramDAO.getEmailParam(contractId, paramId));
				break;
			}
			default: {
				break;
			}
		}

		return html(conSet, mapping, form, "parameterGet");
	}

	public ActionForward parameterUpdate(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");
		int contractId = form.getParamInt("contractId");
		int paramBillingId = form.getParamInt("paramId");
		int parameterType = form.getParamInt("paramType");

		Set<Integer> allowedParamIds = Utils.toIntegerSet(form.getPermission().get("parameterIds"));
		if (!allowedParamIds.isEmpty() && !allowedParamIds.contains(paramBillingId))
			throw new BGMessageException("Параметр с кодом " + paramBillingId + " запрещен для изменения.");

		ContractParamDAO paramDAO = new ContractParamDAO(form.getUser(), billingId);
		switch (parameterType) {
			case ParameterType.ContractType.TYPE_FLAG: {
				paramDAO.updateFlagParameter(contractId, paramBillingId, form.getParamBoolean("value", false));
				break;
			}
			case ParameterType.ContractType.TYPE_TEXT: {
				paramDAO.updateTextParameter(contractId, paramBillingId, form.getParam("value"));
				break;
			}
			case ParameterType.ContractType.TYPE_ADDRESS: {
				ParamAddressValue address = new ParamAddressValue();
	
				address.setStreetId(form.getParamInt("streetId "));
				address.setHouseId(form.getParamInt("houseId"));
				address.setStreetTitle(form.getParam("streetTitle"));
				address.setHouse(form.getParam("house"));
				address.setFlat(form.getParam("flat"));
				address.setRoom(form.getParam("room "));
				address.setPod(form.getParam("pod"));
				address.setFloor(form.getParam("floor"));
				address.setComment(form.getParam("comment"));
	
				paramDAO.updateAddressParameter(contractId, paramBillingId, address);
				break;
			}
			case ParameterType.ContractType.TYPE_DATE: {
				paramDAO.updateDateParameter(contractId, paramBillingId, form.getParam("value"));
				break;
			}
			case ParameterType.ContractType.TYPE_LIST: {
				paramDAO.updateListParameter(contractId, paramBillingId, form.getParam("value"));
				break;
			}
			case ParameterType.ContractType.TYPE_PHONE: {
				ParameterPhoneValue phoneValue = new ParameterPhoneValue();
	
				int paramCount = setup.getInt("param.phone.item.count", 0);
				List<ParameterPhoneValueItem> items = new ArrayList<ParameterPhoneValueItem>();
				for (int index = 1; index <= paramCount; index++) {
					ParameterPhoneValueItem item = new ParameterPhoneValueItem();
					String phonePart = null;
					StringBuilder phone = new StringBuilder();
					StringBuilder format = new StringBuilder("");
					phonePart = form.getParam("part1" + index);
					phone.append(phonePart);
					format.append(phonePart.length());
					phonePart = form.getParam("part2" + index);
					phone.append(phonePart);
					format.append(phonePart.length());
					phonePart = form.getParam("part3" + index);
					phone.append(phonePart);
	
					if (phone.length() != 0 && phone.length() != 11) {
						throw new BGMessageException("Число цифр в телефоне должно быть 11!!");
					}
	
					item.setPhone(phone.toString());
					item.setFormat(format.toString());
					item.setComment(form.getParam("comment" + index));
	
					items.add(item);
				}
				phoneValue.setItemList(items);
	
				paramDAO.updatePhoneParameter(contractId, paramBillingId, phoneValue);
				break;
			}
			case ParameterType.ContractType.TYPE_EMAIL: {
				/*
				 * ParamEmailValue emailValue = new ParamEmailValue();
				 * 
				 * List<String> emails = Utils.toList( form.getParam( "emails"
				 * ), "\n" ); emailValue.setEmails( emails );
				 * 
				 * emailValue.setEid( form.getParamInt( "eid" ) );
				 * 
				 * List<String> subscrs = form.getSelectedValuesListStr( "value"
				 * ); subscrs.removeAll( Arrays.asList( 0, -1 ) );
				 * emailValue.setSubscrs( subscrs );
				 */
	
				List<ParameterEmailValue> emails = new ArrayList<ParameterEmailValue>();
	
				for (String mail : Utils.toList(form.getParam("emails"), "\n")) {
					try {
						InternetAddress addr = InternetAddress.parse(mail)[0];
						emails.add(new ParameterEmailValue(addr.getAddress(), addr.getPersonal()));
					} catch (AddressException e) {
						throw new BGException("Некорректный адрес: " + mail, e);
					}
				}
	
				paramDAO.updateEmailParameter(contractId, paramBillingId, emails);
				break;
			}
			default: {
				break;
			}
		}

		return json(conSet, form);
	}

	public ActionForward parameterGroupUpdate(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		int paramGroupId = form.getParamInt("paramGroupId");

		new ContractParamDAO(form.getUser(), billingId).updateParameterGroup(contractId, paramGroupId);

		return json(conSet, form);
	}

	public ActionForward objectLinkList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		Integer contractId = form.getParamInt("contractId");
		Integer cityId = form.getParamInt("cityId");

		Set<DeviceInfo.BaseLink> baseLinks = new DeviceInfo().getDeviceInfo(contractId, cityId);

		form.getResponse().setData("links", baseLinks);

		return html(conSet, mapping, form, "objectLinkList");
	}

	public ActionForward additionalActionList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractDAO crmDAO = new ContractDAO(form.getUser(), billingId);

		form.getResponse().setData("additionalActionList", crmDAO.additionalActionList(contractId));

		return html(conSet, mapping, form, "additionalActionList");
	}

	public ActionForward executeAdditionalAction(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		Integer actionId = form.getParamInt("actionId");

		ContractDAO crmDAO = new ContractDAO(form.getUser(), billingId);

		form.getResponse().setData("executeResult", crmDAO.executeAdditionalAction(contractId, actionId));
		form.getResponse().setData("additionalActionList", crmDAO.additionalActionList(contractId));

		return html(conSet, mapping, form, "additionalActionList");
	}

	public ActionForward groupList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		Pair<List<IdTitle>, Set<Integer>> groupsGet = new ContractDAO(form.getUser(), billingId).groupsGet(contractId);

		form.getResponse().setData("groupList", groupsGet.getFirst());
		form.getResponse().setData("selectedGroupIds", groupsGet.getSecond());

		return html(conSet, mapping, form, "groupList");
	}

	@SuppressWarnings("unchecked")
	public ActionForward updateGroups(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		Set<Integer> groupIds = form.getSelectedValues("groupId");
		// String command = form.getParam( "command" );

		ContractDAO contractDao = new ContractDAO(form.getUser(), billingId);
		Set<Integer> currentGroups = contractDao.groupsGet(contractId).getSecond();

		for (Integer deleteGroup : (Iterable<Integer>) CollectionUtils.subtract(currentGroups, groupIds)) {
			contractDao.updateGroup("del", contractId, deleteGroup);
		}
		for (Integer addGroup : (Iterable<Integer>) CollectionUtils.subtract(groupIds, currentGroups)) {
			contractDao.updateGroup("add", contractId, addGroup);
		}

		return json(conSet, form);
	}

	public ActionForward memoList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractDAO contractDAO = new ContractDAO(form.getUser(), billingId);
		form.getResponse().setData("memoList", contractDAO.getMemoList(contractId));

		return html(conSet, mapping, form, "memoList");
	}

	public ActionForward getMemo(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		if (form.getId() > 0) {
			form.getResponse().setData("memo",
					new ContractDAO(form.getUser(), billingId).getMemo(contractId, form.getId()));
		}

		return html(conSet, mapping, form, "memoEditor");
	}

	public ActionForward updateMemo(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		Integer memoId = form.getParamInt("id", 0);
		String memoTitle = form.getParam("title");
		String memoText = form.getParam("text");

		ContractDAO crmDAO = new ContractDAO(form.getUser(), billingId);
		crmDAO.updateMemo(contractId, memoId, memoTitle, memoText);

		return json(conSet, form);
	}

	public ActionForward deleteMemo(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		Integer memoId = form.getParamInt("id", 0);

		if (memoId <= 0) {
			throw new BGIllegalArgumentException();
		}

		ContractDAO crmDAO = new ContractDAO(form.getUser(), billingId);
		crmDAO.deleteMemo(contractId, memoId);

		return json(conSet, form);
	}

	public ActionForward contractObjectList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractObjectDAO contractObjectDAO = new ContractObjectDAO(form.getUser(), billingId);
		form.getResponse().setData("objectList", contractObjectDAO.getContractObjects(contractId));

		return html(conSet, mapping, form, "contractObjectList");
	}

	public ActionForward getContractObject(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer objectId = form.getParamInt("objectId");

		ContractObjectDAO contractObjectDAO = new ContractObjectDAO(form.getUser(), billingId);
		form.getResponse().setData("object", contractObjectDAO.getContractObject(objectId));

		return html(conSet, mapping, form, "contractObjectEditor");
	}

	public ActionForward deleteContractObject(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		Integer objectId = form.getParamInt("objectId");

		ContractObjectDAO contractObjectDAO = new ContractObjectDAO(form.getUser(), billingId);
		contractObjectDAO.deleteContractObject(contractId, objectId);

		return json(conSet, form);
	}

	public ActionForward updateContractObject(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		String title = form.getParam("title");
		int objectId = form.getParamInt("objectId");
		int typeId = form.getParamInt("typeId");
		Date dateFrom = form.getParamDate("dateFrom");
		Date dateTo = form.getParamDate("dateTo");

		ContractObjectDAO contractObjectDAO = new ContractObjectDAO(form.getUser(), billingId);
		contractObjectDAO.updateContractObject(objectId, title, dateFrom, dateTo, typeId, 0);

		return json(conSet, form);
	}

	public ActionForward contractObjectParameterList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer objectId = form.getParamInt("objectId");

		ContractObjectParamDAO paramDAO = new ContractObjectParamDAO(form.getUser(), billingId);
		form.getResponse().setData("parameterList", paramDAO.getParameterList(objectId));

		return html(conSet, mapping, form, "contractObjectParameterList");
	}

	public ActionForward getObjectParameter(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer objectId = form.getParamInt("objectId");
		Integer paramId = form.getParamInt("paramId");

		ContractObjectParamDAO paramDAO = new ContractObjectParamDAO(form.getUser(), billingId);
		ContractParameter parameter = paramDAO.getParameter(objectId, paramId);

		form.getResponse().setData("parameter", parameter);

		int paramType = parameter.getParamType();
		if (paramType <= 0) {
			throw new BGMessageException("Параметр не поддерживается для редактирования");
		}

		switch (paramType) {
			case ParameterType.ContractObjectType.TYPE_TEXT: {
				break;
			}
	
			case ParameterType.ContractObjectType.TYPE_ADDRESS: {
				ParameterAddressValue addressValue = ContractObjectParamDAO
						.toCrmObject(paramDAO.getAddressParam(objectId, paramId), conSet.getConnection());
				if (addressValue != null) {
					int houseId = addressValue.getHouseId();
	
					AddressHouse house = new AddressDAO(conSet.getConnection()).getAddressHouse(houseId, true, true, true);
					if (house != null) {
						form.getResponse().setData("house", house);
					}
				}
	
				form.getResponse().setData("address", addressValue);
				break;
			}
	
			case ParameterType.ContractObjectType.TYPE_DATE: {
				if (Utils.notBlankString(parameter.getValue())) {
					form.getResponse().setData("dateValue", new SimpleDateFormat("yyyy-MM-dd")
							.format(TimeUtils.parse(parameter.getValue(), TimeUtils.PATTERN_DDMMYYYY)));
				}
				break;
			}
	
			case ParameterType.ContractObjectType.TYPE_LIST: {
				form.getResponse().setData("valueList", paramDAO.getListParam(objectId, paramId));
				break;
			}
		}

		return html(conSet, mapping, form, "getObjectParameter");
	}

	public ActionForward updateObjectParameter(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		String billingId = form.getParam("billingId");
		Integer objectId = form.getParamInt("objectId");
		Integer paramBillingId = form.getParamInt("paramId");
		Integer parameterType = form.getParamInt("paramType");

		ContractObjectParamDAO paramDAO = new ContractObjectParamDAO(form.getUser(), billingId);

		switch (parameterType) {
			case ParameterType.ContractObjectType.TYPE_TEXT:
				paramDAO.updateTextParameter(objectId, paramBillingId, form.getParam("textValue"));
				break;
	
			case ParameterType.ContractObjectType.TYPE_ADDRESS:
				ParamAddressValue address = new ParamAddressValue();
	
				address.setStreetId(form.getParamInt("streetId "));
				address.setHouseId(form.getParamInt("houseId"));
				address.setStreetTitle(form.getParam("streetTitle"));
				address.setHouse(form.getParam("house"));
				address.setFlat(form.getParam("flat"));
				address.setRoom(form.getParam("room "));
				address.setPod(form.getParam("pod"));
				address.setFloor(form.getParam("floor"));
				address.setComment(form.getParam("comment"));
	
				paramDAO.updateAddressParameter(objectId, paramBillingId, address);
				break;
	
			case ParameterType.ContractObjectType.TYPE_DATE:
				paramDAO.updateDateParameter(objectId, paramBillingId, form.getParam("dateValue"));
				break;
	
			case ParameterType.ContractObjectType.TYPE_LIST:
				paramDAO.updateListParameter(objectId, paramBillingId, form.getParam("listValueId"));
				break;
	
			default:
				break;
		}

		return json(conSet, form);
	}

	public ActionForward contractObjectModuleInfo(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer objectId = form.getParamInt("objectId");

		CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);
		form.getResponse().setData("moduleInfo", crmDAO.contractObjectModuleList(objectId));

		return json(conSet, form);
	}

	public ActionForward contractObjectModuleSummaryTable(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer objectId = form.getParamInt("objectId");

		CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);
		form.getResponse().setData("moduleInfo", crmDAO.contractObjectModuleList(objectId));

		return html(conSet, mapping, form, "contractObjectModuleSummaryTable");
	}

	public ActionForward contractSubcontractList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractHierarchyDAO crmDAO = new ContractHierarchyDAO(form.getUser(), billingId);
		form.getResponse().setData("subContractList", crmDAO.contractSubcontractList(contractId));
		form.getResponse().setData("superContract", crmDAO.contractSupercontract(contractId));

		return html(conSet, mapping, form, "contractSubcontractList");
	}

	public ActionForward scriptList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		form.getResponse().setData("scriptList",
				new ContractScriptDAO(form.getUser(), billingId).contractScriptList(contractId));

		form.getHttpRequest().setAttribute("contractInfo", new ContractDAO(form.getUser(), billingId).getContractInfo(contractId));

		return html(conSet, mapping, form, "scriptList");
	}

	public ActionForward getScript(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer scriptId = form.getParamInt("scriptId");

		ContractScriptDAO crmDAO = new ContractScriptDAO(form.getUser(), billingId);
		form.getResponse().setData("script", crmDAO.getContractScript(scriptId));
		form.getResponse().setData("scriptTypeList", new DirectoryDAO(form.getUser(), billingId).scriptTypeList());

		return html(conSet, mapping, form, "scriptEditor");
	}

	public ActionForward scriptLog(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		String dateFrom = form.getParam("dateFrom");
		String dateTo = form.getParam("dateTo");

		SearchResult<ContractScriptLogItem> result = new SearchResult<ContractScriptLogItem>(form);

		ContractScriptDAO crmDAO = new ContractScriptDAO(form.getUser(), billingId);
		crmDAO.contractScriptLogList(result, contractId, dateFrom, dateTo);

		return html(conSet, mapping, form, "scriptLog");
	}

	public ActionForward deleteScript(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer scriptId = form.getParamInt("scriptId");

		ContractScriptDAO crmDAO = new ContractScriptDAO(form.getUser(), billingId);
		crmDAO.deleteContractScript(scriptId);

		return json(conSet, form);
	}

	public ActionForward updateScript(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		Integer scriptId = form.getParamInt("scriptId");
		Integer scriptTypeId = form.getParamInt("scriptTypeId");
		String comment = form.getParam("comment");
		String dateFrom = form.getParam("dateFrom");
		String dateTo = form.getParam("dateTo");

		ContractScriptDAO crmDAO = new ContractScriptDAO(form.getUser(), billingId);
		crmDAO.updateContractScript(contractId, scriptId, scriptTypeId, comment, dateFrom, dateTo);

		return json(conSet, form);
	}

	public ActionForward faceLog(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractDAO contractDao = new ContractDAO(form.getUser(), billingId);
		contractDao.faceLog(new SearchResult<ContractFace>(form), contractId);

		form.getResponse().setData("contractInfo", contractDao.getContractInfo(contractId));

		return html(conSet, mapping, form, "faceLog");
	}

	public ActionForward updateFace(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		new ContractDAO(form.getUser(), billingId).updateFace(contractId, form.getParamInt("value"));

		return json(conSet, form);
	}

	public ActionForward modeLog(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractDAO contractDao = new ContractDAO(form.getUser(), billingId);
		contractDao.modeLog(new SearchResult<ContractMode>(form), contractId);

		form.getResponse().setData("contractInfo", contractDao.getContractInfo(contractId));

		return html(conSet, mapping, form, "modeLog");
	}

	public ActionForward updateMode(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		new ContractDAO(form.getUser(), billingId).updateMode(contractId, form.getParamInt("value"));

		return json(conSet, form);
	}

	public ActionForward moduleList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		Pair<List<IdTitle>, List<IdTitle>> pair = new ContractDAO(form.getUser(), billingId).moduleList(contractId);
		form.getResponse().setData("selectedList", pair.getFirst());
		form.getResponse().setData("availableList", pair.getSecond());

		return html(conSet, mapping, form, "moduleList");
	}

	public ActionForward updateModules(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		Set<Integer> moduleIds = form.getSelectedValues("moduleId");
		String command = form.getParam("command");

		if (Utils.notBlankString(command)) {
			ContractDAO contractDao = new ContractDAO(form.getUser(), billingId);
			for (Integer moduleId : moduleIds) {
				contractDao.updateModule(contractId, moduleId, command);
			}
		}

		return json(conSet, form);
	}

	public ActionForward status(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractStatusDAO statusDao = new ContractStatusDAO(form.getUser(), billingId);
		form.getResponse().setData("statusList", statusDao.statusList(contractId));
		form.getResponse().setData("statusLog", statusDao.statusLog(contractId));
		form.getResponse().setData("availableStatusList",
				new DirectoryDAO(form.getUser(), billingId).getContractStatusList());

		form.getHttpRequest().setAttribute("contractInfo", new ContractDAO(form.getUser(), billingId).getContractInfo(contractId));

		return html(conSet, mapping, form, "status");
	}

	public ActionForward updateStatus(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		new ContractStatusDAO(form.getUser(), billingId).updateStatus(contractId, form.getParamInt("statusId"),
				form.getParamDate("dateFrom"), form.getParamDate("dateTo"), form.getParam("comment"));

		return json(conSet, form);
	}

	public ActionForward limit(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		SearchResult<LimitLogItem> limitList = new SearchResult<LimitLogItem>(form);
		List<LimitChangeTask> taskList = new ArrayList<LimitChangeTask>();

		BigDecimal limit = new ContractDAO(form.getUser(), billingId).limit(contractId, limitList, taskList);

		form.getResponse().setData("limit", limit);
		form.getResponse().setData("taskList", taskList);

		return html(conSet, mapping, form, "limit");
	}

	public ActionForward updateLimit(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		new ContractDAO(form.getUser(), billingId).updateLimit(contractId,
				Utils.parseBigDecimal(form.getParam("value")), form.getParamInt("period"),
				form.getParam("comment", ""));

		return json(conSet, form);
	}

	public ActionForward deleteLimitTask(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		new ContractDAO(form.getUser(), billingId).deleteLimitTask(contractId, form.getId());

		return json(conSet, form);
	}

	public ActionForward contractCards(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractDAO contractDao = new ContractDAO(form.getUser(), billingId);
		form.getResponse().setData("cardTypeList", contractDao.getContractCardTypes(contractId));
		form.getResponse().setData("fullCard", contractDao.getContractFullCard(contractId));

		return html(conSet, mapping, form, "cards");
	}

	public ActionForward getContractCard(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		int contractId = form.getParamInt("contractId");
		String cardType = form.getParam("cardType");

		try {
			OutputStream out = form.getHttpResponse().getOutputStream();
			Utils.setFileNameHeades(form.getHttpResponse(), "card.pdf");
			out.write(new ContractDAO(form.getUser(), billingId).getContractCard2Pdf(contractId, cardType));
		} catch (Exception ex) {
			throw new BGException(ex);
		}

		return null;
	}

	public ActionForward serviceList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		int contractId = form.getParamInt("contractId");
		int moduleId = form.getParamInt("moduleId");

		form.getResponse().setData("list",
				new ContractServiceDAO(form.getUser(), billingId).getContractServiceList(contractId, moduleId));

		return html(conSet, mapping, form, "serviceList");
	}

	public ActionForward serviceEdit(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		int contractId = form.getParamInt("contractId");
		int moduleId = form.getParamInt("moduleId");

		form.getResponse().setData("pair",
				new ContractServiceDAO(form.getUser(), billingId).getContractService(contractId, moduleId, form.getId(),
						form.getId() > 0 ? false : form.getParamBoolean("onlyUsing", true)));

		return html(conSet, mapping, form, "serviceEdit");
	}

	public ActionForward serviceUpdate(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		int contractId = form.getParamInt("contractId");

		ContractServiceDAO serviceDAO = new ContractServiceDAO(form.getUser(), billingId);

		for (int serviceId : form.getSelectedValuesList("serviceId")) {
			ContractService service = new ContractService();
			service.setId(form.getId());
			service.setContractId(contractId);
			service.setServiceId(serviceId);
			service.setDateFrom(form.getParamDate("dateFrom"));
			service.setDateTo(form.getParamDate("dateTo"));
			service.setComment(form.getParam("comment"));

			serviceDAO.updateContractService(service);
		}

		return json(conSet, form);
	}

	public ActionForward serviceDelete(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		int contractId = form.getParamInt("contractId");

		new ContractServiceDAO(form.getUser(), billingId).deleteContractService(contractId, form.getId());

		return json(conSet, form);
	}

	// далее сомнительные функции, которые не очень идеологически ложатся в этот
	// класс

	public ActionForward getContractStatisticPassword(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractDAO contractDAO = new ContractDAO(form.getUser(), billingId);
		form.getResponse().setData("password", contractDAO.getContractStatisticPassword(contractId));

		return json(conSet, form);
	}

	public ActionForward addressList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractDAO contractDAO = new ContractDAO(form.getUser(), billingId);

		form.getResponse().setData("contractAddressList", contractDAO.getContractAddress(contractId));

		return html(conSet, mapping, form, "contractAddressList");
	}

	public ActionForward bgbillingOpenContract(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractDAO contractDAO = new ContractDAO(form.getUser(), billingId);
		contractDAO.bgbillingOpenContract(contractId);

		return json(conSet, form);
	}

	public ActionForward bgbillingUpdateContractTitleAndComment(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		String comment = form.getParam("comment");

		ContractDAO contractDAO = new ContractDAO(form.getUser(), billingId);
		contractDAO.bgbillingUpdateContractTitleAndComment(contractId, comment, 0);

		return json(conSet, form);
	}

	public ActionForward bgbillingGetContractPatternList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		String billingId = form.getParam("billingId");

		ContractDAO contractDAO = new ContractDAO(form.getUser(), billingId);
		form.getResponse().setData("patterns", contractDAO.bgbillingGetContractPatternList());

		return json(conSet, form);
	}

	public ActionForward getSubContractList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		String billingId = form.getParam("billingId");
		int contractId = form.getParamInt("contractId");

		ContractHierarchyDAO contractDAO = new ContractHierarchyDAO(form.getUser(), billingId);
		form.getResponse().setData("subContractList", contractDAO.getSubContracts(contractId));

		return json(conSet, form);
	}

	public ActionForward openContract(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		String billingId = form.getParam("billingId");

		if (billingId == null) {
			throw new BGMessageException("Не указан параметр запроса billingId");
		}

		if (billingId.length() == 0) {
			throw new BGMessageException("Не указано значение параметра запроса billingId");
		}

		form.getResponse().setData("openContract", new ContractDAO(form.getUser(), billingId).openContract());

		return json(conSet, form);
	}

	public ActionForward getStreetsByCity(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
		String billingId = form.getParam("billingId");

		if (billingId == null) {
			throw new BGMessageException("Не указан параметр запроса billingId");
		}

		if (billingId.length() == 0) {
			throw new BGMessageException("Не указано значение параметра запроса billingId");
		}

		int cityId = form.getParamInt("cityId");

		if (cityId == 0) {
			throw new BGMessageException("Не указано значение параметра запроса cityId");
		}

		form.getResponse().setData("streets", new ContractDAO(form.getUser(), billingId).getStreetsByCity(cityId));

		return json(conSet, form);
	}
	
    public ActionForward getParamList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
        form.getResponse().setData("paramType", form.getParamInt("paramType"));
        List<IdTitle> list = getParamListImpl(form);
        form.getResponse().setData("paramList", list);
        return html(conSet, mapping, form, "searchParameterList");

    }

    private List<IdTitle> getParamListImpl(DynActionForm form) throws BGException {
        int type = form.getParamInt("paramType");
        String billingId = form.getParam("billingId");
        if (billingId == null) {
            throw new BGMessageException("Не указан параметр запроса billingId");
        }
        return new ContractDAO(form.getUser(), billingId).getParameterList(type);
    }

}
