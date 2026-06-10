package com.wang.common.feign;

import com.wang.common.feign.fallback.SensitiveWordServiceFeignFallback;
import com.wang.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "common-server", fallback = SensitiveWordServiceFeignFallback.class)
public interface SensitiveWordServiceFeign {

    @PostMapping("/sensitive-word/detect")
    Result detectSensitiveWords(@RequestBody Map<String, String> request);

    @PostMapping("/sensitive-word/filter")
    Result filterText(@RequestBody Map<String, String> request);

    @PostMapping("/sensitive-word/auditText")
    Result auditText(@RequestBody Map<String, String> request);
}
