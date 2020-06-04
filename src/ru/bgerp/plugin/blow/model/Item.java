 package ru.bgerp.plugin.blow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;

/**
 * Оболочка процесса для отображения на доске.
 *
 * @author Shamil
 */
public class Item {
    private final Board board;
    private final Process process;
    /** Столбцы, выбранные в очереди процессов. */
    private final Map<String, Object> params;
    /** Фильтры, в которые попадает ячейка. */
    private final Set<Integer> filterIds = new HashSet<>();

    private Integer order;

    // подпроцессы
    private Item parent;
    private boolean childrenSorted = false;
    private final List<Item> children = new ArrayList<>();

    // коды исполнителей (подразделения либо люди)
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

    public int getOrder() {
        if (order != null)
            return order;

        order = 0;
        if (process != null) {
            order = process.getStatus().getPos() * 10000 + process.getPriority();
            // taken the maximum order from child
            for (var child : children) {
                if (child.getOrder() > order)
                    order = child.getOrder();
            }
        }

        return order;
    }

    public List<Item> getChildren() {
        synchronized (children) {
            if (!childrenSorted) {
                // status pos desc, priority desc sorting are already done in process queue
                Collections.sort(children, (i1, i2) -> {
                    // if single executor is defined - than before
                    if (i1.getExecutorId() > 0 && i2.getExecutorId() == 0)
                        return -1;
                    if (i1.getExecutorId() == 0 && i2.getExecutorId() > 0)
                        return 1;
                    // if has children - than before
                    if (!i1.children.isEmpty() && i2.children.isEmpty())
                        return -1;
                    if (i1.children.isEmpty() && !i2.children.isEmpty())
                        return 1;
                    return i2.getOrder() - i1.getOrder();
                });
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
                if (board.isUserMode())
                    executorIds.addAll(process.getProcessExecutorsWithRole(0).stream().map(pe -> pe.getUserId()).collect(Collectors.toList()));
                else
                    executorIds.addAll(process.getProcessGroupWithRole(0).stream().map(pg -> pg.getGroupId()).collect(Collectors.toList()));
            }
        }
        return executorIds;
    }

}
