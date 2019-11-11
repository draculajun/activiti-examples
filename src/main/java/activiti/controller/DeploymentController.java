package activiti.controller;

import activiti.pojo.Result;
import activiti.pojo.ResultUtil;
import activiti.service.DeploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deployment")
public class DeploymentController {

    @Autowired
    DeploymentService deploymentService;

    @GetMapping(value = "/{id}")
    public Result get(@PathVariable("id") String id) {
        return ResultUtil.success(deploymentService.selectByPrimaryKey(id), "");
    }

}
