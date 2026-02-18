package org.bgerp.plugin.bgb.getolt.action;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.cfg.Setup;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.msg.Message;
import org.bgerp.model.msg.config.MessageTypeConfig;
import org.bgerp.plugin.bgb.getolt.Config;
import org.bgerp.plugin.bgb.getolt.Plugin;
import org.bgerp.plugin.bgb.getolt.dao.GetOltApiClient;
import org.bgerp.plugin.bgb.getolt.dao.InetMacApiClient;
import org.bgerp.plugin.bgb.getolt.model.BillingConfig;
import org.bgerp.plugin.bgb.getolt.model.InetService;
import org.bgerp.plugin.bgb.getolt.model.MacUpdateResult;
import org.bgerp.plugin.bgb.getolt.model.OnuSearchResult;
import org.bgerp.plugin.bgb.getolt.model.RefreshResult;
import org.bgerp.plugin.bgb.getolt.model.SessionDropResult;
import org.bgerp.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageTypeNote;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Action for GetOLT process tab.
 * Displays ONU information based on contract binding.
 */
@Action(path = "/user/plugin/getolt/getolt", pathId = true)
public class GetOltAction extends BaseAction {
    private static final Log log = Log.getLog();
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        int processId = form.getParamInt("processId", 0);
        log.debug("GetOltAction called for processId={}", processId);

        if (processId <= 0) {
            form.setResponseData("error", "Не указан ID процесса");
            return html(conSet, form, PATH_JSP + "/onu_info.jsp");
        }

        Connection con = conSet.getConnection();
        Config config = Setup.getSetup().getConfig(Config.class);

        if (config == null || !config.isConfigured()) {
            log.warn("GetOLT plugin is not configured");
            form.setResponseData("error", "GetOLT не настроен");
            return html(conSet, form, PATH_JSP + "/onu_info.jsp");
        }

        // Get contract links from process
        ProcessLinkDAO linkDao = new ProcessLinkDAO(con);
        List<CommonObjectLink> contractLinks = linkDao.getObjectLinksWithType(processId, "contract:%");

        if (contractLinks.isEmpty()) {
            form.setResponseData("error", "Нет привязанных договоров");
            return html(conSet, form, PATH_JSP + "/onu_info.jsp");
        }

        if (contractLinks.size() > 1) {
            log.warn("Process {} has {} contract links, using first one", processId, contractLinks.size());
        }

        // Use first contract link
        CommonObjectLink contractLink = contractLinks.get(0);
        String billingId = extractBillingId(contractLink.getLinkObjectType());
        int cid = contractLink.getLinkObjectId();
        String contractNumber = contractLink.getLinkObjectTitle();

        log.debug("Contract link found: billingId={}, cid={}, contractNumber={}", billingId, cid, contractNumber);

        // Determine operator with fallback
        String operator = resolveOperator(con, processId, billingId, config);
        log.debug("Resolved operator: {} (billingMapping={})", operator, config.getBillingOperatorMapping(billingId));

        // Call GetOLT API
        GetOltApiClient apiClient = new GetOltApiClient(config);
        OnuSearchResult result = apiClient.searchOnu(operator, contractNumber, cid);

        // Pass data to JSP
        form.setResponseData("searchResult", result);
        form.setResponseData("contractNumber", contractNumber);
        form.setResponseData("cid", cid);
        form.setResponseData("operator", operator);
        form.setResponseData("billingId", billingId);

        return html(conSet, form, PATH_JSP + "/onu_info.jsp");
    }

    /**
     * Extract billing ID from link object type.
     * Format: "contract:{billingId}" -> "{billingId}"
     */
    private String extractBillingId(String linkObjectType) {
        if (linkObjectType != null && linkObjectType.startsWith("contract:")) {
            return linkObjectType.substring("contract:".length());
        }
        return "default";
    }

    /**
     * Resolve operator name with fallback logic.
     * 1. Try process parameter "Operator" (param_list)
     * 2. Fallback to billing ID mapping
     * 3. Fallback to default operator
     */
    private String resolveOperator(Connection con, int processId, String billingId, Config config) {
        try {
            // 1. Try process parameter
            ParamValueDAO paramDao = new ParamValueDAO(con);
            Set<Integer> operatorValues = paramDao.getParamList(processId, config.getOperatorParamId());

            if (!operatorValues.isEmpty()) {
                int valueId = operatorValues.iterator().next();
                String operator = config.getParamOperatorMapping(valueId);
                if (operator != null && !operator.isEmpty()) {
                    log.debug("Operator from process param: valueId={}, operator={}", valueId, operator);
                    return operator;
                }
            }
        } catch (Exception e) {
            log.debug("Error reading operator param: {}", e.getMessage());
        }

        // 2. Fallback to billing ID mapping
        String operator = config.getBillingOperatorMapping(billingId);
        if (operator != null && !operator.isEmpty()) {
            log.debug("Operator from billingId {}: {}", billingId, operator);
            return operator;
        }

        // 3. Fallback to default
        log.debug("Using default operator: {}", config.getDefaultOperator());
        return config.getDefaultOperator();
    }

    /**
     * Start OLT port refresh.
     * Requires permission: /user/plugin/getolt/getolt:refreshPort
     */
    public ActionForward refreshPort(DynActionForm form, ConnectionSet conSet) throws Exception {
        String oltIp = form.getParam("oltIp");
        int portNumber = form.getParamInt("portNumber", 0);
        int processId = form.getParamInt("processId", 0);

        log.info("RefreshPort called: oltIp={}, portNumber={}, processId={}", oltIp, portNumber, processId);

        Map<String, Object> response = new HashMap<>();

        if (oltIp == null || oltIp.isEmpty()) {
            response.put("success", false);
            response.put("message", "Не указан IP OLT");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        if (portNumber <= 0) {
            response.put("success", false);
            response.put("message", "Не указан номер порта");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        Config config = Setup.getSetup().getConfig(Config.class);
        if (config == null || !config.isConfigured()) {
            response.put("success", false);
            response.put("message", "GetOLT не настроен");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        GetOltApiClient apiClient = new GetOltApiClient(config);
        RefreshResult result = apiClient.refreshPort(oltIp, portNumber);

        response.put("success", result.isSuccess());
        if (result.isSuccess()) {
            response.put("operationId", result.getOperationId());
            response.put("status", result.getStatus());
            response.put("message", result.getMessage());

            // Create note in process
            if (processId > 0) {
                createNote(conSet.getConnection(), form, processId,
                    "Обновлены данные порта OLT\nOLT: " + oltIp + "\nПорт: " + portNumber);
            }
        } else {
            response.put("message", result.getErrorMessage());
        }

        form.setResponseData("data", response);
        return json(conSet, form);
    }

    /**
     * Reboot ONU.
     * Requires permission: /user/plugin/getolt/getolt:rebootOnu
     */
    public ActionForward rebootOnu(DynActionForm form, ConnectionSet conSet) throws Exception {
        int oltId = form.getParamInt("oltId", 0);
        int port = form.getParamInt("port", 0);
        int onuId = form.getParamInt("onuId", 0);
        int processId = form.getParamInt("processId", 0);
        String mac = form.getParam("mac", "");

        log.info("RebootOnu called: oltId={}, port={}, onuId={}, processId={}", oltId, port, onuId, processId);

        Map<String, Object> response = new HashMap<>();

        if (oltId <= 0) {
            response.put("success", false);
            response.put("message", "Не указан ID OLT");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        if (port <= 0) {
            response.put("success", false);
            response.put("message", "Не указан номер порта");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        if (onuId <= 0) {
            response.put("success", false);
            response.put("message", "Не указан ID ONU");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        Config config = Setup.getSetup().getConfig(Config.class);
        if (config == null || !config.isConfigured()) {
            response.put("success", false);
            response.put("message", "GetOLT не настроен");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        GetOltApiClient apiClient = new GetOltApiClient(config);
        RefreshResult result = apiClient.rebootOnu(oltId, port, onuId);

        response.put("success", result.isSuccess());
        if (result.isSuccess()) {
            response.put("message", result.getMessage());

            // Create note in process
            if (processId > 0) {
                StringBuilder noteText = new StringBuilder();
                noteText.append("Выполнена перезагрузка ONU\n");
                noteText.append("Порт/ONU ID: ").append(port).append("/").append(onuId);
                if (!mac.isEmpty()) {
                    noteText.append("\nMAC: ").append(mac);
                }
                createNote(conSet.getConnection(), form, processId, noteText.toString());
            }
        } else {
            response.put("message", result.getErrorMessage());
        }

        form.setResponseData("data", response);
        return json(conSet, form);
    }

    /**
     * Activate/replace ONU - update MAC address in billing.
     * Requires permission: /user/plugin/getolt/getolt:activateOnu
     */
    public ActionForward activateOnu(DynActionForm form, ConnectionSet conSet) throws Exception {
        int processId = form.getParamInt("processId", 0);
        String newMac = form.getParam("newMac", "");
        int serviceId = form.getParamInt("serviceId", 0);  // Optional, for multi-service selection

        log.info("ActivateOnu called: processId={}, newMac={}, serviceId={}", processId, newMac, serviceId);

        Map<String, Object> response = new HashMap<>();

        // Validate processId
        if (processId <= 0) {
            response.put("success", false);
            response.put("message", "Не указан ID процесса");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        // Validate MAC
        if (newMac == null || newMac.isEmpty()) {
            response.put("success", false);
            response.put("message", "Не указан MAC-адрес");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        // Normalize and validate MAC format
        String normalizedMac = normalizeMac(newMac);
        if (!isValidMac(normalizedMac)) {
            response.put("success", false);
            response.put("message", "Неверный формат MAC-адреса. Ожидается: XX:XX:XX:XX:XX:XX");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        Connection con = conSet.getConnection();
        Config config = Setup.getSetup().getConfig(Config.class);

        if (config == null || !config.isConfigured()) {
            response.put("success", false);
            response.put("message", "GetOLT не настроен");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        // Get contract links from process
        ProcessLinkDAO linkDao = new ProcessLinkDAO(con);
        List<CommonObjectLink> contractLinks = linkDao.getObjectLinksWithType(processId, "contract:%");

        if (contractLinks.isEmpty()) {
            response.put("success", false);
            response.put("message", "Нет привязанных договоров");
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        // Use first contract link
        CommonObjectLink contractLink = contractLinks.get(0);
        String billingId = extractBillingId(contractLink.getLinkObjectType());
        int cid = contractLink.getLinkObjectId();
        String contractNumber = contractLink.getLinkObjectTitle();

        log.debug("Contract for activation: billingId={}, cid={}, contractNumber={}", billingId, cid, contractNumber);

        // Determine operator
        String operator = resolveOperator(con, processId, billingId, config);
        log.debug("Operator for activation: {}", operator);

        // Check InetMac config for operator
        BillingConfig billingConfig = config.getInetMacConfig(operator);
        if (billingConfig == null || !billingConfig.isConfigured()) {
            response.put("success", false);
            response.put("message", "InetMac API не настроен для оператора: " + operator);
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        // Call InetMac API
        InetMacApiClient inetMacClient = new InetMacApiClient(billingConfig);
        MacUpdateResult updateResult;

        if (serviceId > 0) {
            // Specific service selected (from multi-service dialog)
            updateResult = inetMacClient.updateMacAddress(serviceId, normalizedMac);
        } else {
            // Auto-select or return services for selection
            updateResult = inetMacClient.updateMacByContractId(cid, normalizedMac);
        }

        log.info("MAC update result: {}", updateResult);

        if (updateResult.isMultipleServices()) {
            // Return services for user selection
            response.put("success", false);
            response.put("status", "MULTIPLE_SERVICES");
            response.put("message", updateResult.getMessage());
            response.put("services", convertServicesToMap(updateResult.getServices()));
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        if (!updateResult.isSuccess()) {
            response.put("success", false);
            response.put("message", updateResult.getMessage());
            form.setResponseData("data", response);
            return json(conSet, form);
        }

        // MAC updated successfully - try to drop session
        SessionDropResult dropResult = null;
        if (updateResult.getServiceId() > 0) {
            dropResult = inetMacClient.dropSession(updateResult.getServiceId());
            log.info("Session drop result: {}", dropResult);
        }

        // Create note in process
        StringBuilder noteText = new StringBuilder();
        noteText.append("Активация/замена ONU\n");
        noteText.append("Договор: ").append(contractNumber).append("\n");
        noteText.append("Старый MAC: ").append(updateResult.getOldMac() != null ? updateResult.getOldMac() : "-").append("\n");
        noteText.append("Новый MAC: ").append(normalizedMac);
        if (dropResult != null) {
            noteText.append("\nСессия: ").append(dropResult.isSuccess() ? "сброшена" : "не удалось сбросить");
        }
        createNote(con, form, processId, noteText.toString());

        // Success response
        response.put("success", true);
        response.put("message", "MAC-адрес успешно обновлён");
        response.put("oldMac", updateResult.getOldMac());
        response.put("newMac", normalizedMac);
        response.put("serviceId", updateResult.getServiceId());
        if (dropResult != null) {
            response.put("sessionDropped", dropResult.isSuccess());
            response.put("sessionDropMessage", dropResult.getMessage());
        }

        form.setResponseData("data", response);
        return json(conSet, form);
    }

    /**
     * Validate MAC address format.
     * Expected format: XX:XX:XX:XX:XX:XX (after normalization)
     */
    private boolean isValidMac(String mac) {
        if (mac == null || mac.isEmpty()) {
            return false;
        }
        // Regex for XX:XX:XX:XX:XX:XX format (hex digits only)
        return mac.matches("^([0-9A-F]{2}:){5}[0-9A-F]{2}$");
    }

    /**
     * Normalize MAC address to XX:XX:XX:XX:XX:XX format (uppercase, colon-separated).
     */
    private String normalizeMac(String mac) {
        if (mac == null || mac.isEmpty()) {
            return "";
        }
        // Remove all separators and convert to uppercase
        String clean = mac.replaceAll("[:\\-\\s\\.]", "").toUpperCase();

        // Validate length (should be 12 hex digits)
        if (clean.length() != 12 || !clean.matches("^[0-9A-F]{12}$")) {
            return mac.toUpperCase(); // Return as-is for validation to fail
        }

        // Format as XX:XX:XX:XX:XX:XX
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i += 2) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(clean.substring(i, i + 2));
        }
        return sb.toString();
    }

    /**
     * Convert InetService list to Map list for JSON response.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> convertServicesToMap(List<InetService> services) {
        return MAPPER.convertValue(services, List.class);
    }

    /**
     * Create a note (message) in process.
     */
    private void createNote(Connection con, DynActionForm form, int processId, String text) {
        try {
            Setup setup = Setup.getSetup();
            MessageTypeConfig typeConfig = setup.getConfig(MessageTypeConfig.class);
            MessageTypeNote noteType = typeConfig.getMessageType(MessageTypeNote.class);

            if (noteType != null) {
                Message message = new Message();
                message.setTypeId(noteType.getId());
                message.setProcessId(processId);
                message.setSubject("GetOLT");
                message.setText(text);
                message.setFromTime(new Date());
                message.setUserId(form.getUserId());
                message.setToTime(new Date());
                message.setFrom("");
                message.setTo("");

                new MessageDAO(con).updateMessage(message);
                log.info("Created note in process {}: {}", processId, text.replace("\n", " "));
            } else {
                log.warn("MessageTypeNote not configured, cannot create note");
            }
        } catch (Exception e) {
            log.error("Failed to create note in process {}: {}", processId, e.getMessage());
        }
    }
}
