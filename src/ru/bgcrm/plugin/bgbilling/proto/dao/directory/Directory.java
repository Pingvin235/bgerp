package ru.bgcrm.plugin.bgbilling.proto.dao.directory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bgerp.model.base.Id;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;

/**
 * Cached directory
 *
 * @author Shamil Vakhitov
 */
public abstract class Directory <T extends Id> {
    protected final DBInfo dbInfo;
    protected final int moduleId;
    protected final String directoryItemClass;

    private final AtomicLong lastLoadTime = new AtomicLong();
    private final AtomicLong lastLoadVersion = new AtomicLong();

    private final Map<Integer, T> values = new ConcurrentHashMap<>();

    protected Directory(DBInfo dbInfo, int moduleId, String directoryItemClass) {
        this.dbInfo = dbInfo;
        this.moduleId = moduleId;
        this.directoryItemClass = directoryItemClass;
    }

    public T get(User user, int id) {
        loadIfNeeded(user);
        return values.computeIfAbsent(id, this::missingValue);
    }

    private void loadIfNeeded(User user) {
        if (lastLoadVersion.get() > 0 && (System.currentTimeMillis() - lastLoadTime.get() < 10000L))
            return;

        long version = new DirectoryDAO(user, dbInfo).getDirectoryVersion(directoryItemClass, moduleId);

        if (lastLoadVersion.get() == version)
            return;

        var values = list(user);
        this.values.clear();
        this.values.putAll(values.stream().collect(Collectors.toMap(T::getId, Function.identity())));

        lastLoadTime.set(System.currentTimeMillis());
        lastLoadVersion.set(version);
    }

    protected abstract List<T> list(User user);

    protected abstract T missingValue(int id);
}
