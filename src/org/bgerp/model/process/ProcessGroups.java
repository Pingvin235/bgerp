package org.bgerp.model.process;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bgerp.util.Dynamic;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.util.Utils;

/**
 * Process execution groups.
 *
 * @author Shamil Vakhitov
 */
public class ProcessGroups extends TreeSet<ProcessGroup> {
    public ProcessGroups() {
        super();
    }

    public ProcessGroups(Set<ProcessGroup> groups) {
        super(groups);
    }

    public ProcessGroups(Integer... groupId) {
        super(Stream.of(groupId)
            .map(id -> new ProcessGroup(id))
            .collect(Collectors.toSet()));
    }

    /**
     * Set of concatenated IDs from group ID and role ID.
     * @return
     */
    @Dynamic
    public Set<String> getGroupRoleIds() {
        return stream().map(ProcessGroup::toGroupRolePair).collect(Collectors.toSet());
    }

    /**
     * Converts array of IDs groupId:roleId.
     * @param array
     * @return
     */
    public static ProcessGroups from(String[] array) {
        ProcessGroups result = new ProcessGroups();

        if (array == null) {
            return result;
        }

        if (array != null) {
            for (String item : array) {
                ProcessGroup processGroup = new ProcessGroup();

                if (item.indexOf(":") > -1) {
                    processGroup.setGroupId(Utils.parseInt(item.substring(0, item.indexOf(":"))));
                    processGroup.setRoleId(Utils.parseInt(item.substring(item.indexOf(":") + 1)));
                } else {
                    processGroup.setGroupId(Utils.parseInt(item));
                    processGroup.setRoleId(0);
                }

                result.add(processGroup);
            }
        }

        return result;
    }

    /**
     * Converts list of ID with titles.
     * @param list
     * @return
     */
    public static ProcessGroups from(List<IdTitle> list) {
        ProcessGroups result = new ProcessGroups();

        for (IdTitle item : list) {
            ProcessGroup processGroup = new ProcessGroup();
            processGroup.setGroupId(item.getId());
            processGroup.setRoleId(Utils.parseInt(item.getTitle()));

            result.add(processGroup);
        }

        return result;
    }
}
