package org.bgerp.model.process.queue.column;

import java.util.Set;

import ru.bgcrm.util.Utils;

class ColumnLink {
    final boolean linked;
    final String linkType;
    final boolean openOnly;
    final Set<Integer> typeIds;

    ColumnLink(String value) {
        String[] tokens = value.split(":");

        String linkTypeFilter = tokens.length > 1 ? tokens[1] : "*";
        String stateFilter = tokens.length > 2 ? tokens[2] : "open";
        String typeFilter = tokens.length > 3 ? tokens[3] : "*";

        linked = value.startsWith("linked");
        linkType = linkTypeFilter.equals("*") ? null : linkTypeFilter;
        openOnly = stateFilter.equals("open");
        typeIds = typeFilter.equals("*") ? null : Utils.toIntegerSet(typeFilter);
    }
}
