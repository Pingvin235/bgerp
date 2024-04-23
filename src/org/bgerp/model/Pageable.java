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
    private final List<T> list = new ArrayList<>();

    public Pageable() {}

    /**
     * Constructor with page size.
     * @param pageSize
     */
    public Pageable(int pageSize) {
        page.setPageSize(pageSize);
    }

    /**
     * Constructor sets a created Pageable object to {@code form} response.
     * Restores a page size from user personalization. If nothing is stored there,
     * then {@code defaultPageSize} is used.
     * @param form the form.
     * @param defaultPageSize the default page size.
     */
    public Pageable(DynActionForm form, int defaultPageSize) {
        this.page = form.getPage();

        // restore user stored page size
        if (page.isPaginationEnabled() && page.getPageSize() <= 0) {
            String key = Page.PAGE_SIZE + "." + form.getPageableId();
            if (form.getUser() != null)
                page.setPageSize(form.getUser().getPersonalizationMap().getInt(key, defaultPageSize));
        }

        form.getResponse().addPageable(this);
    }

    /**
     * Constructor sets a created Pageable object to {@code form} response.
     * Restores a page size from user personalization. If nothing is stored there,
     * then {@link Page#DEFAULT_PAGE_SIZE} is used.
     * @param form the form.
     */
    public Pageable(DynActionForm form) {
        this(form, Page.DEFAULT_PAGE_SIZE);
    }

    /**
     * @return the instance with disabled pagination.
     */
    public Pageable<T> withoutPagination() {
        page.setPageIndex(Page.PAGE_INDEX_NO_PAGING);
        return this;
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
