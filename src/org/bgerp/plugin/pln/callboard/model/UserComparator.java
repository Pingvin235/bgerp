package org.bgerp.plugin.pln.callboard.model;

import java.util.Comparator;
import java.util.Map;

import org.bgerp.cache.UserCache;

/*
 * Sorts users in a group by full name if no sort order is specified
 * If a sort order is specified, sorts according to it
 */
public class UserComparator implements Comparator<Integer> {
    private Map<Integer, Integer> orderMap;

    public UserComparator() {
    }

    public UserComparator(Map<Integer, Integer> orderMap) {
        this.orderMap = orderMap;
    }

    @Override
    public int compare(Integer u1, Integer u2) {
        if (orderMap != null && orderMap.containsKey(u1) && orderMap.containsKey(u2)) {
            return orderMap.get(u1) - orderMap.get(u2);
        }

        return UserCache.getUser(u1).getTitle().compareTo(UserCache.getUser(u2).getTitle());
    }
}