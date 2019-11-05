package activiti.pojo;

import java.util.List;

public class Page {

    //当前页
    private Integer currentPage;

    //起始查询
    private Integer offset;

    //一页多少条记录
    private Integer pageSize;

    //分页内容
    private List<?> pageData;

    //记录总数
    private Integer total;

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<?> getPageData() {
        return pageData;
    }

    public void setPageData(List<?> pageData) {
        this.pageData = pageData;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

}
