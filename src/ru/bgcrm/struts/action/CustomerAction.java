package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.List;
import java.util.SortedMap;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.dao.param.ParamDAO;
import org.bgerp.dao.param.ParamGroupDAO;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.param.Parameter;
import org.bgerp.model.param.ParameterValue;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.dao.PatternDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.customer.CustomerChangedEvent;
import ru.bgcrm.event.customer.CustomerRemovedEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Action(path = "/user/customer")
public class CustomerAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_USER + "/customer";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        return customer(form, conSet);
    }

    public ActionForward customerCreate(DynActionForm form, ConnectionSet conSet) throws Exception {
        String title = form.getParam("title", l.l("Новый контрагент"));

        Customer customer = new Customer();
        customer.setTitle(title);

        new CustomerDAO(conSet.getConnection()).updateCustomer(customer);

        form.setResponseData("customer", customer);

        return json(conSet, form);
    }

    public ActionForward customerGet(DynActionForm form, ConnectionSet conSet) throws Exception {
        Connection con = conSet.getConnection();

        CustomerDAO customerDAO = new CustomerDAO(con);
        PatternDAO patternDAO = new PatternDAO(con);
        ParamGroupDAO groupDAO = new ParamGroupDAO(con);

        Customer customer = customerDAO.getCustomerById(form.getId());
        if (customer != null) {
            customer.setGroupIds(customerDAO.getGroupIds(form.getId()));
            form.setResponseData("customer", customer);

            // TODO: Переделать на кэш.
            var request = form.getHttpRequest();
            request.setAttribute("patternList", patternDAO.getPatternList(Customer.OBJECT_TYPE));
            request.setAttribute("parameterGroupList", groupDAO.getParameterGroupList(Customer.OBJECT_TYPE));
        }

        return html(conSet, form, PATH_JSP + "/edit.jsp");
    }

    public ActionForward customerUpdate(DynActionForm form, ConnectionSet conSet) throws Exception {
        CustomerDAO customerDAO = new CustomerDAO(conSet.getConnection(), true, form.getUserId());

        Customer customer = customerDAO.getCustomerById(form.getId());
        if (customer == null) {
            throw new BGMessageException("Контрагент не найден.");
        }

        String titleBefore = Utils.maskNull(customer.getTitle());

        customer.setTitle(form.getParam("title"));
        customer.setTitlePattern(form.getParam("titlePattern", ""));
        customer.setTitlePatternId(form.getParamInt("titlePatternId", -1));
        customer.setParamGroupId(Utils.parseInt(form.getParam("parameterGroupId")));
        customer.setGroupIds(form.getParamValues("customerGroupId"));

        if (Utils.isBlankString(customer.getTitle()) && customer.getTitlePatternId() <= 0 && Utils.isBlankString(customer.getTitlePattern())) {
            throw new BGIllegalArgumentException();
        }

        customerDAO.updateCustomerTitle(titleBefore, customer, -1, form.getResponse());
        customerDAO.updateGroupIds(customer.getId(), form.getParamValues("customerGroupId"));

        CustomerChangedEvent updateEvent = new CustomerChangedEvent(form, form.getId());
        EventProcessor.processEvent(updateEvent, conSet);

        return json(conSet, form);
    }

    public ActionForward customerDelete(DynActionForm form, ConnectionSet conSet) throws Exception {
        Connection con = conSet.getConnection();

        new CustomerDAO(con).deleteCustomer(form.getId());
        new ParamValueDAO(con).deleteParams(Customer.OBJECT_TYPE, form.getId());
        new CustomerLinkDAO(con).deleteObjectLinks(form.getId());

        CustomerRemovedEvent deleteEvent = new CustomerRemovedEvent(form, form.getId());
        EventProcessor.processEvent(deleteEvent, new SingleConnectionSet(con));

        return json(conSet, form);
    }

    public ActionForward customer(DynActionForm form, ConnectionSet conSet) throws Exception {
        CustomerDAO customerDAO = new CustomerDAO(conSet.getConnection());

        Customer customer = customerDAO.getCustomerById(form.getId());
        if (customer != null) {
            customer.setGroupIds(customerDAO.getGroupIds(form.getId()));
            form.setResponseData("customer", customer);
        }

        return html(conSet, form, PATH_JSP + "/customer.jsp");
    }

    public ActionForward customerTitleList(DynActionForm form, ConnectionSet conSet) throws Exception {
        List<String> titles = new CustomerDAO(conSet.getConnection()).getCustomerTitles(
                LikePattern.SUB.get(form.getParam("title")),
                setup.getInt("customer.search.by.title.count", 10));
        form.setResponseData("list", titles);

        return html(conSet, form, PATH_JSP + "/customer.jsp");
    }

    public ActionForward customerMerge(DynActionForm form, ConnectionSet conSet) throws Exception {
        Integer customerId = form.getParamInt("customerId");
        Integer mergingCustomerId = form.getParamInt("mergingCustomerId");

        Connection con = conSet.getConnection();

        ParamValueDAO paramValueDAO = new ParamValueDAO(con);
        CustomerLinkDAO customerLinkDAO = new CustomerLinkDAO(con);
        List<Parameter> customerParameterList = new ParamDAO(con).getParameterList(Customer.OBJECT_TYPE, 0);

        List<ParameterValue> customerParamValues = paramValueDAO.loadParameters(customerParameterList, customerId, true);
        List<ParameterValue> mergingCustomerParamValues = paramValueDAO.loadParameters(customerParameterList, mergingCustomerId, true);

        // params copy
        for (Parameter param : customerParameterList) {
            String type = param.getType();
            Object paramCustomerValue = "";
            Object paramMergingCustomerValue = "";

            int paramId = param.getId();

            for (ParameterValue customerPVP : customerParamValues) {
                if (customerPVP.getParameter().getId() == paramId) {
                    paramCustomerValue = customerPVP.getValue();
                    break;
                }
            }

            for (ParameterValue mergingCustomerPVP : mergingCustomerParamValues) {
                if (mergingCustomerPVP.getParameter().getId() == paramId) {
                    paramMergingCustomerValue = mergingCustomerPVP.getValue();
                    break;
                }
            }

            if (paramCustomerValue != null && paramMergingCustomerValue != null) {
                // param merging
                boolean isMultiple = param.getConfigMap().getBoolean(Parameter.PARAM_MULTIPLE_KEY, false);

                if (Parameter.TYPE_ADDRESS.equals(type) && isMultiple) {
                    SortedMap<Integer, ParameterAddressValue> customerAddressMap = paramValueDAO.getParamAddress(customerId, paramId);
                    SortedMap<Integer, ParameterAddressValue> mergingCustomerAddressMap = paramValueDAO.getParamAddress(mergingCustomerId, paramId);

                    for (ParameterAddressValue addressValue : mergingCustomerAddressMap.values()) {
                        boolean exist = false;
                        for (ParameterAddressValue existAddressValue : customerAddressMap.values()) {
                            if (existAddressValue.equals(addressValue)) {
                                exist = true;
                            }
                        }

                        if (!exist) {
                            paramValueDAO.updateParamAddress(customerId, paramId, 0, addressValue);
                        }
                    }
                } else if (Parameter.TYPE_PHONE.equals(type)) {
                    ParameterPhoneValue customerPhoneValue = paramValueDAO.getParamPhone(customerId, paramId);
                    ParameterPhoneValue mergingCustomerPhoneValue = paramValueDAO.getParamPhone(mergingCustomerId, paramId);

                    for (ParameterPhoneValueItem phoneItem : mergingCustomerPhoneValue.getItemList()) {
                        boolean exist = false;
                        for (ParameterPhoneValueItem existPhoneValue : customerPhoneValue.getItemList()) {
                            if (existPhoneValue.equals(phoneItem)) {
                                exist = true;
                            }
                        }

                        if (!exist) {
                            customerPhoneValue.addItem(phoneItem);
                        }
                    }

                    paramValueDAO.updateParamPhone(customerId, paramId, customerPhoneValue);

                } else if (Parameter.TYPE_EMAIL.equals(type) && isMultiple) {
                    SortedMap<Integer, ParameterEmailValue> mergingCustomerEmailMap = paramValueDAO.getParamEmail(mergingCustomerId, paramId);
                    SortedMap<Integer, ParameterEmailValue> customerEmailMap = paramValueDAO.getParamEmail(customerId, paramId);

                    for (ParameterEmailValue emailValue : mergingCustomerEmailMap.values()) {
                        boolean exist = false;
                        for (ParameterEmailValue existEmailValue : customerEmailMap.values()) {
                            if (existEmailValue.equals(emailValue)) {
                                exist = true;
                            }
                        }

                        if (!exist) {
                            paramValueDAO.updateParamEmail(customerId, paramId, 0, emailValue);
                        }
                    }
                } else if (!paramCustomerValue.equals(paramMergingCustomerValue)) {
                    throw new BGMessageException("Параметр '" + param.getTitle() + "' должны совпадать");
                }
            } else if (paramMergingCustomerValue != null) {
                paramValueDAO.copyParam(mergingCustomerId, customerId, paramId);
            }
            //оба null, оба пустые или менять не надо
        }

        // links copy (there are only bgbilling contracts)
        for (CommonObjectLink link : customerLinkDAO.getObjectLinksWithType(mergingCustomerId, "")) {
            customerLinkDAO.deleteLink(link);

            link.setObjectId(customerId);
            link.setObjectType("customer");

            LinkAddingEvent event = new LinkAddingEvent(form, link);
            EventProcessor.processEvent(event, new SingleConnectionSet(con));

            customerLinkDAO.addLink(link);
        }

        new ProcessLinkDAO(con).linkToAnotherObject(mergingCustomerId, "customer", customerId, "customer", "", "");

        EventProcessor.processEvent(new CustomerChangedEvent(form, customerId), conSet);
        EventProcessor.processEvent(new CustomerRemovedEvent(form, mergingCustomerId), conSet);

        // customer deletion of merged customer
        new CustomerDAO(con).deleteCustomer(mergingCustomerId);
        new CustomerLinkDAO(con).deleteObjectLinks(mergingCustomerId);

        return json(con, form);
    }

    protected void formatCustomerTitle(Customer customer, CustomerDAO customerDAO, ParamValueDAO paramDAO, Connection con) throws Exception {
        PatternDAO patternDAO = new PatternDAO(con);
        String titlePattern = customer.getTitlePattern();
        if (customer.getTitlePatternId() > 0) {
            ru.bgcrm.model.param.Pattern pattern = patternDAO.getPattern(customer.getTitlePatternId());
            if (pattern != null) {
                titlePattern = pattern.getPattern();
            }
        }
        customer.setTitle(Utils.formatPatternString(Customer.OBJECT_TYPE, customer.getId(), paramDAO, titlePattern));
        customerDAO.updateCustomer(customer);
    }

    protected void setCustomerTitle(String title, Customer customer, PatternDAO patternDAO, ParamValueDAO paramDAO) throws Exception {
        if (customer.getTitlePatternId() == 0) {
            customer.setTitle(Utils.formatPatternString(Customer.OBJECT_TYPE, customer.getId(), paramDAO, customer.getTitlePattern()));
        } else if (customer.getTitlePatternId() > 0) {
            ru.bgcrm.model.param.Pattern pattern = patternDAO.getPattern(customer.getTitlePatternId());
            customer.setTitle(Utils.formatPatternString(Customer.OBJECT_TYPE, customer.getId(), paramDAO, pattern.getPattern()));
        } else {
            customer.setTitle(title);
        }
    }
}
