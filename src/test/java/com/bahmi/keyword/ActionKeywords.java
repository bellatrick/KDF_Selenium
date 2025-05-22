package com.bahmi.keyword;

import Bahmi.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

public class ActionKeywords {

    private static final Logger logger = LoggerFactory.getLogger(ActionKeywords.class);
    public static WebDriver driver;
    private static WebDriverWait wait;

    private static final long EXPLICIT_WAIT_SECONDS = 20;

    public static void setDriver(WebDriver webDriver) {
        driver = webDriver;
        if (driver != null) {
            wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SECONDS));
        } else {
            logger.error("WebDriver provided to ActionKeywords is null. Keywords will fail.");
        }
    }

    public static void OPEN_BROWSER(String locatorStrategy,String locatorValue, String testData_browserName) {
        try {
            BaseTest.initializeDriver();
            setDriver(BaseTest.getDriver());
            logger.info("Browser opened and driver initialized successfully via BaseTest logic.");

        } catch (Exception e) {
            logger.error("Failed to execute keyword OPEN_BROWSER. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Keyword OPEN_BROWSER failed", e);
        }
    }

    public static void NAVIGATE_TO_URL(String locatorStrategy,String locatorValue,String url) {
        try {
            BaseTest.navigateToUrl(url); // Using the method from BaseTest
            logger.info("Keyword NAVIGATE_TO_URL: Navigated to {}", url);
        } catch (Exception e) {
            logger.error("Failed to execute keyword NAVIGATE_TO_URL for {}. Error: {}", url, e.getMessage(), e);
            throw new RuntimeException("Keyword NAVIGATE_TO_URL failed", e);
        }
    }

    public static void INPUT_TEXT(String locatorStrategy, String locatorValue, String textToInput) {
        try {
            WebElement element = findElement(locatorStrategy, locatorValue);
            if (element != null) {
                wait.until(ExpectedConditions.visibilityOf(element));
                element.clear();
                element.sendKeys(textToInput);
                logger.info("Keyword INPUT_TEXT: Entered '{}' into element located by {}='{}'", textToInput, locatorStrategy, locatorValue);
            } else {
                logger.error("Keyword INPUT_TEXT: Element not found with {}='{}'", locatorStrategy, locatorValue);
                throw new NoSuchElementException("Element not found for INPUT_TEXT: " + locatorStrategy + "=" + locatorValue);
            }
        } catch (Exception e) {
            logger.error("Failed to execute keyword INPUT_TEXT on element {}='{}'. Data: '{}'. Error: {}",
                    locatorStrategy, locatorValue, textToInput, e.getMessage(), e);
            throw new RuntimeException("Keyword INPUT_TEXT failed", e);
        }
    }

    public static void CLICK_ELEMENT(String locatorStrategy, String locatorValue, String ignoredTestData) { // TestData often ignored for click
        try {
            WebElement element = findElement(locatorStrategy, locatorValue);
            if (element != null) {
                wait.until(ExpectedConditions.elementToBeClickable(element));
                element.click();
                logger.info("Keyword CLICK_ELEMENT: Clicked on element located by {}='{}'", locatorStrategy, locatorValue);
            } else {
                logger.error("Keyword CLICK_ELEMENT: Element not found with {}='{}'", locatorStrategy, locatorValue);
                throw new NoSuchElementException("Element not found for CLICK_ELEMENT: " + locatorStrategy + "=" + locatorValue);
            }
        } catch (Exception e) {
            logger.error("Failed to execute keyword CLICK_ELEMENT on element {}='{}'. Error: {}",
                    locatorStrategy, locatorValue, e.getMessage(), e);
            throw new RuntimeException("Keyword CLICK_ELEMENT failed", e);
        }
    }

    public static void SELECT_BY_VISIBLE_TEXT(String locatorStrategy, String locatorValue, String textToSelect) {
        try {
            WebElement element = findElement(locatorStrategy, locatorValue);
            if (element != null) {
                wait.until(ExpectedConditions.visibilityOf(element));
                Select select = new Select(element);
                select.selectByVisibleText(textToSelect);
                logger.info("Keyword SELECT_BY_VISIBLE_TEXT: Selected '{}' from dropdown {}='{}'", textToSelect, locatorStrategy, locatorValue);
            } else {
                logger.error("Keyword SELECT_BY_VISIBLE_TEXT: Dropdown element not found with {}='{}'", locatorStrategy, locatorValue);
                throw new NoSuchElementException("Dropdown element not found for SELECT_BY_VISIBLE_TEXT: " + locatorStrategy + "=" + locatorValue);
            }
        } catch (Exception e) {
            logger.error("Failed to execute keyword SELECT_BY_VISIBLE_TEXT on element {}='{}'. Data: '{}'. Error: {}",
                    locatorStrategy, locatorValue, textToSelect, e.getMessage(), e);
            throw new RuntimeException("Keyword SELECT_BY_VISIBLE_TEXT failed", e);
        }
    }



    public static void VERIFY_URL_CONTAINS(String ignoredLocatorStrategy, String ignoredLocatorValue, String expectedUrlSubstring) {
        try {
            boolean result = wait.until(ExpectedConditions.urlContains(expectedUrlSubstring));
            if (result) {
                logger.info("Keyword VERIFY_URL_CONTAINS: Current URL '{}' contains '{}'. Verification PASSED.", driver.getCurrentUrl(), expectedUrlSubstring);
            } else {
                logger.error("Keyword VERIFY_URL_CONTAINS: Current URL '{}' does NOT contain '{}'. Verification FAILED.", driver.getCurrentUrl(), expectedUrlSubstring);
                throw new AssertionError("Verification FAILED: URL does not contain '" + expectedUrlSubstring + "'. Current URL: " + driver.getCurrentUrl());
            }
        } catch (TimeoutException e) {
            logger.error("Keyword VERIFY_URL_CONTAINS: Timed out waiting for URL to contain '{}'. Current URL: '{}'. Error: {}",
                    expectedUrlSubstring, driver.getCurrentUrl(), e.getMessage(), e);
            throw new AssertionError("Verification FAILED (Timeout): URL does not contain '" + expectedUrlSubstring + "'. Current URL: " + driver.getCurrentUrl(), e);
        } catch (Exception e) {
            logger.error("Failed to execute keyword VERIFY_URL_CONTAINS for expected text '{}'. Error: {}",
                    expectedUrlSubstring, e.getMessage(), e);
            throw new RuntimeException("Keyword VERIFY_URL_CONTAINS failed", e);
        }
    }

    public static void VERIFY_TEXT_PRESENT(String ignoredLocatorStrategy, String ignoredLocatorValue, String textToVerify) {
        try {
            String pageSource = driver.getPageSource();
            if (pageSource.contains(textToVerify)) {
                logger.info("Keyword VERIFY_TEXT_PRESENT: Text '{}' found on the page. Verification PASSED.", textToVerify);
            } else {
                logger.error("Keyword VERIFY_TEXT_PRESENT: Text '{}' NOT found on the page. Verification FAILED.", textToVerify);
                throw new AssertionError("Verification FAILED: Text '" + textToVerify + "' not found on page.");
            }
        } catch (Exception e) {
            logger.error("Failed to execute keyword VERIFY_TEXT_PRESENT for text '{}'. Error: {}", textToVerify, e.getMessage(), e);
            throw new RuntimeException("Keyword VERIFY_TEXT_PRESENT failed", e);
        }
    }

    public static void VERIFY_ELEMENT_TEXT(String locatorStrategy, String locatorValue, String expectedText) {
        try {
            WebElement element = findElement(locatorStrategy, locatorValue);
            if (element != null) {
                wait.until(ExpectedConditions.visibilityOf(element));
                String actualText = element.getText().trim();
                if (actualText.equals(expectedText.trim())) {
                    logger.info("Keyword VERIFY_ELEMENT_TEXT: Element {}='{}' has text '{}'. Verification PASSED.", locatorStrategy, locatorValue, expectedText);
                } else {
                    logger.error("Keyword VERIFY_ELEMENT_TEXT: Element {}='{}' - Expected text: '{}', Actual text: '{}'. Verification FAILED.",
                            locatorStrategy, locatorValue, expectedText, actualText);
                    throw new AssertionError("Verification FAILED: Element text mismatch. Expected: '" + expectedText + "', Actual: '" + actualText + "'");
                }
            } else {
                logger.error("Keyword VERIFY_ELEMENT_TEXT: Element not found with {}='{}'", locatorStrategy, locatorValue);
                throw new NoSuchElementException("Element not found for VERIFY_ELEMENT_TEXT: " + locatorStrategy + "=" + locatorValue);
            }
        } catch (Exception e) {
            logger.error("Failed to execute keyword VERIFY_ELEMENT_TEXT for element {}='{}', expected text '{}'. Error: {}",
                    locatorStrategy, locatorValue, expectedText, e.getMessage(), e);
            throw new RuntimeException("Keyword VERIFY_ELEMENT_TEXT failed", e);
        }
    }


    // Keyword: CLOSE_BROWSER
    public static void CLOSE_BROWSER(String ignored1, String ignored2, String ignored3) { // Parameters to match signature, but not used
        try {
            BaseTest.quitDriver(); // Using the method from BaseTest
            logger.info("Keyword CLOSE_BROWSER: Browser closed successfully.");
        } catch (Exception e) {
            logger.error("Failed to execute keyword CLOSE_BROWSER. Error: {}", e.getMessage(), e);
            // Don't re-throw as it might hide other test failures if this is in a teardown
        }
    }

    public static void WAIT_FOR_SECONDS(String ignoredLocatorStrategy, String ignoredLocatorValue, String secondsToWaitStr) {
        try {
            long seconds = Long.parseLong(secondsToWaitStr);
            Thread.sleep(seconds * 1000);
            logger.info("Keyword WAIT_FOR_SECONDS: Waited for {} seconds.", seconds);
        } catch (NumberFormatException e) {
            logger.error("Keyword WAIT_FOR_SECONDS: Invalid number format for seconds: '{}'. Error: {}", secondsToWaitStr, e.getMessage(), e);
            throw new IllegalArgumentException("Invalid number format for WAIT_FOR_SECONDS: " + secondsToWaitStr, e);
        } catch (InterruptedException e) {
            logger.warn("Keyword WAIT_FOR_SECONDS: Thread interrupted during wait. Error: {}", e.getMessage(), e);
            Thread.currentThread().interrupt(); // Preserve interrupt status
        } catch (Exception e) {
            logger.error("Failed to execute keyword WAIT_FOR_SECONDS. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Keyword WAIT_FOR_SECONDS failed", e);
        }
    }


    /**
     * Helper method to find an element based on locator strategy and value.
     * Includes an explicit wait for the element to be present.
     */
    private static WebElement findElement(String locatorStrategy, String locatorValue) {
        if (driver == null) {
            logger.error("WebDriver is null in findElement. Cannot locate element {}='{}'", locatorStrategy, locatorValue);
            throw new IllegalStateException("WebDriver is null. Cannot find element.");
        }
        By locator = getBy(locatorStrategy, locatorValue);
        try {
            // Wait for element to be present in DOM, not necessarily visible or interactable yet
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            logger.warn("Timeout waiting for element presence: {}='{}'. Element might not exist or page not fully loaded.", locatorStrategy, locatorValue);
            return null; // Or throw a custom exception
        } catch (Exception e) {
            logger.error("Exception while finding element {}='{}': {}", locatorStrategy, locatorValue, e.getMessage());
            return null; // Or throw
        }
    }

    /**
     * Helper method to convert locator strategy string to Selenium By object.
     */
    private static By getBy(String locatorStrategy, String locatorValue) {
        switch (locatorStrategy.toLowerCase()) {
            case "id":
                return By.id(locatorValue);
            case "name":
                return By.name(locatorValue);
            case "classname":
                return By.className(locatorValue);
            case "tagname":
                return By.tagName(locatorValue);
            case "linktext":
                return By.linkText(locatorValue);
            case "partiallinktext":
                return By.partialLinkText(locatorValue);
            case "cssselector":
            case "css":
                return By.cssSelector(locatorValue);
            case "xpath":
                return By.xpath(locatorValue);
            default:
                logger.error("Unsupported locator strategy: {}", locatorStrategy);
                throw new IllegalArgumentException("Unsupported locator strategy: " + locatorStrategy);
        }
    }
}