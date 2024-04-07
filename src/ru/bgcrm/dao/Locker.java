package ru.bgcrm.dao;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.UserCache;
import org.bgerp.util.Log;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.event.client.LockEvent;
import ru.bgcrm.model.Lock;
import ru.bgcrm.util.sql.ConnectionSet;

//TODO: Добавить поток очистки старых блокировок.
public class Locker {
    private static final Log log = Log.getLog();

    private static Map<String, Lock> locksById = new ConcurrentHashMap<String, Lock>();
    private static Map<Integer, Set<Lock>> locksByUser = new ConcurrentHashMap<Integer, Set<Lock>>();

    public Locker() {
        EventProcessor.subscribe(new EventListener<GetPoolTasksEvent>() {
            @Override
            public void notify(GetPoolTasksEvent e, ConnectionSet conSet) throws BGException {
                Set<Lock> locks = locksByUser.get(e.getUser().getId());
                if (locks != null) {
                    for (Lock lock : locks) {
                        e.getForm().getResponse().addEvent(new LockEvent(lock));
                        lock.continueTime();
                    }
                }
            }

        }, GetPoolTasksEvent.class);
    }

    public static void addLock(Lock lock) throws BGMessageException {
        Lock existLock = locksById.get(lock.getId());
        if (existLock != null) {
            if (existLock.getUserId() != lock.getUserId()) {
                if (existLock.getToTime() < System.currentTimeMillis()) {
                    freeLock(existLock);
                } else {
                    throw new BGMessageException("Ресурс заблокирован пользователем: {}", UserCache.getUser(existLock.getUserId()).getTitle());
                }
            } else {
                log.debug("Move lock time: {}", lock.getId());

                lock.continueTime();
                return;
            }
        }

        log.debug("Add lock: {}", lock.getId());

        Set<Lock> locks = locksByUser.get(lock.getUserId());
        if (locks == null) {
            locksByUser.put(lock.getUserId(), locks = new HashSet<Lock>());
        }

        locksById.put(lock.getId(), lock);
        locks.add(lock);
    }

    public static boolean checkLock(String id) {
        boolean result = false;

        Lock lock = locksById.get(id);
        if (lock != null) {
            if (result = lock.getToTime() < System.currentTimeMillis()) {
                freeLock(lock);
            }
        }

        return result;
    }

    public static void freeLock(Lock lock) {
        log.debug("Free lock: {}", lock.getId());

        // TODO: Может проверять, чтобы освобождал тот же, что и занимает, хотя вызов из
        // addLock не так.
        locksById.remove(lock.getId());

        Set<Lock> locks = locksByUser.get(lock.getUserId());
        if (locks != null) {
            locks.remove(lock);
        }
    }
}