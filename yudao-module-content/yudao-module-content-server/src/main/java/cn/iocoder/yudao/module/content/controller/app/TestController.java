package cn.iocoder.yudao.module.content.controller.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试Controller - 用于验证Spring MVC是否正常工作
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "Controller工作正常！");
        result.put("data", "Spring MVC已正确扫描Controller");
        return result;
    }

    @GetMapping("/api-test")
    public Map<String, Object> apiTest() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "API路径测试成功");
        result.put("path", "/test/api-test");
        return result;
    }
}
