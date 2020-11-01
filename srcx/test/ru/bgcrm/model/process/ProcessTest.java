package ru.bgcrm.model.process;

import com.google.common.collect.Sets;

import org.junit.Assert;
import org.junit.Test;

public class ProcessTest {
    @Test
    public void testExecutors() {
        var p = new Process();
        p.getExecutors().add(new ProcessExecutor(1, 2, 0));
        p.getExecutors().add(new ProcessExecutor(2, 2, 0));
        p.getExecutors().add(new ProcessExecutor(3, 2, 1));

        var pe = p.getProcessExecutorsInGroupWithRole(0, 2);
        Assert.assertTrue(pe.equals(Sets.newHashSet(1, 2)));

        pe = p.getProcessExecutorsWithRole(1);
        Assert.assertTrue(pe.equals(Sets.newHashSet(3)));

        pe = p.getExecutorIds();
        Assert.assertTrue(pe.equals(Sets.newHashSet(1, 2, 3)));
    }

    @Test
    public void testGroups() {
        var p = new Process();
        p.getProcessGroups().add(new ProcessGroup(1, 0));
        p.getProcessGroups().add(new ProcessGroup(2, 0));
        p.getProcessGroups().add(new ProcessGroup(3, 1));

        var pg = p.getGroupIdsWithRole(0);
        Assert.assertTrue(pg.equals(Sets.newHashSet(1, 2)));
        
        pg = p.getGroupIdsWithRoles(Sets.newHashSet(0, 1));
        Assert.assertTrue(pg.equals(Sets.newHashSet(1, 2, 3)));

        pg = p.getGroupIds();
        Assert.assertTrue(pg.equals(Sets.newHashSet(1, 2, 3)));
    }
}
