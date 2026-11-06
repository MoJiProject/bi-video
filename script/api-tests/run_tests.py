import os
import sys
import unittest
from pathlib import Path

from test_api import BASE_URL, check_api_ready


SCRIPT_DIR = Path(__file__).resolve().parent


def validate_credentials():
    for prefix in ("API", "API_ADMIN"):
        username = os.environ.get(f"{prefix}_USERNAME")
        password = os.environ.get(f"{prefix}_PASSWORD")
        if bool(username) != bool(password):
            raise ValueError(
                f"Set both {prefix}_USERNAME and {prefix}_PASSWORD, or leave both unset."
            )


def main():
    try:
        validate_credentials()
        check_api_ready()
    except (AssertionError, RuntimeError, ValueError) as error:
        print(f"[Tests not started] {error}", file=sys.stderr)
        print(
            f"Start the backend and check API_BASE_URL: {BASE_URL.rstrip('/')}",
            file=sys.stderr,
        )
        print(
            "Set API_BASE_URL to override the default http://localhost:8081.",
            file=sys.stderr,
        )
        return 2

    suite = unittest.defaultTestLoader.discover(
        str(SCRIPT_DIR), pattern="test_*.py"
    )
    result = unittest.TextTestRunner(verbosity=2).run(suite)
    return 0 if result.wasSuccessful() else 1


if __name__ == "__main__":
    raise SystemExit(main())
