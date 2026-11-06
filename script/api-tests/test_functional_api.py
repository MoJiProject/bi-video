import os
import unittest
import uuid
from urllib.parse import urlencode

from test_api import (
    assert_api_success,
    check_api_ready,
    credentials,
    login,
    request,
)


def setUpModule():
    try:
        check_api_ready()
    except (AssertionError, RuntimeError) as error:
        raise unittest.SkipTest(f"API unavailable; feature tests skipped: {error}") from error


def fixture_id(name):
    value = os.environ.get(name)
    if value is None:
        return None
    if not value.isdigit() or int(value) <= 0:
        raise ValueError(f"{name} must be a positive integer; got {value!r}")
    return value


def query_string(values):
    return urlencode({key: value for key, value in values.items() if value is not None})


def assert_page(test_case, data, description, records_key="records"):
    test_case.assertIsInstance(data, dict, f"{description} must return an object")
    test_case.assertIsInstance(
        data.get(records_key), list, f"{description} must contain {records_key}"
    )
    return data[records_key]


class PublicFeatureApiTests(unittest.TestCase):
    def test_auth_public_profile_and_video_browsing(self):
        public_user_id = fixture_id("API_USER_ID")
        if public_user_id:
            profile = assert_api_success(
                self,
                request(f"/api/auth/user/{public_user_id}"),
                "public user profile",
            )
            self.assertEqual(int(profile["userId"]), int(public_user_id))

        for path, description in (
            ("/api/banner", "visible banners"),
            ("/api/video/categories", "video categories"),
        ):
            with self.subTest(endpoint=path):
                data = assert_api_success(self, request(path), description)
                self.assertIsInstance(data, list)

        page = assert_api_success(
            self,
            request("/api/video/list?pageNum=1&pageSize=5&sort=latest"),
            "latest videos",
        )
        self.assertLessEqual(len(assert_page(self, page, "latest videos")), 5)

        for path, description in (
            ("/api/search/video?pageSize=5", "empty video search"),
            ("/api/search/user?pageSize=5", "empty user search"),
            ("/api/search/post?pageSize=5", "empty post search"),
        ):
            with self.subTest(endpoint=path):
                data = assert_api_success(self, request(path), description)
                assert_page(self, data, description)

        self.assertIsInstance(
            assert_api_success(self, request("/api/search/hot?limit=5"), "hot searches"),
            list,
        )

    def test_public_social_and_user_home_pages(self):
        user_id = fixture_id("API_USER_ID")
        if not user_id:
            self.skipTest("Set API_USER_ID to test user-home APIs")

        query = query_string({"homeUserId": user_id, "pageNum": 1})
        page_routes = (
            (f"/api/home/featured?{query}", "featured videos"),
            (f"/api/home/contribute?{query}", "user contributions"),
            (f"/api/home/dynamic?{query}", "user dynamics"),
            (f"/api/home/dynamicImg?{query}", "user image dynamics"),
            (f"/api/home/relations?homeUserId={user_id}&type=8&pageNum=1", "user follows"),
        )
        for path, description in page_routes:
            with self.subTest(endpoint=path):
                data = assert_api_success(self, request(path), description)
                assert_page(self, data, description)

        for path, description in (
            (f"/api/home/love?{query}", "user liked videos"),
            (f"/api/home/collectsClassify?{query}", "user collection folders"),
        ):
            with self.subTest(endpoint=path):
                data = assert_api_success(self, request(path), description)
                self.assertIsInstance(data, list, description)

        folder_id = fixture_id("API_FOLDER_ID")
        if folder_id:
            collection = assert_api_success(
                self,
                request(f"/api/home/collectByName?{query}&folderId={folder_id}"),
                "videos in collection",
            )
            assert_page(self, collection, "videos in collection")

        user_videos = assert_api_success(
            self,
            request(f"/api/user/{user_id}/videos?size=5"),
            "public user videos",
        )
        self.assertLessEqual(len(assert_page(self, user_videos, "public user videos")), 5)

    def test_public_dynamic_feed(self):
        feed = assert_api_success(
            self,
            request("/api/post/list?size=5"),
            "public dynamic feed",
        )
        self.assertLessEqual(len(assert_page(self, feed, "public dynamic feed")), 5)

    def test_video_detail_comments_and_danmaku(self):
        video_id = fixture_id("API_VIDEO_ID")
        if not video_id:
            self.skipTest("Set API_VIDEO_ID to test video detail APIs")

        detail = assert_api_success(
            self, request(f"/api/video/{video_id}"), "video detail"
        )
        self.assertIsInstance(detail, dict)
        self.assertTrue(detail.get("title"), "video detail should contain a title")

        title = assert_api_success(
            self,
            request(f"/api/video/getVideoTitle/{video_id}"),
            "video title",
        )
        self.assertEqual(title, detail["title"])

        comment_list = assert_api_success(
            self,
            request(f"/api/comment/list?videoId={video_id}&size=5"),
            "video comments",
        )
        self.assertLessEqual(
            len(assert_page(self, comment_list, "video comments")), 5
        )

        danmaku_list = assert_api_success(
            self,
            request(f"/api/danmaku/list?videoId={video_id}&limit=5"),
            "video danmaku",
        )
        self.assertIsInstance(danmaku_list, list)
        self.assertLessEqual(len(danmaku_list), 5)

        snapshot = assert_api_success(
            self,
            request(f"/api/danmaku/snapshot?videoId={video_id}&limit=5"),
            "danmaku snapshot",
        )
        self.assertIsInstance(snapshot, dict)


class AuthenticatedFeatureApiTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        configured = credentials("API")
        if not configured:
            raise unittest.SkipTest("Set API_USERNAME and API_PASSWORD for user APIs")
        cls.username, password = configured
        cls.token = login(cls, cls.username, password)

    @classmethod
    def tearDownClass(cls):
        if getattr(cls, "token", None):
            request("/api/auth/logout", method="POST", token=cls.token)

    def assert_user_api(self, path, description, expected_type=None):
        data = assert_api_success(
            self, request(path, token=self.token), description
        )
        if expected_type is not None:
            self.assertIsInstance(data, expected_type, description)
        return data

    def test_video_and_library_read_apis(self):
        self.assert_user_api("/api/auth/me", "current profile", dict)
        self.assert_user_api("/api/folder/list", "my folders", list)

        for path, description in (
            ("/api/folder/videos?pageNum=1&pageSize=5", "my folder videos"),
            ("/api/watch-later/list?size=5", "watch-later list"),
            ("/api/history/list?size=5", "watch history"),
        ):
            with self.subTest(endpoint=path):
                result = self.assert_user_api(path, description, dict)
                assert_page(self, result, description)

        video_id = fixture_id("API_VIDEO_ID")
        if video_id:
            state = self.assert_user_api(
                f"/api/video/{video_id}/state", "video interaction state", dict
            )
            self.assertIsInstance(state, dict)
            self.assert_user_api(
                f"/api/video/{video_id}/folders", "folders containing video", list
            )

        uploaded = self.assert_user_api(
            "/api/video/userUploadVideo?videoStatus=-1&pageNum=1",
            "my uploaded videos",
            dict,
        )
        self.assertIsInstance(uploaded, dict)

        stats = self.assert_user_api(
            "/api/video/selectVideoData",
            "my publishing statistics",
            dict,
        )
        self.assertIsInstance(stats, dict)

    def test_social_notifications_messages_and_share_recipients(self):
        routes = (
            ("/api/notification/mentions?size=5", "mentions", dict),
            ("/api/notification/unread", "unread notification count", dict),
            ("/api/comment/selectReplayComment?pageNum=1", "comment replies", list),
            ("/api/comment/selectAt?pageNum=1", "comment mentions", list),
            ("/api/comment/selectLike?pageNum=1", "comment likes", list),
            ("/api/user/mentions", "user mentions", dict),
            ("/api/post/authors", "dynamic feed authors", dict),
            ("/api/relation/list?type=follow&size=5", "following list", dict),
            ("/api/privateMessage/selectDialogue?pageNum=1", "message dialogues", list),
            ("/api/share/recipients", "share recipients", dict),
        )
        for path, description, expected_type in routes:
            with self.subTest(endpoint=path):
                self.assert_user_api(path, description, expected_type)

        unread = assert_api_success(
            self,
            request(
                f"/api/privateMessage/selectNotReadNumber?userId="
                f"{self._current_user_id()}",
                token=self.token,
            ),
            "private-message unread count",
        )
        self.assertIsInstance(unread, int)

    def _current_user_id(self):
        profile = self.assert_user_api("/api/auth/me", "current profile", dict)
        return profile["userId"]

    def test_optional_fixture_video_and_dialogue_apis(self):
        own_video_id = fixture_id("API_OWN_VIDEO_ID")
        if own_video_id:
            detail = self.assert_user_api(
                f"/api/video/getVideoInfo?videoId={own_video_id}",
                "editable video detail",
                dict,
            )
            self.assertIsInstance(detail, dict)

        watch_history = assert_api_success(
            self,
            request(
                "/api/watch-together/history",
                headers={"Authorization": self.token},
            ),
            "watch-together history",
        )
        self.assertIsInstance(watch_history, list)

        dialogue_id = fixture_id("API_DIALOGUE_ID")
        if dialogue_id:
            self.assert_user_api(
                f"/api/privateMessage/selectMessage?dialogueId={dialogue_id}&pageNum=1",
                "messages in dialogue",
                dict,
            )

    @unittest.skipUnless(
        os.environ.get("API_RUN_WRITE_TESTS") == "1",
        "Set API_RUN_WRITE_TESTS=1 to run the create/read/rename/delete folder workflow",
    )
    def test_folder_create_read_rename_delete_round_trip(self):
        folder_id = None
        original_name = f"api-test-{uuid.uuid4().hex[:12]}"
        renamed_name = f"{original_name}-renamed"
        try:
            created = assert_api_success(
                self,
                request(
                    "/api/folder",
                    method="POST",
                    token=self.token,
                    body={
                        "name": original_name,
                        "description": "temporary API automation test folder",
                        "visibility": 0,
                    },
                ),
                "create test folder",
            )
            folder_id = created["id"]
            self.assertEqual(created["name"], original_name)

            listed = self.assert_user_api("/api/folder/list", "verify created folder", list)
            self.assertTrue(any(folder["id"] == folder_id for folder in listed))

            assert_api_success(
                self,
                request(
                    f"/api/folder/{folder_id}",
                    method="PUT",
                    token=self.token,
                    body={
                        "name": renamed_name,
                        "description": "renamed API automation test folder",
                        "visibility": 0,
                    },
                ),
                "rename test folder",
            )
            listed = self.assert_user_api("/api/folder/list", "verify renamed folder", list)
            renamed = next(folder for folder in listed if folder["id"] == folder_id)
            self.assertEqual(renamed["name"], renamed_name)
        finally:
            if folder_id is not None:
                assert_api_success(
                    self,
                    request(
                        f"/api/folder/{folder_id}",
                        method="DELETE",
                        token=self.token,
                    ),
                    "clean up test folder",
                )
                remaining = self.assert_user_api(
                    "/api/folder/list", "verify test folder cleanup", list
                )
                self.assertFalse(
                    any(folder["id"] == folder_id for folder in remaining),
                    "the test folder must be removed after the workflow",
                )


class AdminFeatureApiTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        configured = credentials("API_ADMIN")
        if not configured:
            raise unittest.SkipTest(
                "Set API_ADMIN_USERNAME and API_ADMIN_PASSWORD for admin APIs"
            )
        username, password = configured
        cls.token = login(cls, username, password)

    @classmethod
    def tearDownClass(cls):
        if getattr(cls, "token", None):
            request("/api/auth/logout", method="POST", token=cls.token)

    def test_admin_read_only_management_apis(self):
        overview = assert_api_success(
            self,
            request("/api/system/overview/getOverview", token=self.token),
            "admin dashboard overview",
        )
        self.assertIsInstance(overview, dict)

        user_search = assert_api_success(
            self,
            request(
                "/api/system/user/searchUsers",
                method="POST",
                token=self.token,
                body={"pageNum": 1, "keyword": "", "type": -1, "loginFlag": -1},
            ),
            "user management list",
        )
        records = assert_page(self, user_search, "user management list")
        for row in records:
            self.assertIn("adminFlag", row)
            self.assertIn("superAdminFlag", row)

        post_routes = (
            ("/api/system/content/searchComments", "comment moderation list"),
            ("/api/system/content/searchDynamics", "dynamic moderation list"),
            ("/api/system/content/searchMessages", "message moderation list"),
            ("/api/system/keyWord/searchKeyWords", "keyword management list"),
            ("/api/system/log/searchLogs", "admin operation logs"),
            ("/api/system/video/searchVideos", "video moderation list"),
        )
        empty_query = {
            "pageNum": 1,
            "keyword": "",
            "type": -1,
            "loginFlag": -1,
            "status": -1,
            "sortWay": 0,
        }
        for path, description in post_routes:
            with self.subTest(endpoint=path):
                result = assert_api_success(
                    self,
                    request(
                        path,
                        method="POST",
                        token=self.token,
                        body=empty_query,
                    ),
                    description,
                )
                assert_page(self, result, description)

        bans = assert_api_success(
            self,
            request(
                "/api/system/user/searchBanList?pageNum=1",
                token=self.token,
            ),
            "user ban history",
        )
        assert_page(self, bans, "user ban history")

        categories = assert_api_success(
            self,
            request("/api/system/video/getSubZoneKeys", token=self.token),
            "video moderation categories",
        )
        self.assertIsInstance(categories, list)


class AuthorizationBoundaryApiTests(unittest.TestCase):
    def test_private_and_write_apis_reject_anonymous_requests(self):
        protected_requests = (
            ("GET", "/api/auth/me", None),
            ("PUT", "/api/auth/profile", {"signature": "unauthorized"}),
            ("PUT", "/api/auth/privacy", {"showBirthday": False}),
            ("GET", "/api/folder/list", None),
            ("GET", "/api/watch-later/list", None),
            ("GET", "/api/history/list", None),
            ("GET", "/api/privateMessage/selectDialogue", None),
            ("GET", "/api/notification/mentions", None),
            ("GET", "/api/notification/unread", None),
            ("GET", "/api/share/recipients", None),
            ("POST", "/api/comment", {}),
            ("POST", "/api/danmaku", {}),
            ("POST", "/api/post", {}),
            ("POST", "/api/user/1/follow", None),
            ("POST", "/api/video/1/like", None),
            ("POST", "/api/video/1/favorite", None),
            ("POST", "/api/video/1/watch-later", None),
            ("POST", "/api/privateMessage/sendMessage", {}),
            ("GET", "/api/system/overview/getOverview", None),
            (
                "POST",
                "/api/system/user/searchUsers",
                {"pageNum": 1, "keyword": "", "type": -1, "loginFlag": -1},
            ),
            ("POST", "/api/system/video/searchVideos", {"pageNum": 1}),
            ("GET", "/api/video/userUploadVideo?pageNum=1", None),
            ("POST", "/api/video/selectVideoData", None),
        )
        for method, path, body in protected_requests:
            with self.subTest(method=method, endpoint=path):
                status, payload = request(path, method=method, body=body)
                self.assertTrue(
                    not 200 <= status < 300
                    or not isinstance(payload, dict)
                    or payload.get("code") != 1,
                    f"{method} {path} must reject anonymous requests",
                )


if __name__ == "__main__":
    unittest.main(verbosity=2)
