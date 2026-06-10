package com.wang.aiserver.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.AiWritingDTO;
import com.wang.pojo.vo.AiWritingVO;

public interface AiWritingService {

    Result writingAssist(AiWritingDTO dto);
}
