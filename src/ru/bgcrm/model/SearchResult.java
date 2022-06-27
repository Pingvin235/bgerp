package ru.bgcrm.model;

import org.bgerp.model.Pageable;

import ru.bgcrm.struts.form.DynActionForm;

@Deprecated
public class SearchResult<L> extends Pageable<L> {
    public SearchResult() {}

    public SearchResult(int pageSize) {
        super(pageSize);
    }

    public SearchResult(DynActionForm form) {
        super(form);
    }
}
