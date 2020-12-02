package com.example.easy.elasticsearch;

import com.example.easy.config.EsTest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("ass")
    public void easyTest(){
        EsTest test = new EsTest();
        EasySearchBody body = new EasySearchBody();
        List<String> fields = new ArrayList<>();
        fields.add("test");
        body.setSearchTargetField(fields);
        body.setSearchValue("A");
        test.query(body,EasyTest.class);
    }
}
