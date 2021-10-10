package org.bgerp.itest.kernel.process;

import java.util.List;

import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.ParamTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;

@Test(groups = "processParam", dependsOnGroups = { "process", "param", "address" })
public class ProcessParamTest {

    @Test
    public void addParams() throws Exception {
        int paramAddressId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_ADDRESS, "Test address", ProcessTest.posParam += 2, "", "");
        int paramTextId =  ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, "Test text", ProcessTest.posParam += 2,
            "#"+ ParamTest.SAVE_ON_FOCUS_LOST + "\n" +
            "#" + ParamTest.ENCRYPTED + "\n",
            ""
        );
        // TODO: params 'date', 'datetime', 'list', 'listcount', 'file' with different options

        var con = DbTest.conRoot;

        var dao = new ProcessTypeDAO(con);
        var type = dao.getProcessType(ProcessTest.processTypeTestId);
        Assert.assertNotNull(type);

        type.getProperties().getParameterIds().addAll(List.of(paramAddressId, paramTextId));

        dao.updateTypeProperties(type);
        ProcessTypeCache.flush(con);

        /* paramAddressId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_ADDRESS, "Address", posParam += 2, "", "");
        // TODO: Make date chooser configuration.
        paramNextDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, "Next date", posParam += 2, "", "");
        paramDeadlineDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, "Deadline", posParam += 2, "", ""); */
    }

    @Test(dependsOnMethods = "addParams")
    public void addProcess() throws Exception {
        var p = ProcessHelper.addProcess(ProcessTest.processTypeTestId, UserTest.USER_ADMIN_ID, "Test parameters");
        Assert.assertNotNull(p);
    }
}
