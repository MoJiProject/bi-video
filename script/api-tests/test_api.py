import json
import os
import unittest
from urllib.error import HTTPError, URLError
from urllib.parse import urljoin
from urllib.request import Request, urlopen


BASE_URL = os.environ.get("API_BASE_URL", "http://localhost:8081").rstrip("/") + "/"
TIMEOUT_SECONDS = 10
_API_READY = False
_API_ERROR = None


def request(path, method="GET", token=None, body=None, headers=None, timeout=TIMEOUT_SECONDS):
    request_headers = {"Accept": "application/json"}
    request_headers.update(headers or {})
    if token:
        request_headers["token"] = token
    data = None
    if body is not None:
        request_headers["Content-Type"] = "application/json"
        data = json.dumps(body).encode("utf-8")

    http_request = Request(
        urljoin(BASE_URL, path.lstrip("/")),
        data=data,
        headers=request_headers,
        method=method,
    )
    try:
        response = urlopen(http_request, timeout=timeout)
    except HTTPError as error:
        response = error
    except URLError as error:
        raise AssertionError(f"{method} {path} failed: {error.reason}") from error
    except OSError as error:
        raise AssertionError(f"{method} {path} failed: {error}") from error

    with response:
        text = response.read().decode("utf-8")
        try:
            payload = json.loads(text) if text else None
        except json.JSONDecodeError:
            payload = text
        return response.status, payload


def check_api_ready():
    global _API_READY, _API_ERROR
    if _API_READY:
        return
    if _API_ERROR:
        raise RuntimeError(_API_ERROR)

    try:
        status, payload = request("/api/auth/ping", timeout=3)
        if not 200 <= status < 300 or not isinstance(payload, dict) or payload.get("code") != 1:
            raise RuntimeError(
                f"Health check returned HTTP {status} with body {payload!r}"
            )
        _API_READY = True
    except (AssertionError, RuntimeError, ValueError) as error:
        _API_ERROR = str(error)
        raise RuntimeError(_API_ERROR) from error


def setUpModule():
    try:
        check_api_ready()
    except (AssertionError, RuntimeError) as error:
        raise unittest.SkipTest(f"API unavailable; module tests skipped: {error}") from error


def assert_api_success(test_case, result, description):
    status, payload = result
    test_case.assertTrue(
        200 <= status < 300,
        f"{description}: HTTP {status}; body={payload!r}",
    )
    test_case.assertIsInstance(payload, dict, f"{description}: response must be JSON")
    test_case.assertEqual(
        payload.get("code"),
        1,
        f"{description}: API code={payload.get('code')}; message={payload.get('msg')}",
    )
    return payload.get("data")


def credentials(prefix):
    username = os.environ.get(f"{prefix}_USERNAME")
    password = os.environ.get(f"{prefix}_PASSWORD")
    return (username, password) if username and password else None


def login(test_case, username, password):
    result = request(
        "/api/auth/login",
        method="POST",
        body={"username": username, "password": password},
    )
    data = assert_api_success(test_case, result, "login")
    test_case.assertIsInstance(data, dict, "login response must contain token data")
    test_case.assertTrue(data.get("token"), "login response must contain a token")
    return data["token"]


class PublicApiTests(unittest.TestCase):
    def test_anonymous_user_can_browse_public_apis(self):
        ping = assert_api_success(self, request("/api/auth/ping"), "anonymous ping")
        self.assertEqual(ping, "anonymous")

        categories = assert_api_success(
            self, request("/api/video/categories"), "video categories"
        )
        self.assertIsInstance(categories, list)

        videos = assert_api_success(
            self,
            request("/api/video/list?pageNum=1&pageSize=5&sort=latest"),
            "video list",
        )
        self.assertIsInstance(videos, dict)
        self.assertIsInstance(videos.get("records"), list)
        self.assertLessEqual(len(videos["records"]), 5)

        hot_searches = assert_api_success(
            self, request("/api/search/hot?limit=5"), "hot searches"
        )
        self.assertIsInstance(hot_searches, list)
        self.assertLessEqual(len(hot_searches), 5)

    def test_private_messaging_rejects_anonymous_requests(self):
        status, payload = request("/api/privateMessage/selectDialogue?pageNum=1")
        self.assertTrue(
            not 200 <= status < 300 or not isinstance(payload, dict) or payload.get("code") != 1,
            "private messaging must not succeed without a token",
        )


class AuthenticatedApiTests(unittest.TestCase):
    token = None

    @unittest.skipUnless(
        credentials("API"),
        "Set API_USERNAME and API_PASSWORD to run the authenticated flow",
    )
    def test_login_profile_authenticated_api_and_logout(self):
        username, password = credentials("API")
        try:
            self.token = login(self, username, password)

            profile = assert_api_success(
                self, request("/api/auth/me", token=self.token), "current profile"
            )
            self.assertEqual(profile.get("username"), username)

            assert_api_success(
                self,
                request(
                    "/api/privateMessage/selectDialogue?pageNum=1",
                    token=self.token,
                ),
                "authenticated dialogue list",
            )

            assert_api_success(
                self,
                request("/api/auth/logout", method="POST", token=self.token),
                "logout",
            )
            self.token = None
            status, payload = request("/api/auth/me")
            self.assertTrue(
                not 200 <= status < 300
                or not isinstance(payload, dict)
                or payload.get("code") != 1,
                "the logged-out session must no longer access the current profile",
            )
        finally:
            if self.token:
                request("/api/auth/logout", method="POST", token=self.token)
                self.token = None


class AdminApiTests(unittest.TestCase):
    @unittest.skipUnless(
        credentials("API_ADMIN"),
        "Set API_ADMIN_USERNAME and API_ADMIN_PASSWORD to run admin checks",
    )
    def test_admin_access_and_regular_user_denial(self):
        admin_username, admin_password = credentials("API_ADMIN")
        tokens = []
        try:
            admin_token = login(self, admin_username, admin_password)
            tokens.append(admin_token)

            overview = assert_api_success(
                self,
                request("/api/system/overview/getOverview", token=admin_token),
                "admin overview",
            )
            self.assertIsInstance(overview, dict)

            users = assert_api_success(
                self,
                request(
                    "/api/system/user/searchUsers",
                    method="POST",
                    token=admin_token,
                    body={"pageNum": 1, "keyword": "", "type": -1, "loginFlag": -1},
                ),
                "admin user search",
            )
            self.assertIsInstance(users, dict)
            self.assertIsInstance(users.get("records"), list)
            for record in users["records"]:
                self.assertIn("adminFlag", record)
                self.assertIn("superAdminFlag", record)

            user = credentials("API")
            if user:
                user_token = login(self, *user)
                tokens.append(user_token)
                status, payload = request(
                    "/api/system/overview/getOverview", token=user_token
                )
                self.assertTrue(
                    not 200 <= status < 300
                    or not isinstance(payload, dict)
                    or payload.get("code") != 1,
                    "a regular user must not access the admin overview",
                )
        finally:
            for token in tokens:
                request("/api/auth/logout", method="POST", token=token)


if __name__ == "__main__":
    unittest.main(verbosity=2)
