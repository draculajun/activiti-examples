package activiti.api;

import activiti.pojo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("demo")
public interface DemoApi {

    @GetMapping("/demo")
    Result getDemo();

}
