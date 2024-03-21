package org.bgerp.plugin.bil.subscription.model;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.bgerp.app.dist.lic.License;

import ru.bgcrm.model.param.ParameterEmailValue;

/**
 * Builder of license plain text.
 *
 * @author Shamil Vakhitov
 */
public class SubscriptionLicense {
    private final StringWriter sw = new StringWriter(2000);
    private final PrintWriter pw = new PrintWriter(sw);

    public SubscriptionLicense withId(String value) {
        pw.println(License.KEY_LIC_ID + "=" + value);
        return this;
    }

    public SubscriptionLicense withEmail(ParameterEmailValue value) {
        pw.println(License.KEY_LIC_EMAIL + "=" + (value == null ? "" : value.getValue()));
        return this;
    }

    public SubscriptionLicense withLimit(String value) {
        pw.println(License.KEY_LIC_LIMIT + "=" + value);
        return this;
    }

    public SubscriptionLicense withDateTo(String value) {
        pw.println(License.KEY_LIC_DATE_TO + "=" + value);
        return this;
    }

    public SubscriptionLicense withPlugin(String name) {
        pw.println(License.KEY_LIC_PLUGIN + name + "=1");
        return this;
    }

    /**
     * @return resulting license's text.
     */
    public String build() {
        pw.flush();
        return sw.toString();
    }
}
