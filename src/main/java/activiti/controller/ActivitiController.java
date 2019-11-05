package activiti.controller;

import activiti.mapperdao.BlackListMapper;
import activiti.pojo.Result;
import activiti.pojo.ResultUtil;
import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("activiti")
public class ActivitiController {

    @Autowired
    BlackListMapper blackListMapper;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    //部署流程定义
    //涉及表：
    // ACT_GE_BYTEARRAY(存放流程定义的XML和PNG);
    // ACT_RE_DEPLOYMENT(流程部署);
    // ACT_RE_PROCDEF(流程定义);
    @GetMapping("/deploymentProcessDefinition")
    public Result deploymentProcessDefinition() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        ProcessEngine engine = processEngine.getProcessEngineConfiguration().buildProcessEngine();
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("diagrams/MyProcess1.bpmn").addClasspathResource("diagrams/MyProcess1.png").deploy();
        return ResultUtil.success("部署名称:" + deployment.getName());
    }

    //启动流程实例，参数myProcess=myProcess1
    //涉及表：
    // ACT_RU_EXECUTION(流程实例);
    // ACT_RU_IDENTITYLINK(下一任务);
    @GetMapping("/startProcessInstance/{myProcess}")
    public Result startProcessInstance(@PathVariable("myProcess") String myProcess) {
        //myProcess1是流程图的Id，默认启动的是最新版本的流程定义
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(myProcess);
        return ResultUtil.success("流程定义Id:" + processInstance.getProcessDefinitionId());
    }

    //查询当前个人任务，参数name=张三
    //涉及表：
    // ACT_RU_TASK(当前任务);
    @GetMapping("/findMyPersonalTask/{name}")
    public Result findMyPersonalTask(@PathVariable("name") String name) {
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(name).list();
        if (taskList != null && taskList.size() > 0) {
            taskList.stream().forEach(e -> {
                System.out.println("任务Id:" + e.getId());
                System.out.println("任务名称：" + e.getName());
                System.out.println("任务创建时间：" + e.getCreateTime());
                System.out.println("任务办理人:" + e.getAssignee());
                System.out.println("任务流程实例Id:" + e.getProcessInstanceId());
                System.out.println("任务执行对象Id:" + e.getExecutionId());
            });
            return ResultUtil.success("taskList:" + taskList);
        } else {
            return ResultUtil.success("taskList: null");
        }
    }


}
