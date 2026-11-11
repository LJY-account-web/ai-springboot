package com.haust.aispringboot.controller;

import com.haust.aispringboot.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//对外接口测试类
@RestController
@RequestMapping("/api")
public class TestController {
    @GetMapping("/test")
    public Result<String> test() {
//        Result result= new Result();
//        result.setCode("200");
//        System.out.println(result.getCode());
        return Result.ok("hello world");
    }

}
