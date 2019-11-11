package activiti.mapperdao;

import activiti.pojo.Deployment;

public interface DeploymentMapper {

    Deployment selectByPrimaryKey(String id);

}

