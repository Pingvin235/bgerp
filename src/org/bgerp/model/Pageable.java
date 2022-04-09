package org.bgerp.model;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.util.Dynamic;

import ru.bgcrm.model.Page;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Pagination supporting list.
 *
 * @author Shamil Vakhitov
 */
public class Pageable<T> {
    private Page page = new Page();
    // final, because initially added response.data
    private final List<T> list = new ArrayList<T>();

    public Pageable() {}

    /**
     * Constructor with page size.
     * @param pageSize
     */
    public Pageable(int pageSize) {
        page.setPageSize(pageSize);
    }

    /**
     * Constructor placing created Pageable to {@code form} response.
     * @param form
     */
    public Pageable(DynActionForm form) {
        this.page = form.getPage();

        // restore user stored page size
        if (page.isPaginationEnabled() && page.getPageSize() <= 0) {
            String pageableId = form.getPageableId();
            String key = Page.PAGE_SIZE + "." + pageableId;

            if (form.getUser() != null) {
                page.setPageSize(form.getUser().getPersonalizationMap().getInt(key, Page.DEFAULT_PAGE_SIZE));
            }
        }

        form.getResponse().addPageable(this);
    }

    /**
     * @return pagination options.
     */
    @Dynamic
    public Page getPage() {
        return page;
    }

    /**
     * @return data list with a page content, the list is modifiable.
     */
    @Dynamic
    public List<T> getList() {
        return list;
    }
}
