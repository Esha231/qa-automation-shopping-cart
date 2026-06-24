package com.example.qa;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test once before marking it as truly failed.
 * Selenium suites have a non-zero baseline flake rate (timing,
 * element-not-yet-rendered, etc.) — this absorbs that noise so a
 * single transient failure doesn't fail the whole nightly run.
 *
 * Wire this onto a test/class with: @Test(retryAnalyzer = RetryAnalyzer.class)
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int MAX_RETRY = 1;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            return true;
        }
        return false;
    }
}
