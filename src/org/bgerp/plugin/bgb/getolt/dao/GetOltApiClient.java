package org.bgerp.plugin.bgb.getolt.dao;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.bgerp.plugin.bgb.getolt.Config;
import org.bgerp.plugin.bgb.getolt.model.OnuApiResponse;
import org.bgerp.plugin.bgb.getolt.model.OnuData;
import org.bgerp.plugin.bgb.getolt.model.OnuSearchResult;
import org.bgerp.plugin.bgb.getolt.model.RefreshResult;
import org.bgerp.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HTTP client for GetOLT External API.
 * Calls GET /api/v1/external/onus/erp-search endpoint.
 *
 * Uses shared HttpClient with connection pooling and static ObjectMapper.
 */
public class GetOltApiClient {
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

    private final Config config;
    private final RequestConfig defaultRequestConfig;
    private final RequestConfig longRequestConfig;

    public GetOltApiClient(Config config) {
        this.config = config;
        this.defaultRequestConfig = RequestConfig.custom()
            .setConnectTimeout(config.getApiTimeout())
            .setSocketTimeout(config.getApiTimeout())
            .setConnectionRequestTimeout(config.getApiTimeout())
            .build();
        this.longRequestConfig = RequestConfig.custom()
            .setConnectTimeout(30000)
            .setSocketTimeout(30000)
            .setConnectionRequestTimeout(30000)
            .build();
    }

    /**
     * Search for ONU by contract data.
     */
    public OnuSearchResult searchOnu(String operator, String contractNumber, Integer cid) {
        if (!config.isConfigured()) {
            log.warn("GetOLT plugin is not configured");
            return OnuSearchResult.error("GetOLT не настроен");
        }

        try {
            var urlBuilder = new StringBuilder(config.getApiUrl());
            if (!urlBuilder.toString().endsWith("/")) {
                urlBuilder.append("/");
            }
            urlBuilder.append("onus/erp-search?");
            urlBuilder.append("operator=").append(URLEncoder.encode(operator, StandardCharsets.UTF_8));

            if (contractNumber != null && !contractNumber.isEmpty()) {
                urlBuilder.append("&contractNumber=").append(URLEncoder.encode(contractNumber, StandardCharsets.UTF_8));
            }
            if (cid != null) {
                urlBuilder.append("&cid=").append(cid);
            }

            String url = urlBuilder.toString();
            log.debug("GetOLT API request: {}", url);

            var request = new HttpGet(url);
            request.setConfig(defaultRequestConfig);
            request.setHeader("X-API-Key", config.getApiKey());
            request.setHeader("Accept", "application/json");

            HttpResponse response = HTTP_CLIENT.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            log.debug("GetOLT API response: status={}, bodyLen={}", statusCode, responseBody.length());

            if (statusCode == 404) {
                var result = OnuSearchResult.error("ONU не найдена для договора " +
                    (contractNumber != null ? contractNumber : String.valueOf(cid)));
                result.setContractNumber(contractNumber);
                result.setCid(cid);
                result.setOperator(operator);
                return result;
            }

            if (statusCode >= 400) {
                log.error("GetOLT API error: status={}, body={}", statusCode, responseBody);
                return OnuSearchResult.error("Ошибка сервиса GetOLT");
            }

            return parseResponse(responseBody, operator, contractNumber, cid);

        } catch (java.net.SocketTimeoutException e) {
            log.error("GetOLT API timeout: {}", e.getMessage());
            return OnuSearchResult.error("Сервис GetOLT не отвечает");
        } catch (Exception e) {
            log.error("GetOLT API error", e);
            return OnuSearchResult.error("Ошибка обработки ответа");
        }
    }

    /**
     * Parse API response JSON into OnuSearchResult using Jackson automatic deserialization.
     */
    private OnuSearchResult parseResponse(String responseBody, String operator, String contractNumber, Integer cid) {
        try {
            log.debug("Parsing GetOLT response: {}", responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);

            JsonNode root = MAPPER.readTree(responseBody);
            List<OnuData> onus = new ArrayList<>();

            if (root.isArray()) {
                onus = MAPPER.readValue(responseBody, new TypeReference<List<OnuData>>() {});
            } else if (root.isObject()) {
                JsonNode dataNode = root.get("data");

                if (dataNode != null && dataNode.isObject()) {
                    OnuApiResponse apiResponse = MAPPER.readValue(responseBody, OnuApiResponse.class);
                    if (apiResponse.getData() != null) {
                        OnuApiResponse.OnuApiData data = apiResponse.getData();
                        if (data.getTarget() != null) {
                            OnuData target = data.getTarget();
                            if (data.getPortNeighbors() != null && !data.getPortNeighbors().isEmpty()) {
                                target.setNeighbors(data.getPortNeighbors());
                            }
                            onus.add(target);
                            log.debug("Parsed target ONU: mac={}, oltIp={}, port={}",
                                target.getMac(), target.getOltIp(), target.getPort());
                        }
                    }
                } else if (dataNode != null && dataNode.isArray()) {
                    onus = MAPPER.readValue(dataNode.traverse(), new TypeReference<List<OnuData>>() {});
                } else if (dataNode == null) {
                    OnuData onu = MAPPER.readValue(responseBody, OnuData.class);
                    if (onu != null && onu.getMac() != null) {
                        onus.add(onu);
                    }
                }
            }

            log.debug("Parsed {} ONUs from response", onus.size());

            onus.sort((a, b) -> {
                if (a.getLastUpdate() == null && b.getLastUpdate() == null) return 0;
                if (a.getLastUpdate() == null) return 1;
                if (b.getLastUpdate() == null) return -1;
                return b.getLastUpdate().compareTo(a.getLastUpdate());
            });

            if (onus.isEmpty()) {
                var result = OnuSearchResult.error("ONU не найдена для договора " +
                    (contractNumber != null ? contractNumber : String.valueOf(cid)));
                result.setContractNumber(contractNumber);
                result.setCid(cid);
                result.setOperator(operator);
                return result;
            }

            var result = OnuSearchResult.success(onus);
            result.setContractNumber(contractNumber);
            result.setCid(cid);
            result.setOperator(operator);
            return result;

        } catch (Exception e) {
            log.error("Error parsing GetOLT response: {}", e.getMessage(), e);
            return OnuSearchResult.error("Ошибка обработки ответа");
        }
    }

    /**
     * Start OLT port refresh.
     */
    public RefreshResult refreshPort(String oltIp, int portNumber) {
        if (!config.isConfigured()) {
            log.warn("GetOLT plugin is not configured");
            return RefreshResult.error("GetOLT не настроен");
        }

        try {
            var urlBuilder = new StringBuilder(config.getApiUrl());
            if (!urlBuilder.toString().endsWith("/")) {
                urlBuilder.append("/");
            }
            urlBuilder.append("actions/refresh/olt/")
                .append(URLEncoder.encode(oltIp, StandardCharsets.UTF_8))
                .append("/port/")
                .append(portNumber);

            String url = urlBuilder.toString();
            log.info("GetOLT refresh: POST {} port {}", oltIp, portNumber);

            var request = new HttpPost(url);
            request.setConfig(longRequestConfig);
            request.setHeader("X-API-Key", config.getApiKey());
            request.setHeader("Accept", "application/json");

            HttpResponse response = HTTP_CLIENT.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            log.debug("GetOLT refresh response: status={}, body={}", statusCode, responseBody);

            if (statusCode >= 400) {
                log.error("GetOLT refresh error: status={}, body={}", statusCode, responseBody);
                return RefreshResult.error("Ошибка запуска обновления: " + statusCode);
            }

            JsonNode root = MAPPER.readTree(responseBody);
            boolean success = root.path("success").asBoolean(false);

            if (success) {
                JsonNode data = root.get("data");
                if (data != null) {
                    String operationId = data.path("operationId").asText(null);
                    String status = data.path("status").asText(null);
                    String message = data.path("message").asText(null);
                    return RefreshResult.success(operationId, status, message);
                }
            }

            String errorMsg = root.path("message").asText("Неизвестная ошибка");
            return RefreshResult.error(errorMsg);

        } catch (java.net.SocketTimeoutException e) {
            log.error("GetOLT refresh timeout: {}", e.getMessage());
            return RefreshResult.error("Сервис GetOLT не отвечает");
        } catch (Exception e) {
            log.error("GetOLT refresh error", e);
            return RefreshResult.error("Ошибка обработки запроса");
        }
    }

    /**
     * Reboot ONU.
     */
    public RefreshResult rebootOnu(int oltId, int port, int onuId) {
        if (!config.isConfigured()) {
            log.warn("GetOLT plugin is not configured");
            return RefreshResult.error("GetOLT не настроен");
        }

        try {
            // Build URL: /api/olt/{oltId}/onu/{port}/{onuId}/reboot
            // Need base URL without /v1/external
            String apiUrl = config.getApiUrl();
            String baseUrl = apiUrl;
            int externalIdx = apiUrl.indexOf("/v1/external");
            if (externalIdx > 0) {
                baseUrl = apiUrl.substring(0, externalIdx);
            }

            String url = baseUrl + "/olt/" + oltId + "/onu/" + port + "/" + onuId + "/reboot";
            log.info("GetOLT reboot: oltId={}, port={}, onuId={}", oltId, port, onuId);

            var request = new HttpPost(url);
            request.setConfig(longRequestConfig);
            request.setHeader("X-API-Key", config.getApiKey());
            request.setHeader("Accept", "application/json");

            HttpResponse response = HTTP_CLIENT.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            log.debug("GetOLT reboot response: status={}, body={}", statusCode, responseBody);

            if (statusCode == 429) {
                JsonNode root = MAPPER.readTree(responseBody);
                String errorMsg = root.path("error").asText("ONU недавно перезагружалась");
                int remainingSeconds = root.path("remainingSeconds").asInt(0);
                if (remainingSeconds > 0) {
                    int mins = remainingSeconds / 60;
                    int secs = remainingSeconds % 60;
                    errorMsg = "ONU недавно перезагружалась. Подождите " + mins + " мин. " + secs + " сек.";
                }
                return RefreshResult.error(errorMsg);
            }

            if (statusCode >= 400) {
                log.error("GetOLT reboot error: status={}, body={}", statusCode, responseBody);
                JsonNode root = MAPPER.readTree(responseBody);
                String errorMsg = root.path("error").asText("Ошибка перезагрузки ONU");
                return RefreshResult.error(errorMsg);
            }

            JsonNode root = MAPPER.readTree(responseBody);
            boolean success = root.path("success").asBoolean(false);

            if (success) {
                String message = root.path("message").asText("ONU перезагружена");
                return RefreshResult.success(null, "completed", message);
            }

            String errorMsg = root.path("error").asText(root.path("message").asText("Неизвестная ошибка"));
            return RefreshResult.error(errorMsg);

        } catch (java.net.SocketTimeoutException e) {
            log.error("GetOLT reboot timeout: {}", e.getMessage());
            return RefreshResult.error("Сервис GetOLT не отвечает");
        } catch (Exception e) {
            log.error("GetOLT reboot error", e);
            return RefreshResult.error("Ошибка перезагрузки ONU");
        }
    }
}
