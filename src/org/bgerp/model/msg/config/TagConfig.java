package org.bgerp.model.msg.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.IdTitle;
import org.bgerp.util.Dynamic;

/**
 * Message tags config
 *
 * @author Shamil Vakhitov
 */
@Dynamic
public class TagConfig extends Config {
    private final List<Tag> tagList = new ArrayList<>(10);
    private final Map<Integer, Tag> tagMap;

    protected TagConfig(ConfigMap config) {
        super(null);
        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("tag.").entrySet())
            tagList.add(new Tag(me.getKey(), me.getValue()));
        tagMap = tagList.stream().collect(Collectors.toMap(Tag::getId, t -> t));
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    public Map<Integer, Tag> getTagMap() {
        return tagMap;
    }

    /**
     * Returns value of selected history storing tag.
     * @param messageTagIds all message tags.
     * @return {@link Tag#TAG_HISTORY_ALL_ID}, {@link Tag#TAG_HISTORY_WITH_ADDRESS_ID} or 0 if none is selected.
     */
    public int getSelectedHistoryTag(Collection<Integer> messageTagIds) {
        return messageTagIds.stream()
            .filter(tagId -> tagId == Tag.TAG_HISTORY_WITH_ADDRESS_ID || tagId == Tag.TAG_HISTORY_ALL_ID)
            .findFirst()
            .orElse(0);
    }

    public static final class Tag extends IdTitle {
        private final String color;

        // Positive IDs are used for user defined tags

        /** Filtering messages by attachment. The tag is not persisted in DB. */
        @Dynamic
        public static final int TAG_ATTACH_ID = -1;
        /** Filtering unread messages. The tag is not persisted in DB. */
        @Dynamic
        public static final int TAG_UNREAD_ID = -5;

        /** Attaching History.txt with messages from address */
        @Dynamic
        public static final int TAG_HISTORY_WITH_ADDRESS_ID = -2;
        /** Attaching History.txt with all messages */
        @Dynamic
        public static final int TAG_HISTORY_ALL_ID = -3;
        /** Pinned message */
        @Dynamic
        public static final int TAG_PIN_ID = -4;

        private Tag(int id, ConfigMap config) {
            super(id, config.get("title", "???"));
            this.color = config.get("color", "ff0000");
        }

        public String getColor() {
            return color;
        }
    }
}
