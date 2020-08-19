package org.bgerp.plugin.msg.email;

import java.util.List;

import javax.mail.internet.MimeMessage;

import org.bgerp.plugin.msg.email.MessageParser.MessageAttach;
import org.junit.Assert;
import org.junit.Test;

public class MessageParserTest {
    @Test
    public void testMessageParse1() throws Exception {
        MessageParser mp = new MessageParser(new MimeMessage(null, this.getClass().getResourceAsStream("mail1.eml")));
        Assert.assertEquals("xxx@station.ru", mp.getFrom());
        Assert.assertEquals("bgerpp@gmail.com", mp.getTo());
        Assert.assertEquals("Fwd: Счёт на оплату услуг №85688769 от 13.07.2020", mp.getMessageSubject());
        Assert.assertEquals(1594907527000L, mp.getFromTime().getTime());
        String text = mp.getTextContent();
        Assert.assertEquals(3584, text.length());
        Assert.assertTrue(text.contains("Для вас был выставлен счёт на продление следующих услуг:\n"));
        Assert.assertTrue(text.contains("Инструкция по оплате"));
        Assert.assertTrue(text.endsWith("Оно обязательно и не требует отписки."));
    }

    @Test
    public void testMessageParse2() throws Exception {
        MessageParser mp = new MessageParser(new MimeMessage(null, this.getClass().getResourceAsStream("mail2.eml")));
        Assert.assertEquals("peter@mmm.de", mp.getFrom());
        Assert.assertEquals("shamil@ttt.org", mp.getTo());
        Assert.assertEquals("Re: Fliegerurlaub", mp.getMessageSubject());
        Assert.assertEquals(1594477504000L, mp.getFromTime().getTime());
        String text = mp.getTextContent();
        Assert.assertEquals(1900, text.length());
        Assert.assertTrue(text.contains("die Treffpunktkoordinaten sind\n"));
        Assert.assertTrue(text.contains("> > Hallo Shamil,\n"));
        Assert.assertTrue(text.contains("> > Meine Telefonnummer findest Du in der Signatur.\n"));
    }

    @Test
    public void testMessageParse3() throws Exception {
        MessageParser mp = new MessageParser(new MimeMessage(null, this.getClass().getResourceAsStream("mail3.eml")));
        List<MessageAttach> attaches = mp.getAttachContent();
        Assert.assertEquals(1, attaches.size());
        MessageAttach attach = attaches.get(0);
        Assert.assertEquals("Счёт №71 от 20 июля на сумму 41 500 р.pdf", attach.title);
    }

    @Test
    public void testMessageParse4() throws Exception {
        MessageParser mp = new MessageParser(new MimeMessage(null, this.getClass().getResourceAsStream("mail4.eml")));
        mp.getTextContent().contains("КОРРЕКТНО заполнять поле _Назначение платежа_");
        List<MessageAttach> attaches = mp.getAttachContent();
        Assert.assertEquals(3, attaches.size());
        MessageAttach attach = attaches.get(0);
        Assert.assertEquals("3244659_Cчет за Июль 2020.pdf", attach.title);
    }

}