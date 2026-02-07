package api.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("🗣 Starting test: {}", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("✅ Succeeded Test: {}", result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("❌ Failed Test: {}", result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("⚠ Skipped Test: {}", result.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("🗿 Starting Test Suite: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("🏁 Finalizing Test Suite: {} ", context.getName());
    }
}
