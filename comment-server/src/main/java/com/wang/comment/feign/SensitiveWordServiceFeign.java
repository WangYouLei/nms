package com.wang.comment.feign;

import com.wang.common.result.Result;
import com.wang.pojo.vo.AuditResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "common-server")
public interface SensitiveWordServiceFeign {

    @PostMapping("/sensitive-word/detect")
    Result detectSensitiveWords(@RequestBody Map<String, String> request);

    @PostMapping("/sensitive-word/filter")
    Result filterText(@RequestBody Map<String, String> request);

    @PostMapping("/sensitive-word/auditText")
    AuditResultVO auditText(@RequestBody Map<String, String> request);
}
