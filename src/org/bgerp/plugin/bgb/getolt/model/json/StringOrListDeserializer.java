package org.bgerp.plugin.bgb.getolt.model.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Deserializes a JSON field that can be either a single string or an array of strings.
 * E.g. "macBehind": "AA:BB:CC" or "macsBehind": ["AA:BB:CC", "DD:EE:FF"]
 */
public class StringOrListDeserializer extends JsonDeserializer<List<String>> {
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<String> result = new ArrayList<>();
        if (p.currentToken() == JsonToken.START_ARRAY) {
            while (p.nextToken() != JsonToken.END_ARRAY) {
                result.add(p.getValueAsString());
            }
        } else {
            String value = p.getValueAsString();
            if (value != null && !value.isEmpty()) {
                result.add(value);
            }
        }
        return result.isEmpty() ? null : result;
    }
}
