package ru.bgcrm.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Log;

public class AlarmSender extends Thread {
    private static final Log log = Log.getLog();

    public static void initSender(Setup setup) {
        String name = setup.get("app.name", "UNDEF");
        String mail = setup.get("alarm.mail", null);

        if (Utils.notBlankString(mail)) {
            sender = new AlarmSender(setup, name, mail);
        }
    }

    public static boolean needAlarmSend(String key, long time, long defaultInterval) {
        return sender != null && sender.needAlarmSendNoStatic(key, time, defaultInterval);
    }

    public static void sendAlarm(AlarmErrorMessage message, long time) {
        if (sender != null) {
            sender.sendAlarmNoStatic(message, time);
        }
    }

    private static AlarmSender sender;

    // нестатическая часть
    private Setup setup;
    // имя приложения
    private String name;
    // электронный адрес для отправки сообщений
    private String mail;
    // минимальный интервал, в миллисекундах между алармами какого-то типа
    // его возможно переопределять в конфигурации
    private volatile Map<String, Long> minAlarmInterval = new HashMap<String, Long>();
    // последнее время добавление аларма какого-то типа
    private Map<String, Long> lastAlarmTime = new ConcurrentHashMap<String, Long>();
    // очередь алармов
    private BlockingQueue<AlarmErrorMessage> alarmQueue = new ArrayBlockingQueue<AlarmErrorMessage>(256);

    private float memoryCheckThreshold = 0.85f;

    // флаг, что отрабатывает старая система проверки
    private boolean oldMemoryCheck = true;

    private AlarmSender(Setup setup, String appName, String mail) {
        this.setup = setup;
        this.name = appName;
        this.mail = mail;

        reloadMinAlarmIntervals();

        start();
    }

    //TODO: Сделать автоматическую перезагрузку интервалов при изменении конфигурации.
    private void reloadMinAlarmIntervals() {
        Map<String, Long> newMinIntervalMap = new HashMap<String, Long>();

        Map<String, String> intervals = setup.getHashValuesWithPrefix("alarm.min.interval.");
        for (Map.Entry<String, String> me : intervals.entrySet()) {
            String key = me.getKey();
            int value = Utils.parseInt(me.getValue());

            if (value >= 0) {
                newMinIntervalMap.put(key, (long) value * 1000);
            }
        }

        this.minAlarmInterval = newMinIntervalMap;
    }

    // перед добавлением аларма в очередь -
    // проверка времени отправки последнего аларма такго типа
    private boolean needAlarmSendNoStatic(String key, long time, long defaultInterval) {
        Long lastSendTime = lastAlarmTime.get(key);
        Long interval = minAlarmInterval.get(key);
        if (interval == null) {
            interval = defaultInterval;
        }

        return lastSendTime == null || (time - lastSendTime) > interval;
    }

    // добавление аларма
    private void sendAlarmNoStatic(AlarmErrorMessage message, long time) {
        while (!alarmQueue.offer(message)) {
            alarmQueue.poll();
        }

        lastAlarmTime.put(message.getKey(), time);
    }

    @Override
    public void run() {
        log.info("AlarmSender started..");

        while (true) {
            try {
                AlarmErrorMessage message = alarmQueue.poll();
                if (message != null) {
                    StringBuilder text = new StringBuilder(200);
                    text.append("ID события: ");
                    text.append(message.getKey());
                    text.append("\n");
                    text.append("Время регистрации события: ");
                    text.append(TimeUtils.format(message.getRegistrationTime(), "dd.MM.yyyy HH:mm:ss"));
                    text.append("\n\n");
                    text.append(message.getText());

                    // отправка письма
                    new MailMsg(setup).sendMessage(mail, "[" + name + "] " + message.getSubject(), text.toString());
                }
                // спим только, если нет сообщений в очереди
                else {
                    sleep(5 * 1000);
                }

                if (oldMemoryCheck) {
                    checkSystemHealth();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private long lastSystemCheck = 0;
    // интервал между проверками системы
    private static final long SYSTEM_CHECK_INTERVAL = 2 * 1000;

    private void checkSystemHealth() {
        long now = System.currentTimeMillis();
        if (now - lastSystemCheck > SYSTEM_CHECK_INTERVAL) {
            Runtime r = Runtime.getRuntime();
            if ((r.maxMemory() * memoryCheckThreshold) < (r.totalMemory() - r.freeMemory())) {
                String key = "system.no.memory";
                String msg = "Приложению недостаточно выделенной для него памяти!\n" + "Зарезервировано " + r.totalMemory() + " из " + r.maxMemory()
                        + " (максимум) байтов памяти.\n" + "Сейчас свободно: " + r.freeMemory() + " байтов.\n\n"
                        + "Необходимо выделить большее количество памяти приложению.";

                if (needAlarmSendNoStatic(key, now, 30 * 1000)) {
                    sendAlarmNoStatic(new AlarmErrorMessage(key, "Недостаток памяти системы", msg), now);
                }

                //попытка освободить хоть какую-то память
                r.gc();
            }
            lastSystemCheck = now;
        }
    }
}