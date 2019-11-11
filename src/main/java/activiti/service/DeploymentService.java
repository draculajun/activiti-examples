package activiti.service;

import activiti.pojo.Deployment;

public interface DeploymentService {

    Deployment selectByPrimaryKey(String id);

}
