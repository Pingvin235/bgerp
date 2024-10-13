package org.bgerp.app.servlet.jsp;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.model.base.iface.Comment;
import org.bgerp.model.base.iface.IdTitle;

import com.fasterxml.jackson.core.JsonProcessingException;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.TimeUtils;

/**
 * Functions called from ui JSP taglib.
 *
 * @author Shamil Vakhitov
 */
public class UiFunction {
    public static final UiFunction INSTANCE = new UiFunction();

    public static final String selectSingleSourceJson(Collection<IdTitle<?>> list, Set<?> availableIdSet, List<?> availableIdList,
            Map<Integer, IdTitle<?>> map, boolean showId, boolean showComment) throws JsonProcessingException {
        List<Map<String, String>> result = null;

        Function<IdTitle<?>, Map<String, String>> itemMap = item -> {
            String title = item.getTitle();

            if (showId)
                title += " (" + item.getId() + ")";
            if (showComment)
                title += " (" + UtilFunction.quotEscape(((Comment) item).getComment()) + ")";

            return Map.of("id", String.valueOf(item.getId()), "value", title);
        };

        Predicate<IdTitle<?>> filterAvailableIdSet = item -> {
            if (CollectionUtils.isEmpty(availableIdSet))
                return true;
            return availableIdSet.contains(item.getId());
        };

        if (CollectionUtils.isEmpty(availableIdList))
            result = list.stream()
                .filter(filterAvailableIdSet)
                .map(itemMap)
                .collect(Collectors.toList());
        else
            result = availableIdList.stream()
                .map(id -> map.get(id))
                .filter(item -> item != null)
                .map(itemMap)
                .collect(Collectors.toList());

        return BaseAction.MAPPER.writeValueAsString(result);
    }

    public static final String processCreatedAndClosed(Localizer l, Process process) {
        var result = new StringBuilder(100)
            .append(l.l("Created")).append(": ").append(TimeUtils.format(process.getCreateTime(), TimeUtils.FORMAT_TYPE_YMDHMS));
        if (process.getCloseTime() != null)
            result.append(" ").append(l.l("Closed")).append(": ").append(TimeUtils.format(process.getCloseTime(), TimeUtils.FORMAT_TYPE_YMDHMS));
        return result.toString();
    }
}
