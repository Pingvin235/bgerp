package org.bgerp.plugin.bgb.getolt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.bgb.getolt.model.BillingConfig;
import org.bgerp.util.Log;

/**
 * Configuration for GetOLT plugin.
 * Reads settings from bgerp.properties with prefix "getolt:".
 */
public class Config extends org.bgerp.app.cfg.Config {
    private static final Log log = Log.getLog();

    private static final String CONFIG_API_URL = "api.url";
    private static final String CONFIG_API_KEY = "api.key";
    private static final String CONFIG_API_TIMEOUT = "api.timeout";
    private static final String CONFIG_OPERATOR_PARAM_ID = "operator.paramId";
    private static final String CONFIG_OPERATOR_DEFAULT = "operator.default";
    private static final String CONFIG_OPERATOR_MAPPING_PREFIX = "operator.mapping.";
    private static final String CONFIG_OPERATOR_BILLING_PREFIX = "operator.billing.";
    private static final String CONFIG_INETMAC_PREFIX = "inetmac.";

    private final String apiUrl;
    private final String apiKey;
    private final int apiTimeout;
    private final int operatorParamId;
    private final String defaultOperator;
    private final Map<Integer, String> paramOperatorMapping;
    private final Map<String, String> billingOperatorMapping;
    private final Map<String, BillingConfig> inetMacConfigs;

    /**
     * Constructor
     * @param config the configuration map
     * @throws InitStopException if initialization fails
     */
    protected Config(ConfigMap config) throws InitStopException {
        super(null);

        if (config == null) {
            this.apiUrl = "";
            this.apiKey = "";
            this.apiTimeout = 5000;
            this.operatorParamId = 68;
            this.defaultOperator = "РТЦ";
            this.paramOperatorMapping = new HashMap<>();
            this.billingOperatorMapping = new HashMap<>();
            this.inetMacConfigs = new HashMap<>();
            return;
        }

        config = config.sub(Plugin.ID + ":");

        this.apiUrl = config.get(CONFIG_API_URL, "");
        this.apiKey = config.get(CONFIG_API_KEY, "");
        this.apiTimeout = config.getInt(CONFIG_API_TIMEOUT, 5000);
        this.operatorParamId = config.getInt(CONFIG_OPERATOR_PARAM_ID, 68);
        this.defaultOperator = config.get(CONFIG_OPERATOR_DEFAULT, "РТЦ");

        // Load operator mapping from process parameter values
        this.paramOperatorMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : config.sub(CONFIG_OPERATOR_MAPPING_PREFIX).entrySet()) {
            try {
                int valueId = Integer.parseInt(entry.getKey());
                paramOperatorMapping.put(valueId, entry.getValue());
            } catch (NumberFormatException e) {
                log.warn("Invalid operator mapping key: {}", entry.getKey());
            }
        }

        // Load billing ID to operator mapping
        this.billingOperatorMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : config.sub(CONFIG_OPERATOR_BILLING_PREFIX).entrySet()) {
            billingOperatorMapping.put(entry.getKey(), entry.getValue());
        }

        // Load InetMac API configs for each operator
        // Format: getolt:inetmac.{operator}.url/user/password/timeout
        this.inetMacConfigs = new HashMap<>();
        ConfigMap inetmacConfig = config.sub(CONFIG_INETMAC_PREFIX);
        Set<String> operators = extractOperatorNames(inetmacConfig);
        for (String operator : operators) {
            ConfigMap opConfig = inetmacConfig.sub(operator + ".");
            String url = opConfig.get("url", "");
            String user = opConfig.get("user", "");
            String password = opConfig.get("password", "");
            int timeout = opConfig.getInt("timeout", 10000);

            if (!url.isEmpty()) {
                BillingConfig billingCfg = new BillingConfig(url, user, password, timeout);
                inetMacConfigs.put(operator, billingCfg);
                log.debug("InetMac config for '{}': url={}, user={}, timeout={}",
                    operator, url, user, timeout);
            }
        }

        log.debug("GetOLT API URL: {}", apiUrl);
        log.debug("GetOLT API key configured: {}", !apiKey.isEmpty());
        log.debug("GetOLT API timeout: {}ms", apiTimeout);
        log.debug("Operator param ID: {}", operatorParamId);
        log.debug("Default operator: {}", defaultOperator);
        log.debug("Param operator mapping: {}", paramOperatorMapping);
        log.debug("Billing operator mapping: {}", billingOperatorMapping);
        log.debug("InetMac configs for operators: {}", inetMacConfigs.keySet());

        initWhen(!apiUrl.isEmpty() && !apiKey.isEmpty());
    }

    /**
     * Extract operator names from config keys.
     * Keys are like "Омикрон.url", "Омикрон.user" - we extract "Омикрон".
     */
    private Set<String> extractOperatorNames(ConfigMap config) {
        Set<String> operators = new java.util.HashSet<>();
        for (String key : config.keySet()) {
            int dotIndex = key.indexOf('.');
            if (dotIndex > 0) {
                operators.add(key.substring(0, dotIndex));
            }
        }
        return operators;
    }

    /**
     * Check if the plugin is configured
     * @return true if API URL and key are configured
     */
    public boolean isConfigured() {
        return !apiUrl.isEmpty() && !apiKey.isEmpty();
    }

    /**
     * Get the API URL
     * @return the API URL
     */
    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Get the API key
     * @return the API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Get the API timeout in milliseconds
     * @return the API timeout
     */
    public int getApiTimeout() {
        return apiTimeout;
    }

    /**
     * Get the operator parameter ID
     * @return the operator parameter ID
     */
    public int getOperatorParamId() {
        return operatorParamId;
    }

    /**
     * Get the default operator
     * @return the default operator
     */
    public String getDefaultOperator() {
        return defaultOperator;
    }

    /**
     * Get operator name from process parameter value ID
     * @param valueId the parameter value ID
     * @return the operator name or null if not mapped
     */
    public String getParamOperatorMapping(int valueId) {
        return paramOperatorMapping.get(valueId);
    }

    /**
     * Get operator name from billing ID
     * @param billingId the billing ID
     * @return the operator name or null if not mapped
     */
    public String getBillingOperatorMapping(String billingId) {
        return billingOperatorMapping.get(billingId);
    }

    /**
     * Get InetMac API configuration for operator.
     * @param operator operator name (e.g., "Омикрон", "РТЦ")
     * @return billing config or null if not configured
     */
    public BillingConfig getInetMacConfig(String operator) {
        return inetMacConfigs.get(operator);
    }

    /**
     * Check if InetMac is configured for operator.
     * @param operator operator name
     * @return true if InetMac API is configured for this operator
     */
    public boolean hasInetMacConfig(String operator) {
        BillingConfig cfg = inetMacConfigs.get(operator);
        return cfg != null && cfg.isConfigured();
    }
}
