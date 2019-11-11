package activiti.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Deployment {

    private String id;

    private String name;

    private String category;

    private String tenantid;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deploytime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public Date getDeploytime() {
        return deploytime;
    }

    public void setDeploytime(Date deploytime) {
        this.deploytime = deploytime;
    }
}
