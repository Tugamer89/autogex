🧪 [Testing Improvement] Add tests for Regex instantiation with invalid syntax

🎯 **What:** Added tests to `RegexTest.java` to verify that `IllegalArgumentException` is thrown when invalid regex strings are passed to the `Regex` constructor (e.g., `"a("`, `""`, `null`).
📊 **Coverage:** Now tests the edge cases of unmatched parenthesis, empty regex strings, and null regex strings being passed to `Regex`.
✨ **Result:** Improved test coverage and reliability for invalid regex syntax handling.
