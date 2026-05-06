package org.bgerp.plugin.msg.max;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Log;

/**
 * MAX messenger bot.
 * Sends messages via REST API: POST {apiUrl}/messages?chat_id=XXX
 * Authorization via header: Authorization: TOKEN
 * Long polling: GET {apiUrl}/updates?marker=N&amp;timeout=60
 */
public class Bot {
    private static final Log log = Log.getLog();

    private static Bot instance;

    private final Config config;
    private final HttpClient httpClient;
    private volatile boolean running;
    private Thread pollingThread;

    Bot() {
        this.config = null;
        this.httpClient = null;
    }

    private Bot(Config config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static Bot getInstance() {
        if (instance == null)
            reinit();

        if (instance == null)
            log.info("In config server not enable bot (max:botStart)");

        return instance;
    }

    private static synchronized void reinit() {
        Config config = Setup.getSetup().getConfig(Config.class);

        if (!config.isBotStart()) {
            log.info("MAX bot start disabled (max:botStart=0)");
            return;
        }

        if (config.getToken() == null || config.getToken().isBlank()) {
            log.warn("MAX bot token not configured");
            return;
        }

        try {
            instance = new Bot(config);
            instance.startPolling();
            log.info("MAX bot initialized, API: {}", config.getApiUrl());
        } catch (Exception e) {
            log.error("Error init MAX bot: {}", e.getMessage(), e);
        }
    }

    /**
     * Send a text message to a chat.
     * @return true if sent successfully
     */
    boolean sendMessage(String chatId, String text) {
        return sendMessage(chatId, text, null);
    }

    /**
     * Send a text message to a chat with formatting.
     * @return true if sent successfully
     */
    boolean sendMessage(String chatId, String text, String parseMode) {
        try {
            String url = config.getApiUrl() + "/messages?chat_id=" + chatId;

            String jsonBody;
            if (parseMode != null && !parseMode.isBlank()) {
                jsonBody = "{\"text\":" + jsonEscape(text) + ",\"format\":\"" + parseMode + "\"}";
            } else {
                jsonBody = "{\"text\":" + jsonEscape(text) + "}";
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", config.getToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body().contains("\"message\"")) {
                log.debug("MAX message sent to '{}'", chatId);
                return true;
            } else {
                log.error("MAX send failed: status={}, body={}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("MAX send error to '{}': {}", chatId, e.getMessage(), e);
            return false;
        }
    }

    private void startPolling() {
        running = true;
        pollingThread = new Thread(this::pollLoop, "max-bot-polling");
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    private void pollLoop() {
        long marker = 0;

        while (running) {
            try {
                String url = config.getApiUrl() + "/updates?marker=" + marker + "&timeout=60";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", config.getToken())
                        .GET()
                        .timeout(Duration.ofSeconds(90))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String body = response.body();
                    if (body.contains("\"updates\":[") && !body.contains("\"updates\":[]")) {
                        log.debug("MAX updates received: {}", body);
                    }
                    marker = extractMarker(body, marker);
                }
            } catch (java.net.http.HttpTimeoutException e) {
                log.trace("MAX polling timeout, continuing");
            } catch (Exception e) {
                if (running) {
                    log.error("MAX polling error: {}", e.getMessage());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private long extractMarker(String json, long current) {
        try {
            int idx = json.lastIndexOf("\"marker\":");
            if (idx >= 0) {
                int start = idx + "\"marker\":".length();
                int end = start;
                while (end < json.length() && Character.isDigit(json.charAt(end))) {
                    end++;
                }
                if (end > start) {
                    return Long.parseLong(json.substring(start, end));
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse marker: {}", e.getMessage());
        }
        return current;
    }

    public void stop() {
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }

    private static String jsonEscape(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}
