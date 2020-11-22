 package ru.bgerp.plugin.blow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;

/**
 * Board's cell.
 *
 * @author Shamil Vakhitov
 */
public class Item {
    private final Board board;
    private final Process process;
    /** Column values from a process queue. */
    private final Map<String, Object> params;
    /** Filters, matched to this item. */
    private final Set<Integer> filterIds = new HashSet<>();

    /** Process hierarchy. */
    private Item parent;
    private boolean childrenSorted = false;
    private final List<Item> children = new ArrayList<>();

    /** Executor IDs: users or groups. */
    private Set<Integer> executorIds;

    public Item(Board board, Pair<Process, Map<String, Object>> pair) {
        this.board = board;
        this.process = pair != null ? pair.getFirst() : null;
        this.params = pair != null ? pair.getSecond() : null;
    }

    public Process getProcess() {
        return process;
    }

    public int getProcessId() {
        return process != null ? process.getId() : 0;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Set<Integer> getFilterIds() {
        return filterIds;
    }

    public void addFilterId(int value) {
        filterIds.add(value);
    }

    public Item getParent() {
        return parent;
    }

    public int getPriority() {
        if (process == null)
            return 0;
        if (process.getPriority() > 0)
            return process.getPriority();
        return 0;
    }

    public List<Item> getChildren() {
        synchronized (children) {
            if (!childrenSorted) {
                Collections.sort(children, board.getConfig().getItemComparator());
                childrenSorted = true;
            }
        }
        return Collections.unmodifiableList(children);
    }

    public void addChild(Item item) {
        item.parent = this;
        children.add(item);
    }

    /**
     * Общий процесс - либо не закреплён за конкретным исполнителем, либо их более одного - 0.
     *
     * @return
     */
    public int getExecutorId() {
        Set<Integer> executors = getExecutorIds();
        return executors.isEmpty() || executors.size() > 1 ? 0 :
            ru.bgcrm.util.Utils.getFirst(executors);
    }

    /**
     * Возвращает наличие указанного исполнителя на данном процессе.
     *
     * @param executorId
     * @return
     */
    public boolean hasExecutor(int executorId) {
        return getExecutorIds().contains(executorId);
    }

    private Set<Integer> getExecutorIds() {
        if (executorIds == null) {
            executorIds = new HashSet<>(10);

            for (Item child : children) {
                if (child.getExecutorIds().isEmpty())
                    return executorIds = Collections.emptySet();
                executorIds.addAll(child.getExecutorIds());
            }

            if (process != null) {
                if (board.isUserMode()) {
                    var groupIds = board.getConfig().getExecutorGroupIds();
                    if (groupIds.isEmpty())
                        executorIds.addAll(process.getExecutorIdsWithRole(0));
                    else
                        executorIds.addAll(process.getExecutors().stream()
                            .filter(pe -> pe.getRoleId() == 0 && groupIds.contains(pe.getGroupId()))
                            .map(ProcessExecutor::getUserId)
                            .collect(Collectors.toList()));
                } else
                    executorIds.addAll(process.getGroupIdsWithRole(0));
            }
        }
        return executorIds;
    }

}
