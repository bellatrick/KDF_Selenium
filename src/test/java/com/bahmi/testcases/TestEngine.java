package com.bahmi.testcases;

import Bahmi.BaseTest;
import com.bahmi.keyword.ActionKeywords;
import com.bahmi.utils.ExcelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestEngine {

    private static final Logger logger = LoggerFactory.getLogger(TestEngine.class);
    public ExcelReader testSuiteExcel;
    public ExcelReader testStepsExcel;

    private static final String TEST_SUITE_PATH = "src/test/resources/testdata/TestSuite.xlsx";
    private static final String TEST_CASES_SHEET = "TestCases";
    private static final String TEST_STEPS_SHEET = "TestSteps";

    // Column names from Excel (case-sensitive, ensure they match your Excel file)
    private static final String COL_TEST_CASE_ID = "TestCaseID";
    private static final String COL_RUN_MODE = "RunMode";
    private static final String COL_STEP_KEYWORD = "Keyword";
    private static final String COL_STEP_LOCATOR_STRATEGY = "LocatorStrategy";
    private static final String COL_STEP_LOCATOR_VALUE = "LocatorValue";
    private static final String COL_STEP_TEST_DATA = "TestData";
    private static final String COL_STEP_DESCRIPTION = "StepDescription";


    @BeforeSuite
    public void setUpSuite() {
        logger.info("Test Suite Execution Started.");
        try {
            testSuiteExcel = new ExcelReader(TEST_SUITE_PATH, TEST_CASES_SHEET);

            testStepsExcel = new ExcelReader(TEST_SUITE_PATH, TEST_STEPS_SHEET);
        } catch (Exception e) {
            logger.error("Failed to initialize Excel readers in @BeforeSuite. Aborting tests.", e);
            throw new RuntimeException("CRITICAL: Failed to load test data Excel sheets. " + e.getMessage(), e);
        }
    }


    @DataProvider(name = "keywordDrivenTests")
    public Object[][] getTestCasesToRun() {
        if (testSuiteExcel == null) {
            logger.error("testSuiteExcel is null in getTestCasesToRun. Cannot provide test data.");
            return new Object[0][0]; // Return empty if Excel setup failed
        }
        List<String> testCasesToRun = new ArrayList<>();
        int testCasesRowCount = testSuiteExcel.getRowCount();
        logger.info("Total test cases found in '{}' sheet: {}", TEST_CASES_SHEET, testCasesRowCount);

        for (int i = 1; i <= testCasesRowCount; i++) {
            String runMode = testSuiteExcel.getCellData(i, COL_RUN_MODE);
            String testCaseID = testSuiteExcel.getCellData(i, COL_TEST_CASE_ID);

            if (testCaseID == null || testCaseID.trim().isEmpty()) {
                logger.warn("Skipping row {} in TestCases sheet as TestCaseID is blank.", i);
                continue;
            }
            if ("Y".equalsIgnoreCase(runMode)) {
                testCasesToRun.add(testCaseID);
                logger.info("Test case '{}' marked for execution (RunMode='Y').", testCaseID);
            } else {
                logger.info("Test case '{}' skipped (RunMode='{}').", testCaseID, runMode);
            }
        }

        Object[][] data = new Object[testCasesToRun.size()][1];
        for (int i = 0; i < testCasesToRun.size(); i++) {
            data[i][0] = testCasesToRun.get(i);
        }
        logger.info("DataProvider will run {} test cases.", testCasesToRun.size());
        return data;
    }


    @Test(dataProvider = "keywordDrivenTests")
    public void executeTestCase(String testCaseID) {
        logger.info("================================================================================");
        logger.info("EXECUTING TEST CASE: {}", testCaseID);
        logger.info("================================================================================");
        boolean testCaseResult = true; // Assume pass until a step fails
        try {
            BaseTest.initializeDriver();
            ActionKeywords.setDriver(BaseTest.getDriver()); // Pass driver to ActionKeywords
        } catch (Exception e) {
            logger.error("CRITICAL FAILURE: WebDriver initialization failed for test case: {}. Error: {}", testCaseID, e.getMessage(), e);
            Assert.fail("WebDriver initialization failed for " + testCaseID, e);
            return; // Stop further execution of this test case
        }

        int testStepsRowCount = testStepsExcel.getRowCount();
        boolean stepsFound = false;

        for (int i = 1; i <= testStepsRowCount; i++) { // Starts from 1 as row 0 is header
            String currentTestCaseIDStep = testStepsExcel.getCellData(i, COL_TEST_CASE_ID);

            if (testCaseID.equalsIgnoreCase(currentTestCaseIDStep)) {
                stepsFound = true;
                String stepDescription = testStepsExcel.getCellData(i, COL_STEP_DESCRIPTION);
                String keyword = testStepsExcel.getCellData(i, COL_STEP_KEYWORD);
                String locatorStrategy = testStepsExcel.getCellData(i, COL_STEP_LOCATOR_STRATEGY);
                String locatorValue = testStepsExcel.getCellData(i, COL_STEP_LOCATOR_VALUE);
                String testData = testStepsExcel.getCellData(i, COL_STEP_TEST_DATA);

                logger.info("Executing Step: {} | Keyword: {} | Locator: {}='{}' | Data: '{}'",
                        stepDescription, keyword, locatorStrategy, locatorValue, testData);

                if (keyword == null || keyword.trim().isEmpty()) {
                    logger.warn("Skipping step with empty keyword for TestCaseID: {} at row {}", testCaseID, i);
                    continue;
                }

                try {
                    // Using Java Reflection to call keyword methods
                    Method method = ActionKeywords.class.getMethod(keyword.toUpperCase(), String.class, String.class, String.class);
                    method.invoke(null, locatorStrategy, locatorValue, testData); // null for static method
                    logger.info("Step PASSED: {}", stepDescription);
                } catch (NoSuchMethodException e) {
                    testCaseResult = false;
                    logger.error("Step FAILED: {} - Keyword '{}' not found in ActionKeywords.class. Ensure method exists and has correct signature (String, String, String).", stepDescription, keyword, e);
                    break; // Stop executing further steps for this test case
                } catch (Exception e) { // Catches InvocationTargetException and others
                    testCaseResult = false;
                    // The actual exception from the keyword will be wrapped in InvocationTargetException
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    logger.error("Step FAILED: {} - Keyword '{}' execution failed. Error: {}", stepDescription, keyword, cause.getMessage(), cause);
                    break; // Stop executing further steps for this test case
                }
            }
        }

        if (!stepsFound) {
            logger.warn("No steps found for TestCaseID: {} in TestSteps sheet.", testCaseID);
            // Decide if this is a failure or just a warning
            // Assert.fail("No steps found for TestCaseID: " + testCaseID);
        }

        if (!testCaseResult) {
            Assert.fail("Test Case '" + testCaseID + "' FAILED. Check logs for details.");
        } else {
            logger.info("Test Case '{}' PASSED.", testCaseID);
        }
        logger.info("--------------------------------------------------------------------------------");
    }

    @AfterMethod
    public void tearDownTestMethod(org.testng.ITestResult result) {
        logger.info("Finished executing Test Case: {}", result.getMethod().getMethodName() + " with parameters " + result.getParameters()[0]);
        if (BaseTest.checkDriver() != null) { // Check if driver was initialized
            BaseTest.quitDriver();
            logger.info("Browser closed after test case: {}", result.getParameters()[0]);
        } else {
            logger.warn("Driver was null at the end of test case: {}. Browser might not have been initialized or already closed.", result.getParameters()[0]);
        }
    }

    @AfterSuite
    public void tearDownSuite() {
        logger.info("Test Suite Execution Finished.");
        // Close Excel workbooks if they are open
        if (testSuiteExcel != null) {
            testSuiteExcel.closeWorkbook();
        }
        if (testStepsExcel != null) {
            testStepsExcel.closeWorkbook();
        }
        // BaseTest.quitDriver(); // If driver is managed per suite, quit here. We are doing per method.
    }
}