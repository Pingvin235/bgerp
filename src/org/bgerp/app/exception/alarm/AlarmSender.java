package org.bgerp.app.exception.alarm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.util.Log;
import org.bgerp.util.mail.MailMsg;

import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.model.FileData;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * Sender alarms to a configured email
 *
 * @author Shamil Vakhitov
 */
public class AlarmSender extends Thread {
    private static final Log log = Log.getLog();

    private static volatile AlarmSender instance;

    public static void init(Setup setup) {
        initInternal(setup);

        EventProcessor.subscribe((e, conSet) -> {
            if (instance != null)
                instance.run = false;
            initInternal(Setup.getSetup());
        }, SetupChangedEvent.class);
    }

    private static void initInternal(Setup setup) {
        String mail = setup.get("alarm.mail");
        if (Utils.notBlankString(mail))
            instance = new AlarmSender(setup, mail);
    }

    public static void send(String key, long defaultInterval, String subject, Supplier<String> text, Exception ex, Supplier<List<FileData>> attachments) {
        final long time = System.currentTimeMillis();
        if (instance != null && instance.isSendingNeeded(key, time, defaultInterval))
            instance.send(new AlarmMessage(key, subject, text.get(), ex, attachments == null ? null : attachments.get()), time);
    }

    public static void send(String key, long defaultInterval, String subject, Supplier<String> text) {
        send(key, defaultInterval, subject, text, null, null);
    }

    // end of static

    /** running flag */
    private volatile boolean run = true;
    /** app's name */
    private final String name;
    /** email for sending alarms */
    private final String mail;
    /** minimal interval between alarms in ms. for alarm keys */
    private volatile Map<String, Long> minAlarmInterval = new HashMap<>();
    /** last adding time for alarm keys */
    private final Map<String, Long> lastAlarmTime = new ConcurrentHashMap<>();
    /** alarm queue */
    private final BlockingQueue<AlarmMessage> queue = new ArrayBlockingQueue<>(256);
    /** app health checker */
    private final Health health = new Health();

    private AlarmSender(Setup setup, String mail) {
        // TODO: Make a method for getting title key
        this.name = setup.get("title");
        this.mail = mail;

        reloadMinAlarmIntervals(setup);

        setName("alarm-sender");

        start();
    }

    private void reloadMinAlarmIntervals(ConfigMap config) {
        Map<String, Long> newMinIntervalMap = new HashMap<>();

        for (var me : config.sub("alarm.min.interval.").entrySet()) {
            String key = me.getKey();
            int value = Utils.parseInt(me.getValue());

            if (value >= 0) {
                newMinIntervalMap.put(key, (long) value * 1000);
            }
        }

        this.minAlarmInterval = newMinIntervalMap;
    }

    private boolean isSendingNeeded(String key, long time, long defaultInterval) {
        Long lastSendTime = lastAlarmTime.get(key);
        Long interval = minAlarmInterval.get(key);
        if (interval == null) {
            interval = defaultInterval;
        }

        return lastSendTime == null || (time - lastSendTime) > interval;
    }

    private void send(AlarmMessage message, long time) {
        while (!queue.offer(message))
            queue.poll();

        lastAlarmTime.put(message.getKey(), time);
    }

    @Override
    public void run() {
        log.info("Starting");

        while (run) {
            try {
                AlarmMessage message = queue.poll();
                if (message != null) {
                    StringBuilder text = new StringBuilder(200)
                        .append("Alarm Key: ")
                        .append(message.getKey())
                        .append("\n")
                        .append("First Registration: ")
                        .append(TimeUtils.format(message.getTime(), "dd.MM.yyyy HH:mm:ss"))
                        .append("\n\n")
                        .append(message.getText());

                    new MailMsg(Setup.getSetup())
                        .withAttachments(message.getAttachments())
                        .send(mail, "[" + name + "] " + message.getSubject(), text.toString());

                    log.debug("Sending '{}' to {}", message.getSubject(), mail);
                }
                // sleep if no messages in queue
                else {
                    sleep(5 * 1000);
                }

                health.check();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }
}