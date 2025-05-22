package Bahmi;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class BaseTest {
    public static WebDriver driver;
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    private static final String DEFAULT_BROWSER="chrome";

    private static final long IMPLICIT_WAIT_SECONDS= 10;

    //Bahmi's website page is slow, so I used one minute for page load to avoid flakiness
    private  static final long PAGE_LOAD_TIMEOUT_SECOND=60;

    public static void initializeDriver(){
        if(driver==null){
            String browserName=System.getProperty("browser", DEFAULT_BROWSER);
            logger.info("Initializing WebDriver for browser: {}", browserName);

            switch(browserName.toLowerCase()){
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    driver= new FirefoxDriver();
                    break;

                case "edge":
                    WebDriverManager.edgedriver().setup();
                    driver = new EdgeDriver();
                    break;

                case "chrome":
                default:
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--start-maximized");
                    chromeOptions.addArguments("--disable-extensions");
                    chromeOptions.addArguments("--disable-popup-blocking");
                    driver = new ChromeDriver(chromeOptions);
                    break;
            }

            driver.manage().deleteAllCookies();
            driver.manage().window().maximize();

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SECONDS));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SECOND));
            logger.info("{} browser initialized successfully.", browserName);


        }

        else{
            logger.warn("WebDriver instance already exists. Not re-initializing");
        }

    }

    public static void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver quit successfully.");
            } catch (Exception e) {
                logger.error("Error while quitting WebDriver: {}", e.getMessage(), e);
            } finally {
                driver = null;
            }
        } else {
            logger.info("WebDriver instance is already null. No action taken for quitDriver().");
        }
    }
    public static WebDriver getDriver() {
        if (driver == null) {
            logger.info("WebDriver instance is null. It needs to be initialized before use.");
            throw new IllegalStateException("WebDriver instance is null. Please call initializeDriver() first.");
        }
        return driver;
    }

    public static WebDriver checkDriver(){
        return driver;
    }

    public static void navigateToUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            logger.error("URL cannot be null or empty.");
            throw new IllegalArgumentException("URL cannot be null or empty.");
        }
        try {
            getDriver().get(url);
            logger.info("Navigated to URL: {}", url);
        } catch (Exception e) {
            logger.error("Failed to navigate to URL: {}. Error: {}", url, e.getMessage(), e);
            // Optionally, re-throw or handle as a critical failure
            throw new RuntimeException("Failed to navigate to URL: " + url, e);
        }
    }

}




