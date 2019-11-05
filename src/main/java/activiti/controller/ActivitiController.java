package activiti.controller;

import activiti.mapperdao.BlackListMapper;
import activiti.pojo.Result;
import activiti.pojo.ResultUtil;
import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

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

    //查看流程图，根据部署记录ACT_GE_BYTEARRAY的DEPLOYMENT_ID_
    @GetMapping("/viewPic/{deployId}")
    public void viewPic(@PathVariable("deployId") String deployId, HttpServletResponse response) throws IOException {
        String resouceName = "";

        //获取ACT_GE_BYTEARRAY里部署的流程定义的png图片资源
        List<String> resouceNameList = repositoryService.getDeploymentResourceNames(deployId).stream().map(e -> {
            if (e.indexOf(".png") > 0) {
                return e;
            } else {
                return null;
            }
        }).collect(Collectors.toList());
        if (resouceNameList != null) {
            resouceName = resouceNameList.get(1);
        }

        //写文件
        InputStream is = repositoryService.getResourceAsStream(deployId, resouceName);
        File file = new File("./" + resouceName);
        FileUtils.copyInputStreamToFile(is, file);

        //传到前台
        FileInputStream fis = new FileInputStream(file);
        int i = fis.available();
        byte data[] = new byte[i];
        fis.read(data);
        OutputStream toClient = response.getOutputStream();
        toClient.write(data);
        toClient.flush();
        toClient.close();
        fis.close();
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

    //完成我的任务，参数taskId为findMyPersonalTask接口查询返回的
    // ACT_RU_TASK(当前任务);
    @GetMapping("/completeMyPersonalTask/{taskId}")
    public Result completeMyPersonalTask(@PathVariable("taskId") String taskId) {
        taskService.complete(taskId);
        return ResultUtil.success("任务完成:" + taskId);
    }



}
