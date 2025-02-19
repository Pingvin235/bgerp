package org.bgerp.util.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.junit.Assert;
import org.junit.Test;

public class MailMsgTest {
    @Test
    public void testSetAttachFileName() throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();

        MailMsg.setAttachFileName(part, "Договор № 50005038497 (Январь).xlsx");
        Assert.assertEquals("attachment;\r\n" + //
            " filename*0*=UTF-8''%D0%94%D0%BE%D0%B3%D0%BE%D0%B2%D0%BE%D1%80%20%E2%84%96;\r\n" + //
            " filename*1*=%20%35%30%30%30%35%30%33%38%34%39%37%20%28%D0%AF%D0%BD%D0%B2;\r\n" + //
            " filename*2*=%D0%B0%D1%80%D1%8C%29%2E%78%6C%73%78", part.getHeader("Content-Disposition")[0]);

        MailMsg.setAttachFileName(part, "TАМ.txt");
        Assert.assertEquals("attachment; filename*=UTF-8''%54%D0%90%D0%9C%2E%74%78%74", part.getHeader("Content-Disposition")[0]);
    }
}
