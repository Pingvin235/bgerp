package ru.bgerp.plugin.blow.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.bgcrm.util.Utils;

public class ItemComparator implements java.util.Comparator<Item> {
    
    public static final String STATUS_POS = "status_pos";
    public static final String PRIORITY = "priority";
    public static final String HAS_EXECUTOR = "has_executor";
    public static final String HAS_CHILDREN = "has_children";
    public static final String TYPE = "type";
    
    private final Comparator<Item> comparator;

    public ItemComparator(String type, String params) {
        this.comparator = getComparator(type, params);
    }

    private Comparator<Item> getComparator(String type, String params) {
        switch (type) {
            case STATUS_POS:
                return (i1, i2) -> {
                    return i2.getProcess().getStatus().getPos() - i1.getProcess().getStatus().getPos();
                };
            case PRIORITY:
                return (i1, i2) -> {
                    return i2.getProcess().getPriority() - i1.getProcess().getPriority();
                };
            case HAS_EXECUTOR:
                return (i1, i2) -> {
                    if (i1.getExecutorId() > 0 && i2.getExecutorId() == 0)
                        return -1;
                    if (i1.getExecutorId() == 0 && i2.getExecutorId() > 0)
                        return 1;
                    return 0;
                };
            case HAS_CHILDREN:
                return (i1, i2) -> {
                    if (!i1.getChildren().isEmpty() && i2.getChildren().isEmpty())
                        return -1;
                    if (i1.getChildren().isEmpty() && !i2.getChildren().isEmpty())
                        return 1;
                    return 0;
                };
            case TYPE:
                return new TypeComparator(params);
        }
        throw new IllegalArgumentException("No comparator found for type: " + type);
    }

    @Override
    public int compare(Item o1, Item o2) {
        return comparator.compare(o1, o2);
    }

    private static class TypeComparator implements java.util.Comparator<Item> {
        private final List<Integer> typeIds;

        private TypeComparator(String params) {
            this.typeIds = Collections.unmodifiableList(Utils.toIntegerList(params));
        }

        @Override
        public int compare(Item o1, Item o2) {
            return typeIds.indexOf(o1.getProcess().getTypeId()) - typeIds.indexOf(o2.getProcess().getTypeId());
        }
    }
}
