package org.bgerp.plugin.bil.invoice.model;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bgerp.plugin.bil.invoice.Plugin;
import org.bgerp.plugin.bil.invoice.num.NumberProvider;
import org.bgerp.plugin.bil.invoice.num.PatternBasedNumberProvider;
import org.bgerp.plugin.bil.invoice.pos.PositionProvider;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.sql.ConnectionSet;

public class InvoiceType extends IdTitle {
    private final static Map<String, String> TEMPLATE_JSP = Map.of(
        "eu_en", Plugin.PATH_JSP_USER + "/doc/eu_en.jsp",
        "ru_ru", Plugin.PATH_JSP_USER + "/doc/ru_ru.jsp"
    );

    private final NumberProvider numberProvider;
    private final int customerId;
    private final List<PositionProvider> providers;
    private final String jsp;

    public InvoiceType(int id, ParameterMap config) throws Exception {
        super(id, config.get("title", "??? [" + id + "]"));

        this.numberProvider = loadNumberProvider(config.sub("number."));
        this.customerId = config.getInt("customer");
        this.providers = loadPositionProviders(config);
        this.jsp = loadJsp(config);
    }

    private NumberProvider loadNumberProvider(ParameterMap config) throws Exception {
        @SuppressWarnings("unchecked")
        var clazz = (Class<? extends NumberProvider>) DynamicClassManager
                .getClass(config.get("class", PatternBasedNumberProvider.class.getName()));
        return config.getConfig(clazz);
    }

    private List<PositionProvider> loadPositionProviders(ParameterMap config) throws Exception {
        var result = new ArrayList<PositionProvider>();

        for (var providerConfig : config.subIndexed("provider.").values()) {
            @SuppressWarnings("unchecked")
            var clazz = (Class<? extends PositionProvider>) DynamicClassManager.getClass(providerConfig.get("class"));
            result.add(providerConfig.getConfig(clazz));
        }

        return Collections.unmodifiableList(result);
    }

    private String loadJsp(ParameterMap config) throws Exception {
        return config.getSok(TEMPLATE_JSP.get(config.get("template", "eu_en")), false, "template.jsp", "jsp");
    }

    public Invoice invoice(ConnectionSet conSet, int processId, YearMonth month) throws Exception {
        var result = new Invoice();

        result.setTypeId(id);
        result.setProcessId(processId);
        result.setDateFrom(TimeConvert.toDate(month));
        for (var provider : providers)
            provider.addPositions(conSet, result);
        result.amount();

        return result;
    }

    /**
     * @return number provider.
     */
    public NumberProvider getNumberProvider() {
        return numberProvider;
    }

    /**
     * @return customer from who invoice is generated.
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * @return JSP template for rendering print form.
     */
    public String getJsp() {
        return jsp;
    }
}
