package ru.bgcrm.model.process;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class ProcessTest {
    @Test
    public void testExecutors() {
        var p = new Process();
        p.getExecutors().add(new ProcessExecutor(1, 2, 0));
        p.getExecutors().add(new ProcessExecutor(2, 2, 0));
        p.getExecutors().add(new ProcessExecutor(3, 2, 1));

        Assert.assertEquals(Set.of(1, 2), p.getExecutorIdsWithGroupAndRole(2, 0));
        Assert.assertEquals(Set.of(3), p.getExecutorIdsWithRole(1));
        Assert.assertEquals(Set.of(1, 2, 3), p.getExecutorIds());
    }

    @Test
    public void testGroups() {
        var p = new Process();
        p.getGroups().add(new ProcessGroup(1, 0));
        p.getGroups().add(new ProcessGroup(2, 0));
        p.getGroups().add(new ProcessGroup(3, 1));

        Assert.assertEquals(Set.of(1, 2), p.getGroupIdsWithRole(0));
        Assert.assertEquals(Set.of(1, 2, 3), p.getGroupIdsWithRoles(Set.of(0, 1)));
        Assert.assertEquals(Set.of(1, 2, 3), p.getGroupIds());
    }
}
