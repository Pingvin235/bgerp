package org.bgerp.plugin.bil.invoice.num;

import java.sql.Connection;
import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.bil.invoice.dao.InvoiceNumberDAO;
import org.bgerp.plugin.bil.invoice.model.Invoice;
import org.bgerp.plugin.bil.invoice.model.InvoiceType;
import org.bgerp.util.Log;
import org.bgerp.util.text.PatternFormatter;

import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class PatternBasedNumberProvider extends NumberProvider {
    private static final Log log = Log.getLog();

    private final String pattern;

    protected PatternBasedNumberProvider(ConfigMap config) {
        super(null);
        pattern = config.get("pattern", "");
    }

    @Override
    public void number(Connection con, InvoiceType type, Invoice invoice) throws Exception {
        var cnt = new InvoiceNumberDAO(con, invoice);

        var number = PatternFormatter.processPattern(pattern, var -> {
            try {
                if (var.startsWith("process_id")) {
                    String format = StringUtils.substringAfter(var, ":");
                    if (Utils.notBlankString(format))
                        return new DecimalFormat(format).format(invoice.getProcessId());
                    return String.valueOf(invoice.getProcessId());
                }

                if (var.startsWith("date_from")) {
                    String format = StringUtils.substringAfter(var, ":");
                    if (Utils.notBlankString(format))
                        return TimeUtils.format(invoice.getDateFrom(), format);
                    return TimeUtils.format(invoice.getDateFrom(), TimeUtils.FORMAT_TYPE_YMD);
                }

                if (var.startsWith("date_to")) {
                    String format = StringUtils.substringAfter(var, ":");
                    if (Utils.notBlankString(format))
                        return TimeUtils.format(invoice.getDateTo(), format);
                    return TimeUtils.format(invoice.getDateTo(), TimeUtils.FORMAT_TYPE_YMD);
                }

                if (var.startsWith("number_in_month_for_process")) {
                    invoice.setNumberCnt(cnt.month().process().next());

                    String format = StringUtils.substringAfter(var, ":");
                    if (Utils.notBlankString(format))
                        return new DecimalFormat(format).format(invoice.getNumberCnt());
                    return String.valueOf(invoice.getNumberCnt());
                }
            } catch (Exception e) {
                log.error(e);
            }

            return "";
        });

        invoice.setNumber(number);
    }

}
