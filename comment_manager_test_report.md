# Comment & Manager Test Report
Date: 2026-06-08 19:23:29

## Summary
| Metric | Value |
|--------|-------|
| PASS | 30 |
| FAIL | 3 |
| SKIP | 23 |
| ERROR | 0 |
| Total | 56 |
| Pass Rate | 90.9% |

## Test Details

| # | Test | Time | Code | Message | Status |
|---|------|------|------|---------|--------|
| C01 | AddComment-Visitor | 0ms | N/A | missing prerequisites | SKIP |
| C02 | AddComment-Author | 0ms | N/A | missing prerequisites | SKIP |
| C03 | AddReply | 0ms | N/A | missing prerequisites | SKIP |
| C04 | CommentDetail | 0ms | N/A | no comment id | SKIP |
| C05 | NovelComments | 0ms | N/A | missing prerequisites | SKIP |
| C06 | CommentReplies | 0ms | N/A | missing prerequisites | SKIP |
| C07 | CommentTree | 0ms | N/A | missing prerequisites | SKIP |
| C08 | MyComments | 4828ms | code=0 | 系统异常，请稍后重试 | FAIL |
| C09 | UpdateComment | 0ms | N/A | missing prerequisites | SKIP |
| C10 | CommentList-Filter | 469ms | code=0 | 系统异常，请稍后重试 | FAIL |
| C11 | AuditComment | 0ms | N/A | missing prerequisites | SKIP |
| C12 | MgrCommentPage | 146ms | code=0 | 系统异常，请稍后重试 | FAIL |
| C13 | MgrCommentDetail | 0ms | N/A | missing prerequisites | SKIP |
| C14 | MgrAuditComment | 0ms | N/A | missing prerequisites | SKIP |
| C15 | DeleteComment | 0ms | N/A | missing prerequisites | SKIP |
| C16 | MgrDeleteComment | 0ms | N/A | missing prerequisites | SKIP |
| C17 | BatchDeleteComments | 0ms | N/A | missing prerequisites | SKIP |
| C18 | BatchAuditComments | 0ms | N/A | missing prerequisites | SKIP |
| M01 | ManagerLogin | 0ms | code=10000 | success | PASS |
| M02 | ManagerPage | 418ms | code=10000 | success | PASS |
| M03 | ManagerList | 145ms | code=10000 | success | PASS |
| M04 | AddManager | 1093ms | code=10000 | success | PASS |
| M05 | UpdateManager | 34ms | code=10000 | success | PASS |
| M06 | UpdateMgrPassword | 444ms | code=10000 | success | PASS |
| M07 | DeleteManager | 44ms | code=10000 | success | PASS |
| M08 | GetMgrNameAvatar | 39ms | code=10000 | success | PASS |
| M09 | DashboardOverview | 102ms | code=10000 | success | PASS |
| M10 | NovelCountStats | 88ms | code=10000 | success | PASS |
| M11 | AuthorCountStats | 36ms | code=10000 | success | PASS |
| M12 | VisitorCountStats | 46ms | code=10000 | success | PASS |
| M13 | NovelTrend | 74ms | code=10000 | success | PASS |
| M14 | AuthorRegTrend | 61ms | code=10000 | success | PASS |
| M15 | VisitorRegTrend | 39ms | code=10000 | success | PASS |
| M16 | DashRankCollect | 107ms | code=10000 | success | PASS |
| M17 | DashRankOngoing | 20ms | code=10000 | success | PASS |
| M18 | DashRankLatest | 23ms | code=10000 | success | PASS |
| M19 | DashRankNew | 29ms | code=10000 | success | PASS |
| M20 | DashRankAuthor | 32ms | code=10000 | success | PASS |
| M21 | AuthorPage | 73ms | code=10000 | success | PASS |
| M22 | AuthorList | 52ms | code=10000 | success | PASS |
| M23 | AuthorInfo | 20ms | code=10000 | success | PASS |
| M24 | VisitorPage | 46ms | code=10000 | success | PASS |
| M25 | VisitorList | 42ms | code=10000 | success | PASS |
| M26 | VisitorInfo | 28ms | code=10000 | success | PASS |
| M27 | CreateAuditRecord | 0ms | N/A | missing prerequisites | SKIP |
| M28 | AuditDetail | 0ms | N/A | no audit id | SKIP |
| M29 | PendingAudits | 72ms | code=10000 | success | PASS |
| M30 | AuditList | 53ms | code=10000 | success | PASS |
| M31 | ApproveAudit | 0ms | N/A | missing prerequisites | SKIP |
| M33 | RejectAudit | 0ms | N/A | missing prerequisites | SKIP |
| M34 | AuditStatistics | 31ms | code=10000 | success | PASS |
| M35 | CheckPending | 0ms | N/A | missing prerequisites | SKIP |
| M36 | AuditByManager | 28ms | code=10000 | success | PASS |
| M37 | BatchApprove | 0ms | N/A | missing prerequisites | SKIP |
| M38 | BatchReject | 0ms | N/A | missing prerequisites | SKIP |
| M39 | DeleteAuditRecord | 0ms | N/A | no audit id | SKIP |

## Issues Found

- **C08 MyComments**: code=0, msg=系统异常，请稍后重试
- **C10 CommentList-Filter**: code=0, msg=系统异常，请稍后重试
- **C12 MgrCommentPage**: code=0, msg=系统异常，请稍后重试

