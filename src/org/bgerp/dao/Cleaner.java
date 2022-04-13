package org.bgerp.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.bgcrm.dao.CommonDAO;

/**
 * DB cleaner.
 *
 * @author Shamil Vakhitov
 */
public class Cleaner extends CommonDAO {
    protected final List<String> inconsistencyCleanupQueries = new ArrayList<>();

    public List<String> inconsistencyCleanupQueries() {
        return Collections.unmodifiableList(inconsistencyCleanupQueries);
    }
}
