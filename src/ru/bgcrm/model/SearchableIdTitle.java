package ru.bgcrm.model;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.model.base.IdTitle;

public class SearchableIdTitle extends IdTitle {
    private List<String> searchMode = new ArrayList<>();

    public List<String> getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(List<String> searchMode) {
        this.searchMode = searchMode;
    }
}
