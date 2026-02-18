package org.bgerp.plugin.bgb.getolt.dao;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.bgerp.plugin.bgb.getolt.model.BillingConfig;
import org.bgerp.plugin.bgb.getolt.model.InetService;
import org.bgerp.plugin.bgb.getolt.model.MacUpdateResult;
import org.bgerp.plugin.bgb.getolt.model.SessionDropResult;
import org.bgerp.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * HTTP client for BGBilling InetMac WebService API.
 * Handles MAC address updates and session drops via JSON-RPC.
 *
 * Uses shared HttpClient with connection pooling and static ObjectMapper.
 */
public class InetMacApiClient {
    private static final Log log = Log.getLog();

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final CloseableHttpClient HTTP_CLIENT;
    static {
        var connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(20);
        connManager.setDefaultMaxPerRoute(10);
        HTTP_CLIENT = HttpClients.custom()
            .setConnectionManager(connManager)
            .build();
    }

    private final BillingConfig config;
    private final RequestConfig requestConfig;

    public InetMacApiClient(BillingConfig config) {
        this.config = config;
        this.requestConfig = RequestConfig.custom()
            .setConnectTimeout(config.getTimeout())
            .setSocketTimeout(config.getTimeout())
            .setConnectionRequestTimeout(config.getTimeout())
            .build();
    }

    /**
     * Get all internet services for a contract.
     * @param contractId contract ID (cid)
     * @return list of services
     */
    public List<InetService> getServicesByContractId(int contractId) {
        if (!config.isConfigured()) {
            log.warn("InetMac API is not configured");
            return new ArrayList<>();
        }

        try {
            ObjectNode request = MAPPER.createObjectNode();
            request.put("method", "getServicesByContractId");
            ObjectNode params = request.putObject("params");
            params.put("contractId", contractId);

            String responseBody = executeRequest(request);
            if (responseBody == null) {
                return new ArrayList<>();
            }

            log.debug("InetMac getServices response: {}", responseBody);

            JsonNode root = MAPPER.readTree(responseBody);

            String status = root.path("status").asText("");
            if ("error".equals(status)) {
                String errorMsg = root.path("message").asText("Unknown error");
                log.error("InetMac API error: {}", errorMsg);
                return new ArrayList<>();
            }

            JsonNode dataNode = root.path("data").path("return");

            List<InetService> services = new ArrayList<>();
            if (dataNode.isArray()) {
                services = MAPPER.readValue(dataNode.traverse(), new TypeReference<List<InetService>>() {});
            }

            log.debug("InetMac: Found {} services for contractId={}", services.size(), contractId);
            return services;

        } catch (Exception e) {
            log.error("InetMac getServicesByContractId error", e);
            return new ArrayList<>();
        }
    }

    /**
     * Update MAC address for contract (auto-selects service if only one exists).
     * @param contractId contract ID (cid)
     * @param newMac new MAC address
     * @return update result
     */
    public MacUpdateResult updateMacByContractId(int contractId, String newMac) {
        if (!config.isConfigured()) {
            return MacUpdateResult.error("InetMac API не настроен");
        }

        try {
            List<InetService> services = getServicesByContractId(contractId);

            if (services.isEmpty()) {
                return MacUpdateResult.noServices();
            }

            if (services.size() > 1) {
                return MacUpdateResult.multipleServices(services);
            }

            InetService service = services.get(0);
            return updateMacAddress(service.getServiceId(), newMac, service.getMac());

        } catch (Exception e) {
            log.error("InetMac updateMacByContractId error", e);
            return MacUpdateResult.error("Ошибка обновления MAC: " + e.getMessage());
        }
    }

    /**
     * Update MAC address for specific service.
     * @param serviceId service ID
     * @param newMac new MAC address
     * @return update result
     */
    public MacUpdateResult updateMacAddress(int serviceId, String newMac) {
        return updateMacAddress(serviceId, newMac, null);
    }

    /**
     * Update MAC address for specific service.
     * @param serviceId service ID
     * @param newMac new MAC address
     * @param oldMac previous MAC address (optional, for logging)
     * @return update result
     */
    public MacUpdateResult updateMacAddress(int serviceId, String newMac, String oldMac) {
        if (!config.isConfigured()) {
            return MacUpdateResult.error("InetMac API не настроен");
        }

        try {
            ObjectNode request = MAPPER.createObjectNode();
            request.put("method", "updateMacAddress");
            ObjectNode params = request.putObject("params");
            params.put("serviceId", serviceId);
            params.put("newMac", newMac);

            String responseBody = executeRequest(request);
            if (responseBody == null) {
                return MacUpdateResult.error("Нет ответа от сервера");
            }

            log.info("InetMac updateMacAddress response: {}", responseBody);

            JsonNode root = MAPPER.readTree(responseBody);

            String status = root.path("status").asText("");
            if ("error".equals(status)) {
                String errorMsg = root.path("message").asText("Неизвестная ошибка");
                log.error("InetMac updateMacAddress error: {}", errorMsg);
                return MacUpdateResult.error(errorMsg);
            }

            JsonNode returnNode = root.path("data").path("return");
            String innerStatus = returnNode.path("status").asText("");
            if ("error".equals(innerStatus)) {
                String errorMsg = returnNode.path("message").asText("Не удалось обновить MAC");
                log.error("InetMac updateMacAddress operation error: {}", errorMsg);
                return MacUpdateResult.error(errorMsg);
            }

            if ("ok".equals(status)) {
                log.info("InetMac: MAC updated for serviceId={}, old={}, new={}",
                    serviceId, oldMac, newMac);
                return MacUpdateResult.success(serviceId, oldMac, newMac);
            }

            String message = root.path("message").asText("Не удалось обновить MAC");
            return MacUpdateResult.error(message);

        } catch (Exception e) {
            log.error("InetMac updateMacAddress error", e);
            return MacUpdateResult.error("Ошибка обновления MAC: " + e.getMessage());
        }
    }

    /**
     * Drop session for service.
     * @param serviceId service ID
     * @return drop result
     */
    public SessionDropResult dropSession(int serviceId) {
        if (!config.isConfigured()) {
            return SessionDropResult.error("InetMac API не настроен");
        }

        try {
            ObjectNode request = MAPPER.createObjectNode();
            request.put("method", "dropSession");
            ObjectNode params = request.putObject("params");
            params.put("serviceId", serviceId);

            String responseBody = executeRequest(request);
            if (responseBody == null) {
                return SessionDropResult.error("Нет ответа от сервера");
            }

            log.info("InetMac dropSession response: {}", responseBody);

            JsonNode root = MAPPER.readTree(responseBody);

            String status = root.path("status").asText("");
            if ("error".equals(status)) {
                String errorMsg = root.path("message").asText("Неизвестная ошибка");
                log.error("InetMac dropSession error: {}", errorMsg);
                return SessionDropResult.error(errorMsg);
            }

            JsonNode dataNode = root.path("data");
            int dropped = dataNode.path("droppedSessions").asInt(dataNode.path("return").asInt(0));

            log.info("InetMac: Session dropped for serviceId={}, count={}", serviceId, dropped);
            return SessionDropResult.success(dropped);

        } catch (Exception e) {
            log.error("InetMac dropSession error", e);
            return SessionDropResult.error("Ошибка сброса сессии: " + e.getMessage());
        }
    }

    /**
     * Execute HTTP request to billing API.
     */
    private String executeRequest(ObjectNode requestBody) {
        try {
            String url = config.getUrl();

            ObjectNode userNode = requestBody.putObject("user");
            userNode.put("user", config.getUser());
            userNode.put("pswd", config.getPassword());

            log.debug("InetMac API request: POST {} method={}", url, requestBody.get("method"));

            var request = new HttpPost(url);
            request.setConfig(requestConfig);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

            HttpResponse response = HTTP_CLIENT.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            log.debug("InetMac API response: status={}, bodyLen={}", statusCode, responseBody.length());

            if (statusCode >= 400) {
                log.error("InetMac API error: status={}, body={}", statusCode, responseBody);
                return null;
            }

            return responseBody;

        } catch (java.net.SocketTimeoutException e) {
            log.error("InetMac API timeout: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("InetMac API error", e);
            return null;
        }
    }
}
