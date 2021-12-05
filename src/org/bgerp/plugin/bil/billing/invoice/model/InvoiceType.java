package org.bgerp.plugin.bil.billing.invoice.model;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bgerp.plugin.bil.billing.invoice.Plugin;
import org.bgerp.plugin.bil.billing.invoice.pp.PositionProvider;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.servlet.CustomHttpServletResponse;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class InvoiceType extends IdTitle {
    private final String numberExpression;
    private final List<PositionProvider> providers;
    private final String jsp;

    public InvoiceType(int id, ParameterMap config) throws Exception {
        super(id, config.get("title", "??? [" + id + "]"));

        this.providers = loadProviders(config);
        this.numberExpression = config.get("number.expression");
        this.jsp = config.get("jsp", Plugin.PATH_JSP_USER + "/invoice.jsp");
    }

    private List<PositionProvider> loadProviders(ParameterMap config) throws Exception {
        var result = new ArrayList<PositionProvider>();

        for (var providerConfig : config.subIndexed("provider.").values()) {
            @SuppressWarnings("unchecked")
            var clazz = (Class<? extends PositionProvider>) DynamicClassManager.getClass(providerConfig.get("class"));
            result.add(providerConfig.getConfig(clazz));
        }

        return Collections.unmodifiableList(result);
    }

    public Invoice invoice(int processId, YearMonth month) {
        var result = new Invoice();

        result.setTypeId(id);
        result.setProcessId(processId);
        result.setFromDate(TimeConvert.toDate(month));
        for (var provider : providers)
            provider.addPositions(result);
        // TODO: Thing about adding as cents invoiceId % 100 to randomize
        result.setAmount(result.getPositions().stream().map(Position::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        result.setNumber(number(result));

        return result;
    }

    private String number(Invoice invoice) {
        if (Utils.isBlankString(numberExpression))
            return invoice.getProcessId() + "-" + TimeUtils.format(invoice.getFromDate(), "yyyyMM") + "-"
                    + invoice.getAmount().toPlainString().replace('.', '-');
        return new Expression(Map.of("invoice", invoice)).getString(numberExpression);
    }

    public String getJsp() {
        return jsp;
    }

    /**
     * Generates an HTML print form.
     * @param form
     * @param invoice
     * @return
     * @throws Exception
     */
    public byte[] doc(DynActionForm form, Invoice invoice) throws Exception {
        var bos = new ByteArrayOutputStream(1000);
        var resp = new CustomHttpServletResponse(form.getHttpResponse(), bos);
        var req = form.getHttpRequest();

        req.getRequestDispatcher(jsp).include(req, resp);
        resp.flush();

        return bos.toByteArray();
    }
}
