package ru.bgerp.plugin.blow.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.beust.jcommander.internal.Lists;

import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessLinkProcess.Made;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.model.process.Status;

public class BoardTest {
    private static final Status STATUS = new Status(0, "Test");

    public static class Process extends ru.bgcrm.model.process.Process {
        private Process(int id) {
            setId(id);
        }

        @Override
        public Status getStatus() {
            return STATUS;
        }
    }

    public static class ProcessWithExecutor extends Process {
        private ProcessWithExecutor(int processId, int... executorIds) {
            super(processId);
            Set<ProcessExecutor> executors = new HashSet<>();
            for (int executorId : executorIds)
                executors.add(new ProcessExecutor(executorId, 0, 0));
            setExecutors(executors);
        }
    }

    @Test
    public void testGetRoot() throws Exception{
        Process p1 = new Process(1);
        Process p2 = new Process(2);
        Process p3 = new Process(3);
        Process p4 = new Process(4);
        
        @SuppressWarnings("unchecked")
        List<Pair<ru.bgcrm.model.process.Process, Map<String, Object>>> processes = Lists.newArrayList(new Pair<>(p1, null), new Pair<>(p2, null), 
                new Pair<>(p3, null), new Pair<>(p4, null));
        List<CommonObjectLink> links = Lists.newArrayList(new Made(p1.getId(), p2.getId()), new Made(p1.getId(), p3.getId()));
        
        Item rootItem = new Board(new BoardConfig(0, new Preferences()), processes, links).getRoot();
        Assert.assertNotNull(rootItem);
        Assert.assertNull(rootItem.getProcess());
        
        List<Item> children = rootItem.getChildren();
        Assert.assertEquals(2, children.size());
        Assert.assertEquals(p1, children.get(0).getProcess());
        Assert.assertEquals(p4, children.get(1).getProcess());
        
        children = children.get(0).getChildren();
        Assert.assertEquals(2, children.size());
        Assert.assertEquals(p2, children.get(0).getProcess());
        Assert.assertEquals(p3, children.get(1).getProcess());
    }
    
    @Ignore
    @Test
    public void testGetQueues() {
        List<CommonObjectLink> links = new ArrayList<>();
        
        Process p1 = new ProcessWithExecutor(1, 1);
        Process p2 = new ProcessWithExecutor(2, 2);
        
        Process p3 = new ProcessWithExecutor(3, 1);
        links.add(new Made(p1, p3));
        Process p4 = new ProcessWithExecutor(4, 2);
        links.add(new Made(p2, p4));
        
        Process p5 = new ProcessWithExecutor(5, 1);
        links.add(new Made(p1, p5));
        
        Process p6 = new ProcessWithExecutor(6, 1);
        Process p7 = new ProcessWithExecutor(7, 2);
        links.add(new Made(p6, p7));
        Process p8 = new ProcessWithExecutor(8, 2);
        links.add(new Made(p6, p8));
        
        Process p9 = new ProcessWithExecutor(9, 1);
        Process p10 = new ProcessWithExecutor(10, 1);
        links.add(new Made(p9, p10));
        
        Process p11 = new ProcessWithExecutor(11, 1, 2);
        
        @SuppressWarnings("unchecked")
        List<Pair<ru.bgcrm.model.process.Process, Map<String, Object>>> processes = Lists.newArrayList(new Pair<>(p1, null), new Pair<>(p2, null), new Pair<>(p3, null),
                new Pair<>(p4, null), new Pair<>(p5, null), new Pair<>(p6, null), new Pair<>(p7, null), new Pair<>(p8, null), new Pair<>(p9, null),
                new Pair<>(p10, null), new Pair<>(p11, null));
        
        Board board = new Board(null, processes, links);
        
        Map<Integer, List<Item>> queues = board.getQueues();
        Assert.assertEquals(3, queues.keySet().size());
        Assert.assertEquals(6, board.getLastIndex());
        
        List<Item> common = queues.get(0);
        Assert.assertEquals(6, common.get(3).getProcessId());
        Assert.assertEquals(11, common.get(6).getProcessId());
        
        List<Item> ex1 = queues.get(1);
        Assert.assertEquals(5, ex1.get(2).getProcessId());
        Assert.assertEquals(10, ex1.get(5).getProcessId());
        
        List<Item> ex2 = queues.get(2);
        Assert.assertEquals(4, ex2.get(1).getProcessId());
        Assert.assertEquals(8, ex2.get(5).getProcessId());
    }

}
