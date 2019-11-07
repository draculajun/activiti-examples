package activiti.controller;

import activiti.mapperdao.BlackListMapper;
import activiti.pojo.Result;
import activiti.pojo.ResultUtil;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

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
    IdentityService identityService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    @Autowired
    FormService formService;

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
        return ResultUtil.success("部署名称:" + deployment.getId());
    }

    //zip方式，POSTMAN部署流程定义
    @PostMapping("/zipDeploy")
    public Result zipDeploy(@RequestParam(value = "file") MultipartFile file) {

        String fileName = file.getOriginalFilename();
        try {
            InputStream fileInputStream = file.getInputStream();
            String extension = FilenameUtils.getExtension(fileName);
            DeploymentBuilder deployment = repositoryService.createDeployment();
            if (extension.equals("zip") || extension.equals("bar")) {
                ZipInputStream zip = new ZipInputStream(fileInputStream);
                deployment.addZipInputStream(zip);
            } else {
                deployment.addInputStream(fileName, fileInputStream);
            }
            deployment.deploy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultUtil.success("部署名称:" + file.getOriginalFilename());
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
    // ACT_RU_EXECUTION(正在执行的执行对象表);
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
        try {
            taskService.complete(taskId);
            return ResultUtil.success("任务完成:" + taskId);
        } catch (Exception e) {
            return ResultUtil.error(500, "任务无法完成");
        }
    }

    //查询历史任务
    // ACT_HI_ACTINST(所有历史任务);
    @GetMapping("/historyTasks")
    public Result historyTasks() {
        try {
            List<HistoricActivityInstance> hisList = historyService.createHistoricActivityInstanceQuery().processInstanceId("5001").list();
            return ResultUtil.success(hisList, "任务完成");
        } catch (Exception e) {
            return ResultUtil.error(500, "任务无法完成");
        }
    }

    @GetMapping("/abcd")
    public Result test1() {

        String currentUserId = "abc";
        identityService.setAuthenticatedUserId(currentUserId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();

        //启动流程
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("myProcess2").singleResult();
        Map<String, String> variables = new HashMap<>();
        variables.put("startDate", formatter.format(now));
        variables.put("endDate", formatter.format(now.plusMonths(1)));
        variables.put("reason", "休假测试");
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);

        //部门领导审批通过
        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
        variables.put("deptLeaderApprove", "true");
        formService.submitTaskFormData(deptLeaderTask.getId(), variables);

        //人事审批通过
        deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
        System.out.println(deptLeaderTask);


        return ResultUtil.success(null, "任务完成");
    }
}
