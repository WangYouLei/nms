# Novel Module Test Report (Supplementary - Skip Tests)
Date: 2026-06-08 19:14:13

## Summary
| Metric | Value |
|--------|-------|
| PASS | 21 |
| FAIL | 0 |
| SKIP |  |
| ERROR | 0 |
| Total | 22 |
| Pass Rate | 100% |

## Environment
| Service | Port | Status |
|---------|------|--------|
| gateway-server | 5100 | Running |
| author-server | 5200 | Running |
| visitor-server | 5210 | Running |
| common-server | 5220 | Running |
| ai-server | 5230 | Running |
| novel-server | 5250 | Running |
| manager-server | 5280 | Running |
| search-server+ES | - | NOT Running (MySQL fallback) |

## Test Details

| # | Test | Time | Code/Result | Message | Status |
|---|------|------|-------------|---------|--------|
| T04 | UpdateNovel | 114ms | code=10000 | success | PASS |
| T05 | DupName | 22ms | code=30002 | 小说标题已存在 | PASS |
| T06 | DeleteNovel | 57ms | code=10000 | success | PASS |
| T07 | GetDeleted | 27ms | code=0 | 小说不存在 | PASS |
| T21 | CatPage | 251ms | code=10000 | success | PASS |
| T22 | AddCat | 83ms | code=10000 | success | PASS |
| T23 | AddCatDup | 28ms | code=30004 | 小说分类已存在 | PASS |
| T24 | UpdCat | 77ms | code=10000 | success | PASS |
| T25 | DelCat | 87ms | code=10000 | success | PASS |
| T26 | DelCatWithNovels | 34ms | code=0 | 该分类下存在小说，无法删除 | PASS |
| T39 | DelCh | 0ms | N/A | no chapter found | SKIP |
| T40 | RankCollect | 124ms | code=10000 | success | PASS |
| T41 | RankOngoing | 71ms | code=10000 | success | PASS |
| T42 | RankLatest | 46ms | code=10000 | success | PASS |
| T43 | RankNew | 57ms | code=10000 | success | PASS |
| T44 | RankAuthor | 31ms | code=10000 | success | PASS |
| T45 | HotNovels | 45ms | code=10000 | success | PASS |
| T46 | HotNovelsCat | 32ms | code=10000 | success | PASS |
| T47 | NovByCat | 75ms | code=10000 | success | PASS |
| T48 | NovByCatSort | 82ms | code=10000 | success | PASS |
| T49 | NovByCatFin | 46ms | code=10000 | success | PASS |
| T50 | AuthorDetail | 337ms | code=10000 | success | PASS |

## Notes
- These are the 22 previously SKIPped tests that required visitor-server and manager-server.
- Search tests use MySQL LIKE fallback since search-server is not running.

