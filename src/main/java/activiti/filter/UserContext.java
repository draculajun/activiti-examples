package activiti.filter;

import org.springframework.stereotype.Component;

@Component
public class UserContext {

    public static final String RELATION_ID = "relation-id";

    public static final String AUTH_TOKEN = "auth-token";

    public static final String USER_ID = "user-id";

    public static final String ORG_ID = "org-id";

    private String relationid = new String();

    private String authToken = new String();

    private String userId = new String();

    private String orgId = new String();

    public String getRelationid() {
        return relationid;
    }

    public void setRelationid(String relationid) {
        this.relationid = relationid;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

}
