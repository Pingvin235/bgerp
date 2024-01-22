package org.bgerp.itest.kernel.process;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bgerp.dao.param.ParamLogDAO;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.AddressTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.Pageable;
import org.bgerp.util.TimeConvert;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterListCountValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

@Test(groups = "processParam", dependsOnGroups = { "process", "param", "address" })
public class ProcessParamTest {
    private static final String TITLE = "Kernel Process Param";

    public static volatile int paramAddressId;
    private int paramBlobId;
    private int paramDateId;
    private int paramDateTimeId;
    private int paramEmailId;
    private int paramFileId;
    static int paramListId;
    private int paramListDirConfigId;
    private int paramListCountId;
    private int paramMoneyId;
    private int paramTextId;
    private int paramTextRegexpId;
    private int paramTextShowAsLinkId;
    private int paramTextLongTitleId;
    private int paramPhoneId;
    private int paramTreeId;

    private int processTypeId;
    private int processId;

    @Test
    public void param() throws Exception {
        paramAddressId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_ADDRESS, TITLE + " type 'address'",
                ProcessTest.posParam += 2, "", "");

        paramBlobId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_BLOB, TITLE + " type 'blob'",
                ProcessTest.posParam += 2, "", "");

        paramDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, TITLE + " type 'date'",
                ProcessTest.posParam += 2, "", "");

        paramDateTimeId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATETIME, TITLE + " type 'datetime'",
                ProcessTest.posParam += 2, "type=ymdhms", "");

        paramEmailId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_EMAIL, TITLE + " type 'email'",
                ProcessTest.posParam += 2, ResourceHelper.getResource(this, "param.email.config.txt"), "");

        paramFileId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_FILE, TITLE + " type 'file'",
                ProcessTest.posParam += 2, ResourceHelper.getResource(this, "param.file.config.txt"), "");

        paramListId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " type 'list'",
                ProcessTest.posParam += 2,
                ResourceHelper.getResource(this, "param.list.config.txt"),
                ResourceHelper.getResource(this, "param.list.values.txt"));

        paramListDirConfigId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " type 'list' dir config",
                ProcessTest.posParam += 2,
                ResourceHelper.getResource(this, "param.list.dir.config.txt"),
                "");

        paramListCountId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LISTCOUNT, TITLE + " type 'listcount'",
                ProcessTest.posParam += 2,
                ResourceHelper.getResource(this, "param.listcount.config.txt"),
                ResourceHelper.getResource(this, "param.listcount.values.txt"));

        paramMoneyId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_MONEY, TITLE + " type 'money'",
                ProcessTest.posParam += 2, "", "");

        paramTextId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " type 'text'",
                ProcessTest.posParam += 2, ResourceHelper.getResource(this, "param.text.config.txt"), "");

        paramTextRegexpId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " type 'text' regexp",
                ProcessTest.posParam += 2, ResourceHelper.getResource(this, "param.text.regexp.config.txt"), "");

        paramTextShowAsLinkId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " type 'text' show as link",
                ProcessTest.posParam += 2, ResourceHelper.getResource(this, "param.text.show.as.link.config.txt"), "");

        paramTextLongTitleId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " type 'text with very long title long title long title long title long title long title long title long title",
                ProcessTest.posParam += 2, "", "");

        paramPhoneId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_PHONE, TITLE + " type 'phone'",
                ProcessTest.posParam += 2, "", "");

        paramTreeId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TREE, TITLE + " type 'tree'",
                ProcessTest.posParam += 2,
                ResourceHelper.getResource(this, "param.tree.config.txt"),
                ResourceHelper.getResource(this, "param.tree.values.txt"));
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(
                List.of(paramAddressId, paramBlobId, paramDateId, paramDateTimeId, paramEmailId, paramFileId, paramListId, paramListDirConfigId,
                        paramListCountId, paramMoneyId, paramTextId, paramTextShowAsLinkId, paramTextLongTitleId, paramTextRegexpId, paramPhoneId, paramTreeId));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        processId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE,
            ConfigHelper.generateConstants(
                "PARAM_ADDRESS_ID", paramAddressId,
                "PARAM_BLOB_ID", paramBlobId,
                "PARAM_DATE_ID", paramDateId,
                "PARAM_DATETIME_ID", paramDateTimeId,
                "PARAM_EMAIL_ID", paramEmailId,
                "PARAM_FILE_ID", paramFileId,
                "PARAM_LIST_ID", paramListId,
                "PARAM_LISTCOUNT_ID", paramListCountId,
                "PARAM_MONEY_ID", paramMoneyId,
                "PARAM_TEXT_ID", paramTextId,
                "PARAM_PHONE_ID", paramPhoneId,
                "PARAM_TREE_ID", paramTreeId
            ) + ResourceHelper.getResource(this, "process.queue.config.txt"),
            Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "process")
    public void paramValues() throws Exception {
       paramValues(processId);
    }

    private void paramValues(int processId) throws Exception {
        paramValueAddress(processId);
        paramValueBlob(processId);
        paramValueDate(processId);
        paramValueDateTime(processId);
        paramValueEmail(processId);
        paramValueFile(processId);
        paramValueList(processId);
        paramValueListCount(processId);
        paramValueMoney(processId);
        paramValueText(processId);
        paramValuePhone(processId);
        paramValueTree(processId);
    }

    private void paramValueAddress(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        dao.updateParamAddress(processId, paramAddressId, 0, new ParameterAddressValue().withHouseId(AddressTest.houseUfa6.getId()).withFlat("33"));
        var value = dao.getParamAddress(processId, paramAddressId);
        Assert.assertEquals(value.size(), 1);
        Assert.assertEquals(value.get(1).getValue(), "Габдуллы Амантая, 6, apt. 33 450103 Уфа");

        dao.updateParamAddress(processId, paramAddressId, 0, new ParameterAddressValue().withHouseId(AddressTest.houseMuenchen.getId()));
        value = dao.getParamAddress(processId, paramAddressId);
        Assert.assertEquals(value.size(), 2);
        Assert.assertEquals(value.get(2).getValue(), "Dorfstraße, 99a 81247 München");

        dao.updateParamAddress(processId, paramAddressId, 1, null);
        value = dao.getParamAddress(processId, paramAddressId);
        Assert.assertEquals(value.size(), 1);

        dao.updateParamAddress(processId, paramAddressId, -1, null);
        value = dao.getParamAddress(processId, paramAddressId);
        Assert.assertEquals(value.size(), 0);

        dao.updateParamAddress(processId, paramAddressId, 0, new ParameterAddressValue().withHouseId(AddressTest.houseMuenchen.getId()));
        value = dao.getParamAddress(processId, paramAddressId);

        var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramAddressId)), false, new Pageable<>());
        int cnt = 5;
        Assert.assertEquals(log.size(), cnt);
        Assert.assertEquals(log.get(--cnt).getText(), "Габдуллы Амантая, 6, apt. 33 450103 Уфа");
        Assert.assertEquals(log.get(--cnt).getText(), "Габдуллы Амантая, 6, apt. 33 450103 Уфа; Dorfstraße, 99a 81247 München");
        Assert.assertEquals(log.get(--cnt).getText(), "Dorfstraße, 99a 81247 München");
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), "Dorfstraße, 99a 81247 München");
    }

    private void paramValueBlob(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        dao.updateParamBlob(processId, paramBlobId, "Blob value");
        var value = dao.getParamBlob(processId, paramBlobId);
        Assert.assertEquals(value, "Blob value");

        dao.updateParamBlob(processId, paramBlobId, null);
        value = dao.getParamBlob(processId, paramBlobId);
        Assert.assertNull(value);

        dao.updateParamBlob(processId, paramBlobId, "Blob value 1");

        var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramBlobId)), false, new Pageable<>());
        int cnt = 3;
        Assert.assertEquals(log.size(), cnt);
        Assert.assertEquals(log.get(--cnt).getText(), "Length: 10");
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), "Length: 12");
    }

    private void paramValueDate(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        final Date today = TimeConvert.toDate(LocalDate.now());
        dao.updateParamDate(processId, paramDateId, today);
        Assert.assertEquals(new Date(dao.getParamDate(processId, paramDateId).getTime()), today);

        dao.updateParamDate(processId, paramDateId, null);
        Assert.assertNull(dao.getParamDate(processId, paramDateId));

        final Date tomorrow = TimeConvert.toDate(LocalDate.now().plusDays(1));
        dao.updateParamDate(processId, paramDateId, tomorrow);
        Assert.assertEquals(new Date(dao.getParamDate(processId, paramDateId).getTime()), tomorrow);

        var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramDateId)), false, new Pageable<>());
        int cnt = 3;
        Assert.assertEquals(log.size(), cnt);
        Assert.assertEquals(log.get(--cnt).getText(), TimeUtils.format(today, TimeUtils.PATTERN_DDMMYYYY));
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), TimeUtils.format(tomorrow, TimeUtils.PATTERN_DDMMYYYY));
    }

    private void paramValueDateTime(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        final Date now = Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        dao.updateParamDateTime(processId, paramDateTimeId, now);
        Assert.assertEquals(new Date(dao.getParamDateTime(processId, paramDateTimeId).getTime()), now);

        dao.updateParamDateTime(processId, paramDateTimeId, null);
        Assert.assertNull(dao.getParamDateTime(processId, paramDateTimeId));

        final Date future = Date.from(now.toInstant().plusSeconds(1000));
        dao.updateParamDateTime(processId, paramDateTimeId, future);
        Assert.assertEquals(new Date(dao.getParamDateTime(processId, paramDateTimeId).getTime()), future);

        var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramDateTimeId)), false, new Pageable<>());
        int cnt = 3;
        Assert.assertEquals(log.size(), cnt);
        Assert.assertEquals(log.get(--cnt).getText(), TimeUtils.format(now, TimeUtils.PATTERN_DDMMYYYYHHMMSS));
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), TimeUtils.format(future, TimeUtils.PATTERN_DDMMYYYYHHMMSS));
    }

    private void paramValueEmail(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        final var emailFirst = new ParameterEmailValue("email1@domain.org", "First Person");
        final var emailSecond = new ParameterEmailValue("email2@domain.org", "");

        dao.updateParamEmail(processId, paramEmailId, 0, emailFirst);
        var value = dao.getParamEmail(processId, paramEmailId);
        Assert.assertEquals(value.size(), 1);
        Assert.assertEquals(value.get(1), emailFirst);

        dao.updateParamEmail(processId, paramEmailId, 0, emailSecond);
        value = dao.getParamEmail(processId, paramEmailId);
        Assert.assertEquals(value.size(), 2);
        Assert.assertEquals(value.get(2), emailSecond);

        dao.updateParamEmail(processId, paramEmailId, 1, null);
        value = dao.getParamEmail(processId, paramEmailId);
        Assert.assertEquals(value.size(), 1);

        dao.updateParamEmail(processId, paramEmailId, -1, null);
        value = dao.getParamEmail(processId, paramEmailId);
        Assert.assertEquals(value.size(), 0);

        dao.updateParamEmail(processId, paramEmailId, 0, emailFirst);

         var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramEmailId)), false, new Pageable<>());
        int cnt = 5;
        Assert.assertEquals(log.size(), cnt);
        Assert.assertEquals(log.get(--cnt).getText(), "First Person <email1@domain.org>");
        Assert.assertEquals(log.get(--cnt).getText(), "First Person <email1@domain.org>, email2@domain.org");
        Assert.assertEquals(log.get(--cnt).getText(), "email2@domain.org");
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), "First Person <email1@domain.org>");
    }

    private void paramValueFile(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());
        var fileDao = new FileDataDAO(DbTest.conRoot);

        var valueFile = dao.getParamFile(processId, paramFileId);
        Assert.assertTrue(valueFile.values().isEmpty());

        var stat = fileDao.stat();

        dao.updateParamFile(processId, paramFileId, 0, new FileData("file1.txt",
                IOUtils.toByteArray(ProcessParamTest.class.getResourceAsStream(ProcessParamTest.class.getSimpleName() + ".param.file.value.txt"))));
        dao.updateParamFile(processId, paramFileId, 0, new FileData("file2.txt",
                IOUtils.toByteArray(ProcessParamTest.class.getResourceAsStream(ProcessParamTest.class.getSimpleName() + ".param.file.value.txt"))));
        valueFile = dao.getParamFile(processId, paramFileId);
        Assert.assertEquals(valueFile.size(), 2);
        Assert.assertEquals(valueFile.get(1).getTitle(), "file1.txt");
        byte[] data1 = IOUtils.toByteArray(fileDao.getFile(valueFile.get(1)).toURI());
        Assert.assertEquals(new String(data1, StandardCharsets.UTF_8).trim(), "Test content");
        Assert.assertEquals(valueFile.get(2).getTitle(), "file2.txt");

        dao.updateParamFile(processId, paramFileId, 1, null);
        valueFile = dao.getParamFile(processId, paramFileId);
        Assert.assertEquals(valueFile.size(), 1);

        dao.updateParamFile(processId, paramFileId, -1, null);
        valueFile = dao.getParamFile(processId, paramFileId);
        Assert.assertTrue(valueFile.isEmpty());

        Assert.assertEquals(fileDao.stat(), stat);

        dao.updateParamFile(processId, paramFileId, 0, new FileData("file1.txt",
                IOUtils.toByteArray(ProcessParamTest.class.getResourceAsStream(ProcessParamTest.class.getSimpleName() + ".param.file.value.txt"))));
        valueFile = dao.getParamFile(processId, paramFileId);
        Assert.assertEquals(valueFile.size(), 1);

        var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramFileId)), false, new Pageable<>());
        int cnt = 5;
        Assert.assertEquals(log.size(), cnt);
        Assert.assertEquals(log.get(--cnt).getText(), "file1.txt");
        Assert.assertEquals(log.get(--cnt).getText(), "file1.txt, file2.txt");
        Assert.assertEquals(log.get(--cnt).getText(), "file2.txt");
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), "file1.txt");
    }

    private void paramValueList(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        Set<Integer> valuesFirst = Set.of(1, 2);
        dao.updateParamList(processId, paramListId, valuesFirst);
        Assert.assertEquals(dao.getParamList(processId, paramListId), valuesFirst);

        dao.updateParamList(processId, paramListId, (Set<Integer>) null);
        Assert.assertTrue(dao.getParamList(processId, paramListId).isEmpty());

        var valuesSecond = Map.of(2, "", 3, "Comment");
        dao.updateParamListWithComments(processId, paramListId, valuesSecond);
        Assert.assertEquals(dao.getParamListWithComments(processId, paramListId), valuesSecond);

        var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramListId)), false, new Pageable<>());
        int cnt = 3;
        Assert.assertEquals(log.size(), cnt);
        Assert.assertEquals(log.get(--cnt).getText(), "Value1, Value2");
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), "Value2, Value03");
    }

    private void paramValueListCount(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        dao.updateParamListCount(processId, paramListCountId,
                Map.of(1, "5.2", 2, Utils.parseBigDecimal("4.3")));
        Assert.assertEquals(dao.getParamListCount(processId, paramListCountId),
                Map.of(1, new ParameterListCountValue("5.20"), 2, new ParameterListCountValue("4.30")));

        dao.updateParamListCount(processId, paramListCountId, null);
        Assert.assertTrue(dao.getParamListCount(processId, paramListCountId).isEmpty());

        var values = Map.of(2, new ParameterListCountValue(Utils.parseBigDecimal("4.50"), "Comment"), 3, new ParameterListCountValue("9.20"));
        dao.updateParamListCount(processId, paramListCountId, values);
        Assert.assertEquals(dao.getParamListCount(processId, paramListCountId), values);

        var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramListId)), false, new Pageable<>());
        int cnt = 3;
        Assert.assertEquals(log.size(), cnt);
        // TODO: Change after fixing listcount change logs.
        Assert.assertEquals(log.get(--cnt).getText(), "Value1, Value2");
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), "Value2, Value03");
    }

    private void paramValueMoney(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        dao.updateParamMoney(processId, paramMoneyId, Utils.parseBigDecimal("10.55"));
        var value = dao.getParamMoney(processId, paramMoneyId);
        Assert.assertEquals(value, Utils.parseBigDecimal("10.55"));

        dao.updateParamMoney(processId, paramMoneyId, "");
        Assert.assertNull(dao.getParamMoney(processId, paramMoneyId));

        dao.updateParamMoney(processId, paramMoneyId, "10.43");
        value = dao.getParamMoney(processId, paramMoneyId);
        Assert.assertEquals(value, Utils.parseBigDecimal("10.43"));

        var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramMoneyId)), false, new Pageable<>());
        int cnt = 3;
        Assert.assertEquals(log.size(), cnt);
        Assert.assertEquals(log.get(--cnt).getText(), "10.55");
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), "10.43");
    }

    private void paramValueText(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        // paramTextId
        dao.updateParamText(processId, paramTextId, "Value 1");
        Assert.assertEquals(dao.getParamText(processId, paramTextId), "Value 1");

        dao.updateParamText(processId, paramTextId, null);
        Assert.assertNull(dao.getParamText(processId, paramTextId));

        dao.updateParamText(processId, paramTextId, "Value 2");
        Assert.assertEquals(dao.getParamText(processId, paramTextId), "Value 2");

        var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramTextId)), false, new Pageable<>());
        int cnt = 3;
        Assert.assertEquals(log.size(), cnt);
        Assert.assertEquals(log.get(--cnt).getText(), "Value 1");
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), "Value 2");

        // paramText show as link
        dao.updateParamText(processId, paramTextShowAsLinkId, "1.1.1.1");
    }

    private void paramValuePhone(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        var value = new ParameterPhoneValue(List.of(new ParameterPhoneValueItem("73472333333", "Comment 1")));
        dao.updateParamPhone(processId, paramPhoneId, value);
        Assert.assertEquals(dao.getParamPhone(processId, paramPhoneId), value);

        value = new ParameterPhoneValue();
        dao.updateParamPhone(processId, paramPhoneId, value);
        Assert.assertEquals(dao.getParamPhone(processId, paramPhoneId), value);

        value = new ParameterPhoneValue(List.of(
            new ParameterPhoneValueItem("73472333333", "Comment 1"),
            new ParameterPhoneValueItem("79172333334", "Comment 2")));
        dao.updateParamPhone(processId, paramPhoneId, value);
        Assert.assertEquals(dao.getParamPhone(processId, paramPhoneId), value);

        var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramPhoneId)), false, new Pageable<>());
        int cnt = 3;
        Assert.assertEquals(log.size(), cnt);
        Assert.assertEquals(log.get(--cnt).getText(), "+7 (347) 233-33-33 [Comment 1]");
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), "+7 (347) 233-33-33 [Comment 1], +7 917 233-33-34 [Comment 2]");
    }

    private void paramValueTree(int processId) throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        var values = Set.of("1", "2.1");
        dao.updateParamTree(processId, paramTreeId, values);
        Assert.assertEquals(dao.getParamTree(processId, paramTreeId), values);

        dao.updateParamTree(processId, paramTreeId, null);
        Assert.assertEquals(dao.getParamTree(processId, paramTreeId), Set.of());

        values = Set.of("2", "1.2");
        dao.updateParamTree(processId, paramTreeId, values);
        Assert.assertEquals(dao.getParamTree(processId, paramTreeId), values);

        var log = new ParamLogDAO(DbTest.conRoot).getHistory(processId, ParameterCache.getParameterList(List.of(paramTreeId)), false, new Pageable<>());
        int cnt = 3;
        Assert.assertEquals(log.size(), cnt);
        Assert.assertEquals(log.get(--cnt).getText(), "Value 1, Value 2.1");
        Assert.assertEquals(log.get(--cnt).getText(), "");
        Assert.assertEquals(log.get(--cnt).getText(), "Value 1.2, Value2");
    }
}
