package com.example.qa;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CartTest {

    WebDriver driver;

    // Defaults assume containers on the same docker-compose network.
    // Override via -D flags for local runs against localhost.
    String baseUrl = System.getProperty("app.url", "http://shopping-cart-app:8080");
    String gridUrl = System.getProperty("grid.url", "http://localhost:4444/wd/hub");

    @BeforeMethod
    public void setUp() throws Exception {
        ChromeOptions options = new ChromeOptions();
        driver = new RemoteWebDriver(new URL(gridUrl), options);
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void addItemToCart() {
        driver.get(baseUrl);
        driver.findElement(By.id("add-to-cart-1")).click();
        String cartText = driver.findElement(By.id("cart-count")).getText();
        Assert.assertTrue(cartText.contains("1"), "Expected cart count to be 1, got: " + cartText);
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (driver != null) {
            if (result.getStatus() == ITestResult.FAILURE) {
                captureScreenshot(result.getName());
            }
            driver.quit();
        }
    }

    private void captureScreenshot(String testName) {
        try {
            File screenshotsDir = new File("target/screenshots");
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs();
            }
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String fileName = testName + "_" + System.currentTimeMillis() + ".png";
            Files.copy(srcFile.toPath(), Paths.get(screenshotsDir.getPath(), fileName));
            System.out.println("Screenshot saved: target/screenshots/" + fileName);
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }
    }
}
