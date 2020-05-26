package ru.bgcrm.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Page {
    public static final int DEFAULT_PAGE_SIZE = 25;

    public static final String RECORD_COUNT = "recordCount";
    public static final String PAGE_COUNT = "pageCount";
    public static final String PAGE_INDEX = "pageIndex";
    public static final String PAGE_SIZE = "pageSize";
    public static final String PAGEABLE_ID = "pageableId";

    public static int PAGE_INDEX_NO_PAGING = -1;

    private int pageSize = 0;
    // по-умолчанию 1, чтобы по дефолту выводилась первая страница
    // передача page.pageIndex = -1 в запросе означает, что _не_ нужно постраничное разбиение
    // а если параметра нет, то будет значение по-умолчанию, т.е. первая страница
    private int pageIndex = 1;
    private int pageCount = 0;
    private int recordCount = 0;

    public Page(int pageIndex, int pageSize) {
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
    }

    public Page() {
    }

    public void setData(Page page) {
        this.pageSize = page.pageSize;
        this.pageIndex = page.pageIndex;
        this.pageCount = page.pageCount;
        this.recordCount = page.recordCount;
    }

    /**
     * Возвращает необходимость постраничного разбиения результатов запроса. 		
     */
    public boolean isNeedPaging() {
        return pageIndex > 0;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getPageFirstRecordNumber() {
        return (getPageIndex() - 1) * getPageSize();
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
        if (recordCount > 0 && pageSize > 0) {
            BigDecimal a = new BigDecimal(recordCount);
            BigDecimal b = a.divide(new BigDecimal(pageSize), RoundingMode.UP);
            pageCount = b.intValue();
            if (pageIndex > pageCount) {
                pageIndex = pageCount;
            }
        }
    }
}
