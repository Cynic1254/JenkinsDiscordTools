package com.github.cynic1254

class Jenkins {
    static void Init(Object steps) {
        jobBaseName = steps.env.JOB_BASE_NAME
        buildURL = steps.env.BUILD_URL
        buildNumber = steps.env.BUILD_NUMBER
    }

    static String jobBaseName = ""
    static String buildURL = ""
    static Integer buildNumber = -1
}
