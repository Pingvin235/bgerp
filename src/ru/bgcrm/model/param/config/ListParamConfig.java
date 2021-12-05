package ru.bgcrm.model.param.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.RangeChecker;
import ru.bgcrm.util.Utils;

/**
 * List parameter value.
 *
 * @author Shamil Vakhitov
 */
public class ListParamConfig extends Config {
    private static final String ALLOW_COMMENT_VALUES = "allowCommentValues";
    private static final String NEED_COMMENT_VALUES = "needCommentValues";

    private Map<Integer, String> allowCommentValues = Collections.emptyMap();
    private Map<Integer, String> needCommentValues = Collections.emptyMap();
    private Map<Integer, String> commentValues = Collections.emptyMap();;

    public ListParamConfig(ParameterMap setup) {
        super(null);

        String values = setup.get(ALLOW_COMMENT_VALUES);
        if (Utils.notBlankString(values)) {
            allowCommentValues = new RangeCheckerMapWrapper(new RangeChecker(values));
        }
        values = setup.get(NEED_COMMENT_VALUES);
        if (Utils.notBlankString(values)) {
            needCommentValues = new RangeCheckerMapWrapper(new RangeChecker(values));
        }

        if (allowCommentValues != null || needCommentValues != null) {
            commentValues = new HashMap<Integer, String>() {
                @Override
                public String get(Object key) {
                    String result = null;

                    if (allowCommentValues != null) {
                        result = allowCommentValues.get(key);
                    }
                    if (result == null && needCommentValues != null) {
                        result = needCommentValues.get(key);
                    }

                    return result;
                }
            };
        }
    }

    public Map<Integer, String> getAllowCommentValues() {
        return allowCommentValues;
    }

    public Map<Integer, String> getNeedCommentValues() {
        return needCommentValues;
    }

    public Map<Integer, String> getCommentValues() {
        return commentValues;
    }

    private static class RangeCheckerMapWrapper extends HashMap<Integer, String> {
        private RangeChecker checker;

        private RangeCheckerMapWrapper(RangeChecker checker) {
            this.checker = checker;
        }

        @Override
        public String get(Object key) {
            if (key instanceof Integer) {
                return checker.check(((Integer) key).longValue()) ? "1" : null;
            } else if (key instanceof Long) {
                return checker.check((Long) key) ? "1" : null;
            }
            return null;
        }

        @Override
        public boolean containsKey(Object key) {
            return get(key) != null;
        }
    }
}