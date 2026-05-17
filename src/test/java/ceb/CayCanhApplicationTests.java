package ceb;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "ceb.config",
        "ceb.controller",
        "ceb.domain",
        "ceb.exception",
        "ceb.repository",
        "ceb.security",
        "ceb.service"
})
class CayCanhApplicationTests {
}
