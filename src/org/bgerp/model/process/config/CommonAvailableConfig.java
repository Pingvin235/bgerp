package org.bgerp.model.process.config;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public abstract class CommonAvailableConfig extends ru.bgcrm.util.Config {
    public static class Rule {
        /** For which process link types is the rule valid. Empty or {@code null} - for all types. */
        public Set<String> linkTypes;
        /** Process type IDs filter. Empty or {@code null} - unused.*/
        public final Set<Integer> typeIds;
        /** Process closing date filter. When {@code null} - unused. */
        public final Boolean open;
        /** Process status IDs filter. Empty or {@code null} - unused. */
        public final Set<Integer> statusIds;

        private Rule(ParameterMap config) {
            this(Collections.unmodifiableSet(Utils.toSet(config.get("relation.link.types"))),
                Collections.unmodifiableSet(Utils.toIntegerSet(config.get("filter.process.types"))),
                Utils.parseBoolean(config.get("filter.process.open"), null),
                Collections.unmodifiableSet(Utils.toIntegerSet(config.get("filter.process.statuses"))));
        }

        private Rule(Set<String> linkTypes, Set<Integer> typeIds, Boolean open, Set<Integer> statusIds) {
            this.linkTypes = linkTypes;
            this.typeIds = typeIds;
            this.open = open;
            this.statusIds = statusIds;
        }
    }

    private final List<Rule> rules;

    protected CommonAvailableConfig(ParameterMap config) throws InitStopException {
        super(null);
        rules = config.subKeyed(prefix() + ".available.").values().stream()
            .map(cfg -> new Rule(cfg))
            .collect(Collectors.toList());
        initWhen(!rules.isEmpty());
    }

    protected abstract String prefix();

    public List<Rule> rules(String linkType) {
        return rules.stream()
            .filter(rule -> rule.linkTypes == null || rule.linkTypes.isEmpty() || rule.linkTypes.contains(linkType))
            .collect(Collectors.toList());
    }
}
