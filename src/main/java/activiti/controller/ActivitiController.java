package activiti.controller;

import activiti.mapperdao.BlackListMapper;
import activiti.pojo.BlackList;
import activiti.pojo.Result;
import activiti.pojo.ResultUtil;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("activiti")
public class ActivitiController {

    @Autowired
    BlackListMapper blackListMapper;

    @GetMapping
    public Result activiti() {

        List<BlackList> list = blackListMapper.selectList(null);

        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        ProcessEngine engine = processEngine.getProcessEngineConfiguration().buildProcessEngine();


        return ResultUtil.success("hello world");
    }


}
