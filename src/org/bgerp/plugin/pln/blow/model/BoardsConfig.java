package org.bgerp.plugin.pln.blow.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.cache.ProcessQueueCache;
import org.bgerp.plugin.pln.blow.Plugin;

import ru.bgcrm.model.process.queue.Queue;
import ru.bgcrm.model.user.User;

public class BoardsConfig extends org.bgerp.app.cfg.Config {
    private SortedMap<Integer, BoardConfig> boardMap = new TreeMap<>();

    protected BoardsConfig(ConfigMap config, boolean validate) {
        super(null, validate);
        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed(Plugin.ID + ":board.").entrySet()) {
            BoardConfig b = new BoardConfig(me.getKey(), me.getValue());
            boardMap.put(b.getId(), b);
        }
    }

    public Collection<BoardConfig> getBoards(User user) {
        Set<Integer> queueIds = ProcessQueueCache.getUserQueueList(user)
            .stream()
            .map(Queue::getId)
            .collect(Collectors.toSet());

        return boardMap.values()
            .stream()
            .filter(board -> queueIds.contains(board.getQueue().getId()))
            .collect(Collectors.toList());
    }

    public BoardConfig getBoard(int id) {
        return boardMap.get(id);
    }

    public List<BoardConfig> getOpenBoards() {
        return boardMap.values().stream()
                .filter(b -> StringUtils.isNotBlank(b.getOpenUrl()))
                .collect(Collectors.toList());
    }

}