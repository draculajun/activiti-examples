package activiti.controller;

import activiti.api.DemoApi;
import activiti.mapperdao.BlackListMapper;
import activiti.pojo.Result;
import activiti.pojo.ResultUtil;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("activiti")
public class ActivitiController {

    @Autowired
    DemoApi demoApi;

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

    //删除部署
    @GetMapping("/deleteProcessDefinition/{id}")
    public Result deleteProcessDefinition(@PathVariable(value = "id") String id) {
        repositoryService.deleteDeployment(id, true);
        return ResultUtil.success("删除部署:" + id + "完成");
    }

    //查询某人的任务列表（包括当前分配给他的以及等待签收的）
    @GetMapping("/userTasks/{username}")
    public Result userTasks(@PathVariable(value = "username") String username) {
        List<Task> allTasks = new ArrayList<>();
        //读取直接分配给当前人或已签收的任务
        List<Task> doingTasks = taskService.createTaskQuery().taskAssignee(username).list();
        //等待签收的任务
        List<Task> waitingClaimTasks = taskService.createTaskQuery().taskCandidateUser(username).list();
        allTasks.addAll(doingTasks);
        allTasks.addAll(waitingClaimTasks);

        //简化方法（简化上面的合集）
        allTasks = taskService.createTaskQuery().taskCandidateOrAssigned(username).list();

        return ResultUtil.success("用户 " + username + " 的任务列表：" + allTasks);
    }

    //查询候选组任务列表
    @GetMapping("/candidateGroup/{candidateGroup}")
    public Result candidateGroupTasks(@PathVariable(value = "candidateGroup") String candidateGroup) {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(candidateGroup).list();
        return ResultUtil.success("候选组 " + candidateGroup + " 的任务列表：" + tasks);
    }

    //内置表单formService启动流程，加入流程参数
    @GetMapping("/formService")
    public Result formService() {
        //---------------------------------------------------------------------------------------------------------
        //设置流程发起人
        String currentUserId = "abc";
        identityService.setAuthenticatedUserId(currentUserId);
        //---------------------------------------------------------------------------------------------------------
        //启动流程
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();
//        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave").singleResult();
        Map<String, String> variables = new HashMap<>();
//        variables.put("startDate", formatter.format(now));
//        variables.put("endDate", formatter.format(now.plusMonths(1)));
//        variables.put("reason", "休假测试");
//        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
        //---------------------------------------------------------------------------------------------------------
        //部门领导审批通过
//        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
//        variables = new HashMap<>();
//        variables.put("deptLeaderApproved", "true");
//        formService.submitTaskFormData(deptLeaderTask.getId(), variables);
        //模拟请求，过滤不可修改的流程参数并提交审核
//        Map<String, String> reqMap = new HashMap<>();
//        reqMap.put("startDate", "2019-10-10");
//        reqMap.put("endDate", "2019-11-11");
//        reqMap.put("reason", "reason");
//        reqMap.put("deptLeaderApproved", "true");   //部门审批意见为isWritable
//        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
//        TaskFormData taskFormData = formService.getTaskFormData(deptLeaderTask.getId());
//        List<FormProperty> formProperties = taskFormData.getFormProperties();
//        Map<String, String> formValues = new HashMap<>();
//        formProperties.stream().forEach(e -> {
//            if (e.isWritable()) {
//                formValues.put(e.getId(), reqMap.get(e.getId()));
//            }
//        });
//        formService.submitTaskFormData(deptLeaderTask.getId(), formValues);
        //---------------------------------------------------------------------------------------------------------
        //人事审批通过
//        Task hrTask = taskService.createTaskQuery().taskCandidateGroup("hr").singleResult();
//        variables.put("hrApproved", "true");
//        formService.submitTaskFormData(hrTask.getId(), variables);
        //---------------------------------------------------------------------------------------------------------
        //销假
//        Task reportBackTask = taskService.createTaskQuery().taskAssignee(currentUserId).singleResult();
//        variables.put("reportBackDate", formatter.format(now));
//        formService.submitTaskFormData(reportBackTask.getId(), variables);
        //---------------------------------------------------------------------------------------------------------



        return ResultUtil.success(null, "任务完成");
    }

    @GetMapping("/test")
    public Result test(){
        return ResultUtil.success(null, "Activiti is OK");
    }


    @GetMapping("/feignTest")
    public Result feignTest(){
        Result result = demoApi.getDemo();
        return ResultUtil.success(null, result.getMessage());
    }

}
