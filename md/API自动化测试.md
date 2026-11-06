# 青芒视频 API 功能自动化测试

## 测试内容

脚本基于 Python 标准库 `unittest`、`urllib`，不需要安装第三方依赖。测试通过 HTTP 调用正在运行的后端接口，校验 HTTP 状态码、统一响应 `code`、关键字段类型、分页结构、登录授权边界，以及可选的创建—查询—修改—删除完整流程。

| 功能 | 接口覆盖 | 测试内容 |
|---|---|---|
| 认证与公开资料 | `GET /api/auth/ping`、`GET /api/auth/user/{userId}`、`POST /api/auth/login`、`GET /api/auth/me`、`POST /api/auth/logout` | 匿名探活、公开用户资料、登录令牌、当前账号归属、登出后会话失效 |
| 首页与视频浏览 | `GET /api/banner`、`GET /api/video/categories`、`GET /api/video/list`、`GET /api/video/{videoId}`、`GET /api/video/getVideoTitle/{videoId}` | 可见轮播、分区、视频列表分页、视频详情和标题一致性 |
| 搜索 | `GET /api/search/video`、`GET /api/search/user`、`GET /api/search/post`、`GET /api/search/hot` | 空关键词搜索的分页结构、热搜列表和数量上限 |
| 用户主页 | `GET /api/home/featured`、`/contribute`、`/love`、`/collectsClassify`、`/collectByName`、`/dynamic`、`/dynamicImg`、`/relations`；`GET /api/user/{userId}/videos` | 主页各类列表、收藏夹、关系列表、游标分页结构 |
| 动态 | `GET /api/post/list`、`GET /api/user/mentions`、`GET /api/post/authors`、`GET /api/relation/list` | 公开动态流以及登录后的动态提醒、作者和关注列表 |
| 评论与弹幕 | `GET /api/comment/list`、`GET /api/danmaku/list`、`GET /api/danmaku/snapshot` | 指定视频下的评论分页、弹幕列表及快照结构 |
| 收藏与观看 | `GET /api/folder/list`、`GET /api/folder/videos`、`GET /api/watch-later/list`、`GET /api/history/list`、`GET /api/video/{videoId}/state`、`GET /api/video/{videoId}/folders` | 登录用户的收藏夹、待看、历史和互动状态 |
| 消息与通知 | `GET /api/privateMessage/selectDialogue`、`GET /api/privateMessage/selectMessage`、`GET /api/privateMessage/selectNotReadNumber`、`GET /api/notification/mentions`、`/unread`、`GET /api/comment/selectReplayComment`、`/selectAt`、`/selectLike`、`GET /api/share/recipients` | 会话、未读计数、提醒列表及分享接收人；会话详情需配置会话 ID |
| 投稿管理 | `GET /api/video/getVideoInfo`、`GET /api/video/userUploadVideo`、`POST /api/video/selectVideoData` | 登录用户可访问投稿详情、投稿列表和投稿统计；详情需配置视频 ID |
| 管理后台 | `GET /api/system/overview/getOverview`、`GET /api/system/user/searchBanList`、`GET /api/system/video/getSubZoneKeys`、用户/评论/动态/消息/关键词/日志/视频搜索接口 | 管理员概览、分页列表字段和 `superAdminFlag`；同时可验证普通用户被拒绝 |
| 授权边界 | 用户资料、收藏、通知、评论、动态、互动、私信、后台查询及管理写接口 | 对未登录请求，验证接口不会返回成功业务响应 |
| 可选写入流程 | `POST /api/folder`、`GET /api/folder/list`、`PUT /api/folder/{folderId}`、`DELETE /api/folder/{folderId}` | 创建测试收藏夹、回读、改名验证，最后清理测试数据 |

上表列出当前自动化脚本覆盖的功能接口，不代表系统全部接口清单。文件上传、视频转码/发布、管理员封禁/删内容等可能产生不可逆副作用的写接口不在默认运行范围内。

### 请求参数与预期结果

| 接口 | 请求示例 / 必要参数 | 鉴权 | 自动检查内容 |
|---|---|---|---|
| `GET /api/auth/ping` | 无 | 否 | 成功响应，未登录时 `data` 为 `anonymous` |
| `POST /api/auth/login` | JSON：`{"username":"...","password":"..."}` | 否 | `data.token` 存在；后续以 `token` 请求头携带 |
| `GET /api/auth/user/{userId}` | 路径中的用户 ID | 否 | 返回资料的 `userId` 与请求 ID 相同 |
| `GET /api/video/list` | `pageNum=1&pageSize=5&sort=latest` | 否 | `data.records` 为数组且最多 5 条 |
| `GET /api/search/video`、`/user`、`/post` | `pageSize=5`；不传关键词以避免写入搜索历史 | 否 | 返回分页对象及 `records` 数组 |
| `GET /api/search/hot` | `limit=5` | 否 | 返回数组且最多 5 条 |
| `GET /api/post/list` | `size=5` | 否 | 返回游标分页对象及 `records` 数组 |
| `GET /api/home/{featured,contribute,love,collectsClassify,dynamic,dynamicImg}` | `homeUserId=<API_USER_ID>&pageNum=1` | 否 | 分页接口检查 `records`；点赞及收藏夹分类检查数组 |
| `GET /api/home/collectByName` | `homeUserId=<API_USER_ID>&folderId=<API_FOLDER_ID>` | 否 | 返回指定收藏夹的视频分页；收藏夹 ID 可选配置 |
| `GET /api/home/relations` | `homeUserId=<API_USER_ID>&type=8&pageNum=1` | 否 | 返回关注关系列表分页 |
| `GET /api/user/{userId}/videos` | `size=5` | 否 | 返回游标分页对象 |
| `GET /api/video/{videoId}`、`/getVideoTitle/{videoId}` | 公开可播放的视频 ID | 否 | 详情含标题，标题接口与详情标题一致 |
| `GET /api/comment/list` | `videoId=<API_VIDEO_ID>&size=5` | 否 | 返回评论分页对象 |
| `GET /api/danmaku/list`、`/snapshot` | `videoId=<API_VIDEO_ID>&limit=5` | 否 | 列表返回数组，快照返回对象 |
| `GET /api/folder/list`、`/folder/videos` | 分页示例：`pageNum=1&pageSize=5` | 是 | 收藏夹返回数组，收藏视频返回分页对象 |
| `GET /api/watch-later/list`、`/history/list` | `size=5` | 是 | 返回含 `records` 数组的游标分页对象 |
| `GET /api/video/{videoId}/state`、`/folders` | 视频 ID | 是 | 互动状态返回对象，所属收藏夹返回数组 |
| `GET /api/privateMessage/selectDialogue` | `pageNum=1` | 是 | 返回会话数组 |
| `GET /api/privateMessage/selectMessage` | `dialogueId=<API_DIALOGUE_ID>&pageNum=1` | 是 | 返回会话消息对象 |
| `GET /api/privateMessage/selectNotReadNumber` | `userId` 必须为当前登录用户 ID | 是 | 返回未读消息整数 |
| `GET /api/notification/mentions` | `size=5` | 是 | 返回提醒游标分页对象 |
| `GET /api/notification/unread` | 无 | 是 | 返回各类未读数量对象 |
| `GET /api/comment/selectReplayComment`、`/selectAt`、`/selectLike` | `pageNum=1` | 是 | 返回通知数组 |
| `GET /api/share/recipients` | 可选游标 `cursorId` | 是 | 返回接收人分页对象 |
| `GET /api/video/getVideoInfo` | `videoId=<API_OWN_VIDEO_ID>`，必须是当前用户自己的投稿 | 是 | 返回可编辑投稿详情对象 |
| `GET /api/video/userUploadVideo` | `videoStatus=-1&pageNum=1` | 是 | 返回投稿列表对象 |
| `POST /api/video/selectVideoData` | 空 JSON 请求体 | 是 | 返回投稿统计对象 |
| `POST /api/system/user/searchUsers` | JSON：`{"pageNum":1,"keyword":"","type":-1,"loginFlag":-1}` | 管理员 | 返回分页用户列表，每条记录含 `adminFlag`、`superAdminFlag` |
| `POST /api/system/content/searchComments`、`searchDynamics`、`searchMessages`、`/api/system/keyWord/searchKeyWords`、`/api/system/log/searchLogs`、`/api/system/video/searchVideos` | JSON：`{"pageNum":1,"keyword":"","type":-1,"status":-1,"sortWay":0}` | 管理员 | 返回分页对象及 `records` 数组 |
| `GET /api/system/user/searchBanList` | `pageNum=1` | 管理员 | 返回封禁记录分页对象 |
| `GET /api/system/overview/getOverview`、`/api/system/video/getSubZoneKeys` | 无 | 管理员 | 分别返回概览对象和分区数组 |

接口成功响应的统一外层格式为 `{"code":1,"data":...}`。接口错误的 HTTP 状态码可能是 4xx，也可能是 HTTP 200 携带非 1 的 `code`；授权测试以“不是成功业务响应”作为断言，而不绑定某一种错误码。

## 运行前准备

1. 启动后端应用及 MySQL、Redis 等运行依赖，并确认测试账号可以正常访问。
2. 至少配置 `API_USERNAME`、`API_PASSWORD` 以运行登录用户功能测试。
3. 配置 `API_ADMIN_USERNAME`、`API_ADMIN_PASSWORD` 以运行管理员接口测试。
4. 使用现有、可读取的数据配置可选的 ID。用户主页测试账号应开放相关列表；收藏夹 ID 应属于该用户且对外可见。测试不会自行创建账号或假定固定数据库内容。

| 环境变量 | 用途 | 未设置时 |
|---|---|---|
| `API_BASE_URL` | 后端根地址，例如 `http://localhost:8081` | 使用 `http://localhost:8081` |
| `API_USERNAME`、`API_PASSWORD` | 普通用户登录凭据 | 登录用户测试跳过 |
| `API_ADMIN_USERNAME`、`API_ADMIN_PASSWORD` | 管理员登录凭据 | 管理后台测试跳过 |
| `API_USER_ID` | 已存在且可公开访问的用户 ID | 用户资料仍测试其余公开接口；主页接口测试跳过 |
| `API_FOLDER_ID` | 属于 `API_USER_ID` 且公开的收藏夹 ID | 指定收藏夹视频列表测试跳过 |
| `API_VIDEO_ID` | 已存在且可查看的视频 ID | 视频详情、评论、弹幕和部分投稿接口跳过 |
| `API_OWN_VIDEO_ID` | 当前测试用户自己的投稿 ID | 投稿编辑详情测试跳过 |
| `API_DIALOGUE_ID` | 当前测试用户可查看的私信会话 ID | 私信会话内容测试跳过 |
| `API_RUN_WRITE_TESTS` | 设为 `1` 才运行收藏夹 CRUD 往返测试 | 写入流程跳过 |

## 运行命令

PowerShell 示例：

```powershell
$env:API_BASE_URL = "http://localhost:8081"
$env:API_USERNAME = "test_user"
$env:API_PASSWORD = "test_password"
$env:API_ADMIN_USERNAME = "test_admin"
$env:API_ADMIN_PASSWORD = "test_admin_password"
$env:API_USER_ID = "2"
$env:API_FOLDER_ID = "3"
$env:API_VIDEO_ID = "10"
$env:API_OWN_VIDEO_ID = "10"
$env:API_DIALOGUE_ID = "1"
python script/api-tests/run_tests.py
```

`run_tests.py` 会先快速探测 API 并检查账号变量。服务不可用或账号配置不完整时，会输出一条原因和修复提示后退出，不会继续请求所有接口产生一屏连接错误。退出码 `0` 表示测试通过，`1` 表示有测试失败，`2` 表示测试启动条件不满足。

如果只运行默认只读测试，不要设置 `API_RUN_WRITE_TESTS`。只有在专用测试环境、且允许测试账号临时创建和删除一个收藏夹时，才运行：

```powershell
$env:API_RUN_WRITE_TESTS = "1"
python script/api-tests/run_tests.py
```

## 接口调用与判定规则

- 所有 API 成功响应都检查 HTTP 2xx，并要求 JSON 中 `code` 为 `1`；`data` 按接口契约检查为对象、数组或分页对象。
- 普通登录令牌通过请求头 `token` 传递；观影历史接口按控制器契约通过 `Authorization` 请求头传令牌。
- 普通用户接口和管理员接口分别使用对应测试账号。管理员用户列表还验证每条记录包含 `adminFlag` 和 `superAdminFlag`。
- 写接口的匿名授权测试只发送不可能成功的未登录请求，不应更改业务数据。默认功能测试使用只读请求；唯一的可选写流程会在 `finally` 中删除创建的测试收藏夹。
- 缺少账号、资源 ID 或运行条件时，相关用例显示为 `skipped`；接口连接失败或断言失败则测试失败，不会伪装成跳过。

## 测试文件

- `script/api-tests/test_api.py`：公共接口、登录流程、管理员概览和基础权限测试。
- `script/api-tests/test_functional_api.py`：视频、搜索、主页、收藏、通知、私信、投稿及管理后台功能接口测试。
