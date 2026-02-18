package org.bgerp.plugin.bgb.getolt.model;

/**
 * Configuration for InetMac API connection to a specific billing.
 */
public class BillingConfig {
    private final String url;
    private final String user;
    private final String password;
    private final int timeout;

    public BillingConfig(String url, String user, String password, int timeout) {
        this.url = url != null ? url : "";
        this.user = user != null ? user : "";
        this.password = password != null ? password : "";
        this.timeout = timeout > 0 ? timeout : 10000;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getTimeout() {
        return timeout;
    }

    /**
     * Check if billing is properly configured.
     * @return true if URL, user and password are not empty
     */
    public boolean isConfigured() {
        return !url.isEmpty() && !user.isEmpty() && !password.isEmpty();
    }

    @Override
    public String toString() {
        return "BillingConfig{url='" + url + "', user='" + user + "', timeout=" + timeout + "}";
    }
}
