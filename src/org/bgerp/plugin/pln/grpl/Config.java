package org.bgerp.plugin.pln.grpl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.pln.grpl.model.BoardConfig;

import javassist.NotFoundException;

public class Config extends org.bgerp.app.cfg.Config {
    private final Map<Integer, BoardConfig> boards;
    private final Map<Integer, BoardConfig> paramBoard;

    protected Config(ConfigMap config) {
        super(null);
        config = config.sub(Plugin.ID + ":");
        boards = loadBoards(config);
        paramBoard = boards.values().stream().collect(Collectors.toUnmodifiableMap(BoardConfig::getParamId, Function.identity()));
    }

    private Map<Integer, BoardConfig> loadBoards(ConfigMap config) {
        Map<Integer, BoardConfig> result = new LinkedHashMap<>();

        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("board.").entrySet()) {
            var board = new BoardConfig(me.getKey(), me.getValue());
            result.put(board.getId(), board);
        }

        return Collections.unmodifiableMap(result);
    }

    public Map<Integer, BoardConfig> getBoards() {
        return boards;
    }

    public BoardConfig getBoard(int paramId) {
        return paramBoard.get(paramId);
    }

    public BoardConfig getBoardOrThrow(int id) throws NotFoundException {
        var result = boards.get(id);
        if (result == null)
            throw new NotFoundException("Not found board with ID: " + id);
        return result;
    }

    /* public List<BoardConfig> getBoards(int processTypeId) {
        return boards.values().stream()
            .filter(b -> b.getProcessTypeIds().contains(processTypeId))
            .collect(Collectors.toList());
    } */
}
