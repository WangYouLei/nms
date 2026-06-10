package com.wang.aiserver.controller;

import com.wang.common.result.Result;
import com.wang.aiserver.service.AiWritingService;
import com.wang.pojo.dto.AiWritingDTO;
import com.wang.pojo.vo.AiWritingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(tags = "AI写作助手")
@RequestMapping("/aiWriting")
public class AiWritingController {

    private final AiWritingService aiWritingService;

    public AiWritingController(AiWritingService aiWritingService) {
        this.aiWritingService = aiWritingService;
    }

    @PostMapping("/assist")
    @ApiOperation("AI写作助手")
    public Result writingAssist(
            @RequestBody @ApiParam("写作助手请求参数") AiWritingDTO request) {
        log.info("AI写作助手请求：type={}", request.getType());
        return aiWritingService.writingAssist(request);
    }
}
