package org.bgerp.plugin.msg.email;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.util.Log;

import ru.bgcrm.model.message.Message;
import ru.bgcrm.util.Utils;

/**
 * Cache of messages inside of IMAP folder.
 *
 * @author Shamil Vakhitov
 */
class FolderCache {
    private static final Log log = Log.getLog();

    private static final FetchProfile FETCH_PROFILE_SIZE = new FetchProfile();
    static {
        FETCH_PROFILE_SIZE.add(FetchProfile.Item.SIZE);
    }

    private static final FetchProfile FETCH_PROFILE_LIST = new FetchProfile();
    static {
        FETCH_PROFILE_LIST.add(FetchProfile.Item.ENVELOPE);
        FETCH_PROFILE_LIST.add("Message-ID");
        FETCH_PROFILE_LIST.add("Received");
    }

    /** Prefix for system message ID. */
    private static final String SYSTEM_ID_PREFIX = "index";

    private static final long MAX_CACHE_VALID_TIMEOUT_MS = Duration.ofMinutes(10).toMillis();

    private final MessageTypeEmail type;

    private long lastListTimeMs;

    /** Folder messages with partially filled fields. */
    private List<Item> data;

    FolderCache(MessageTypeEmail type) {
        this.type = type;
    }

    public synchronized List<Message> list(Folder folder) throws Exception {
        if (data == null || !isListActual(folder) || (System.currentTimeMillis() - lastListTimeMs > MAX_CACHE_VALID_TIMEOUT_MS))
            relist(folder);

        return data.stream().map(item -> item.message).collect(Collectors.toList());
    }

    private boolean isListActual(Folder folder) throws MessagingException {
        javax.mail.Message[] messages = folder.getMessages();
        folder.fetch(messages, FETCH_PROFILE_SIZE);
        List<Integer> sizes = new ArrayList<>(messages.length);
        for (var m : messages)
            sizes.add(m.getSize());

        List<Integer> dataSizes = data.stream().map(item -> item.size).collect(Collectors.toList());
        return dataSizes.equals(sizes);
    }

    public synchronized void relist(Folder folder) throws Exception {
        log.debug("relist");

        javax.mail.Message[] messages = folder.getMessages();
        folder.fetch(messages, FETCH_PROFILE_LIST);

        data = new ArrayList<>(messages.length);

        for (int i = 0; i < messages.length; i++) {
            var mp = new MessageParser(messages[i]);
            var m = new Message()
                .withTypeId(type.getId())
                .withSystemId(SYSTEM_ID_PREFIX + i).withSubject(mp.getMessageSubject())
                .withFromTime(mp.getFromTime()).withFrom(mp.getFrom());

            data.add(new Item(i, messages[i].getSize(), m));

            if (i < 20)
                log.debug("Message subject: {}, from: {}, fromTime: {}", m.getSubject(), m.getFrom(), m.getFromTime());
        }

        log.debug("relist, size: {}", data.size());

        lastListTimeMs = System.currentTimeMillis();
    }

    /** Cached message. */
    public static class Item {
        public final int index;
        private final int size;
        public final Message message;

        private Item(int index, int size, Message message) {
            this.index = index;
            this.size = size;
            this.message = message;
        }
    }

    /**
     * Removes one or more messages by IDs.
     * @param ids string IDs like 'indexDDD'.
     */
    void delete(String... ids) {
        for(var id : ids) {
            data.remove(idToIndex(id));
        }
    }

    /**
     * Converts string message ID to array index.
     * @param id string ID like 'indexDDD'.
     * @return zero-based list index.
     * @throws ArrayIndexOutOfBoundsException position is less than zero or more that maximal index.
     */
    int idToIndex(String id) throws ArrayIndexOutOfBoundsException {
        int result = Utils.parseInt(StringUtils.substringAfter(id, SYSTEM_ID_PREFIX), -1);
        if (result < 0 || data.size() <= result)
            throw new ArrayIndexOutOfBoundsException("Incorrect new message ID: " + id + "; index: " + result + "; size: " + data.size());
        return result;
    }
}
