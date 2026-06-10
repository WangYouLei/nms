package com.wang.visitor.service;

import com.wang.common.result.Result;

public interface VisitorReadingProgressService {

    Result updateProgress(Long visitorId, Long novelId, Long chapterId, Integer chapterOrder);

    Result getProgress(Long visitorId, Long novelId);

    Result getRecentList(Long visitorId);

    Result deleteProgress(Long visitorId, Long novelId);

    Result getReadingCount(Long novelId);
}
