package ru.bgcrm.model.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

/**
 * Message tags config.
 *
 * @author Shamil Vakhitov
 */
public class TagConfig extends Config{
    private final List<Tag> tagList = new ArrayList<>(10);
    private final Map<Integer, Tag> tagMap;

    public TagConfig(ParameterMap setup) {
        super(setup);
        for (Map.Entry<Integer, ParameterMap> me : setup.subIndexed("tag.").entrySet())
            tagList.add(new Tag(me.getKey(), me.getValue()));
        tagMap = tagList.stream().collect(Collectors.toMap(Tag::getId, t -> t));
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    public Map<Integer, Tag> getTagMap() {
        return tagMap;
    }

    public static final class Tag extends IdTitle {
        private final String color;

        public static final int TAG_ATTACH_ID = -1;

        private Tag(int id, String title, String color) {
            super(id, title);
            this.color = color;
        }

        private Tag(int id, ParameterMap params) {
            super(id, params.get("title", "???"));
            this.color = params.get("color", "ff0000");
        }

        public String getColor() {
            return color;
        }
    }
}
