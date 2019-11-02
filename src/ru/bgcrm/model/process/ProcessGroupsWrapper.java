package ru.bgcrm.model.process;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Враппер, позволяющий обращаться к набору объектов ProcessGroup как к набору Integer,
 * при этом все группы воспринимаются в нулевой роли.
 */
class ProcessGroupsWrapper implements Set<Integer> {
    private Set<ProcessGroup> processGroups;

    protected ProcessGroupsWrapper(Set<ProcessGroup> processGroups) {
        this.processGroups = processGroups;
    }

    @Override
    public int size() {
        return processGroups.size();
    }

    @Override
    public boolean isEmpty() {
        return processGroups.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return processGroups.contains(new ProcessGroup((Integer) o, 0));
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private Iterator<ProcessGroup> processGroupsIterator = processGroups.iterator();

            @Override
            public boolean hasNext() {
                return processGroupsIterator.hasNext();
            }

            @Override
            public Integer next() {
                return processGroupsIterator.next().getGroupId();
            }

            @Override
            public void remove() {
                processGroupsIterator.remove();
            }
        };
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Integer e) {
        return processGroups.add(new ProcessGroup(e, 0));
    }

    @Override
    public boolean remove(Object o) {
        return processGroups.remove(new ProcessGroup((Integer) o, 0));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        boolean result = true;

        for (int groupId : c) {
            result = processGroups.add(new ProcessGroup(groupId, 0)) || result;
        }

        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Set<ProcessGroup> forRemove = new HashSet<ProcessGroup>();

        for (ProcessGroup procGroup : processGroups) {
            if (c.contains(procGroup.getGroupId())) {
                forRemove.add(procGroup);
            }
        }

        return processGroups.removeAll(forRemove);
    }

    @Override
    public void clear() {
        processGroups.clear();
    }
}