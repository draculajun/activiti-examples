package activiti.service.impl;

import activiti.mapperdao.DeploymentMapper;
import activiti.pojo.Deployment;
import activiti.service.DeploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeploymentServiceImpl implements DeploymentService {

    @Autowired
    DeploymentMapper deploymentMapper;

    @Override
    public Deployment selectByPrimaryKey(String id) {
        return deploymentMapper.selectByPrimaryKey(id);
    }
}
