package ru.bgcrm.model;

import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Pagination supporting list.
 * @author Shamil
 */
public class SearchResult<L> {
    private Page page = new Page();
    // модифицировать лист нельзя, т.к. он сразу добавляется в response.data
    private final List<L> list = new ArrayList<L>();

    public SearchResult() {}
    
    public SearchResult(int pageSize) {
        page.setPageSize(pageSize);
    }

    public SearchResult(DynActionForm form) {
        this.page = form.getPage();

        // восстановление сохранённого для пользователя размера страницы
        if (page.isNeedPaging() && page.getPageSize() <= 0) {
            String pageableId = form.getPageableId();
            String key = Page.PAGE_SIZE + "." + pageableId;

            if (form.getUser() != null) {
                page.setPageSize(form.getUser().getPersonalizationMap().getInt(key, Page.DEFAULT_PAGE_SIZE));
            }
        }

        form.getResponse().addSearchResult(this);
    }

    public Page getPage() {
        return page;
    }

    public List<L> getList() {
        return list;
    }
}
