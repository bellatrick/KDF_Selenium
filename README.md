
# Bahmni Keyword-Driven Test Automation Framework

This project implements a Keyword-Driven Test Automation Framework using Java, Selenium WebDriver, TestNG, Apache POI (for Excel interaction), and Bonigarcia's WebDriverManager for testing the Bahmni Open Source HMS demo application.

## Table of Contents

1.  [Purpose](#purpose)
2.  [Features](#features)
3.  [Prerequisites](#prerequisites)
4.  [Project Structure](#project-structure)
5.  [Setup Instructions](#setup-instructions)
6.  [Framework Components](#framework-components)
    *   [BaseTest.java](#basetestjava)
    *   [ExcelReader.java](#excelreaderjava)
    *   [ActionKeywords.java](#actionkeywordsjava)
    *   [TestEngine.java](#testenginejava)
    *   [TestSuite.xlsx](#testsuitexlsx)
7.  [How to Write Test Cases](#how-to-write-test-cases)
    *   [TestCases Sheet](#testcases-sheet)
    *   [TestSteps Sheet](#teststeps-sheet)
    *   [Defining New Keywords](#defining-new-keywords)
8.  [How to Run Tests](#how-to-run-tests)
    *   [Via IDE (IntelliJ IDEA / Eclipse)](#via-ide)
    *   [Via Maven](#via-maven)
    *   [Selecting a Browser](#selecting-a-browser)
9.  [Viewing Reports](#viewing-reports)
10. [Troubleshooting Common Issues](#troubleshooting-common-issues)
11. [Page Object Model (POM) - Future/Integration](#page-object-model-pom---futureintegration)

---

## 1. Purpose

The primary purpose of this framework is to automate the functional testing of the [Bahmni Demo Application](https://demo.standard.mybahmni.in/bahmni/home/index.html#/login). It uses a keyword-driven approach to make test case creation easier for testers, even those with limited programming knowledge, by abstracting complex Selenium code into reusable keywords.

---

## 2. Features

*   **Keyword-Driven:** Test cases are defined in an Excel sheet using predefined keywords.
*   **Data-Driven:** Test data is also managed within the Excel sheets.
*   **Selenium WebDriver:** For browser automation.
*   **Java:** Core programming language.
*   **TestNG:** Testing framework for test execution management, assertions, and reporting.
*   **Apache POI:** For reading data and test steps from Microsoft Excel files.
*   **WebDriverManager (Bonigarcia):** Automates the management of browser drivers (ChromeDriver, GeckoDriver, etc.).
*   **Maven:** For project build and dependency management.
*   **Logging:** SLF4J with Logback for detailed logging of test execution.
*   **Cross-Browser Ready (Basic):** Easily configurable to run on different browsers (Chrome, Firefox, Edge implemented in `BaseTest`).

---

## 3. Prerequisites

Before you begin, ensure you have the following installed:
*   **Java Development Kit (JDK):** Version 11 or higher.
*   **Apache Maven:** For building the project and managing dependencies.
*   **IDE (Optional but Recommended):** IntelliJ IDEA or Eclipse.
*   **Web Browser:** Google Chrome (or Firefox/Edge if you wish to test on them).

---

## 4. Project Structure

```
bahmni-keyword-driven-framework/
├── pom.xml                     # Maven Project Object Model
├── README.md                   # This documentation file
└── src/
├── main/
│   └── java/               # (Currently empty, for non-test framework core code if any)
└── test/
├── java/
│   └── com/
│       └── bahmni/
│           ├── base/             # BaseTest class for WebDriver setup/teardown
│           ├── keywords/         # ActionKeywords class (implements keywords)
│           ├── pages/            # (For Page Object Model classes - future enhancement)
│           ├── testcases/        # TestEngine class (TestNG test executor)
│           └── utils/            # Utility classes (ExcelReader)
└── resources/
├── config/           # (Optional) Configuration files
├── drivers/          # (Not strictly needed due to WebDriverManager)
├── logs/             # (Generated log files, if configured)
├── testdata/
│   └── TestSuite.xlsx # Main Excel file for test cases and steps
├── logback.xml       # Logging configuration
└── testng.xml        # TestNG suite definition file
```

---

## 5. Setup Instructions

1.  **Clone the Repository:**
    ```bash
    git clone <repository-url>
    cd bahmni-keyword-driven-framework
    ```
2.  **Import as Maven Project:**
    *   **IntelliJ IDEA:** `File -> Open...` -> Select the `pom.xml` or the project root directory.
    *   **Eclipse:** `File -> Import... -> Maven -> Existing Maven Projects` -> Browse to the project root.
3.  **Build Project (Optional - IDE often does this automatically):**
    Maven will download all necessary dependencies defined in `pom.xml`.
    ```bash
    mvn clean install -DskipTests
    ```
    (The `-DskipTests` flag prevents tests from running during the build if you're just setting up).

---

## 6. Framework Components

### `BaseTest.java`
*   **Location:** `src/test/java/com/bahmni/base/BaseTest.java`
*   **Purpose:** Manages the WebDriver lifecycle.
    *   `initializeDriver()`: Sets up and launches the specified browser (Chrome by default, can be overridden). Configures implicit waits and page load timeouts.
    *   `quitDriver()`: Closes the browser and quits the WebDriver session.
    *   `getDriver()`: Provides access to the current WebDriver instance.
    *   `navigateToUrl(String url)`: Navigates to the specified URL.

### `ExcelReader.java`
*   **Location:** `src/test/java/com/bahmni/utils/ExcelReader.java`
*   **Purpose:** Provides utility methods to read data from `.xlsx` Excel files.
    *   Reads data from specified sheets and cells.
    *   Methods like `getRowCount()`, `getCellData(row, col)`, `getCellData(row, columnName)`, `getRowData(rowNum)`.
    *   Used by `TestEngine` to read test cases and test steps.

### `ActionKeywords.java`
*   **Location:** `src/test/java/com/bahmni/keywords/ActionKeywords.java`
*   **Purpose:** This is the heart of the keyword implementation. Each public static method in this class represents an "action keyword" that can be used in the `TestSteps` sheet of `TestSuite.xlsx`.
    *   **Example Keywords:** `OPEN_BROWSER`, `NAVIGATE_TO_URL`, `INPUT_TEXT`, `CLICK_ELEMENT`, `VERIFY_TEXT_PRESENT`, `CLOSE_BROWSER`, etc.
    *   These methods use Selenium WebDriver commands to perform actions on the web application.
    *   They accept parameters for locator strategy, locator value, and test data, which are passed from the `TestEngine`.

### `TestEngine.java`
*   **Location:** `src/test/java/com/bahmni/testcases/TestEngine.java`
*   **Purpose:** The main TestNG test class that drives the execution.
    *   Uses `@DataProvider` to read `TestCaseID`s from the `TestCases` sheet in `TestSuite.xlsx` that are marked with `RunMode='Y'`.
    *   For each `TestCaseID`, it iterates through the corresponding steps in the `TestSteps` sheet.
    *   Uses **Java Reflection** to dynamically call the appropriate method in `ActionKeywords.java` based on the `Keyword` specified in the Excel sheet.
    *   Manages overall test case pass/fail status.
    *   Uses TestNG annotations (`@BeforeSuite`, `@AfterSuite`, `@Test`, `@AfterMethod`) to control the test lifecycle.

### `TestSuite.xlsx`
*   **Location:** `src/test/resources/testdata/TestSuite.xlsx`
*   **Purpose:** The central spreadsheet where all test cases and their steps are defined.
    *   **`TestCases` Sheet:** Lists all test cases with their IDs, descriptions, and a `RunMode` flag (`Y/N`) to control execution.
    *   **`TestSteps` Sheet:** Contains detailed steps for each test case. Each row defines a step with a `TestCaseID`, `StepID`, `StepDescription`, `Keyword`, `LocatorStrategy`, `LocatorValue`, and `TestData`.

---

## 7. How to Write Test Cases

Test cases are written entirely within the `TestSuite.xlsx` file.

### `TestCases` Sheet

| TestCaseID | Description                                   | RunMode |
| :--------- | :-------------------------------------------- | :------ |
| LOGIN\_001 | Successful Login to Bahmni                    | Y       |
| REG\_TC\_001 | Register New Patient - Mandatory Fields Only | Y       |
| REG\_TC\_002 | Attempt Registration - Missing Given Name     | N       |

*   **`TestCaseID`**: A unique ID for the test case. This links to steps in the `TestSteps` sheet.
*   **`Description`**: A brief summary of the test case.
*   **`RunMode`**: Set to `Y` to execute the test case, `N` to skip it.

### `TestSteps` Sheet

| TestCaseID | StepID | StepDescription          | Keyword           | LocatorStrategy | LocatorValue                                  | TestData                                                     |
| :--------- | :----- | :----------------------- | :---------------- | :-------------- | :-------------------------------------------- | :----------------------------------------------------------- |
| LOGIN\_001 | 1      | Open Browser             | OPEN\_BROWSER     |                 |                                               | chrome                                                       |
| LOGIN\_001 | 2      | Navigate to Login Page   | NAVIGATE\_TO\_URL |                 |                                               | `https://demo.standard.mybahmni.in/bahmni/home/index.html#/login` |
| LOGIN\_001 | 3      | Enter Username           | INPUT\_TEXT       | id              | username                                      | superman                                                     |
| LOGIN\_001 | 4      | Enter Password           | INPUT_TEXT       | id              | password                                      | Admin123                                                     |
| LOGIN\_001 | 5      | Wait for Location Field  | WAIT\_FOR\_SECONDS|                 |                                               | 3                                                              |
| LOGIN\_001 | 6      | Select Location          | CLICK\_ELEMENT    | xpath           | `(//li[contains(@class,'form-field')])[1]`     |                                                              |
| LOGIN\_001 | 7      | Click Login Button       | CLICK\_ELEMENT    | xpath           | `//button[@type='submit' and contains(.,'Login')]` |                                                              |
| LOGIN\_001 | 8      | Verify Login Success     | VERIFY\_URL\_CONTAINS|                 |                                               | `/home`                                                      |

*   **`TestCaseID`**: Must match an ID from the `TestCases` sheet.
*   **`StepID`**: Sequential number for ordering steps within a test case.
*   **`StepDescription`**: What the step does.
*   **`Keyword`**: The name of the action to perform. This **must** correspond to a public static method name in `ActionKeywords.java` (case is handled by `toUpperCase()` in `TestEngine`, but consistency is good).
*   **`LocatorStrategy`**: How to find the web element (e.g., `id`, `name`, `xpath`, `css`, `linkText`, `className`). Leave blank if the keyword doesn't interact with an element (e.g., `NAVIGATE_TO_URL`).
*   **`LocatorValue`**: The actual value of the locator (e.g., `user-name`, `//button[@id='login']`). Leave blank if not needed.
*   **`TestData`**: The data required by the keyword (e.g., URL for `NAVIGATE_TO_URL`, text for `INPUT_TEXT`, value to select for `SELECT_BY_VISIBLE_TEXT`). Leave blank if not needed.

### Defining New Keywords

1.  Open `ActionKeywords.java` (`src/test/java/com/bahmni/keywords/ActionKeywords.java`).
2.  Add a new `public static void` method. The method name will be your new keyword.
    *   It's recommended to follow the signature: `methodName(String locatorStrategy, String locatorValue, String testData)` even if some parameters are not used by the keyword, for consistency with the `TestEngine`'s reflection call. You can name unused parameters `ignoredStrategy`, etc.
3.  Implement the Selenium logic within this method. Use `driver` (from `ActionKeywords.driver`, set by `TestEngine`) to interact with the browser.
4.  Use `logger.info()` for successful actions and `logger.error()` for failures.
5.  If a step is critical and should fail the test case, throw an exception (e.g., `RuntimeException` or `AssertionError`).
6.  You can now use this new method name (as a string) in the `Keyword` column of your `TestSteps` sheet.

---

## 8. How to Run Tests

### Via IDE (IntelliJ IDEA / Eclipse)

1.  **Using `TestEngine.java`:**
    *   Navigate to `src/test/java/com/bahmni/testcases/TestEngine.java`.
    *   Right-click on the `TestEngine` class name or anywhere inside the class.
    *   Select `Run 'TestEngine'` or `Run As -> TestNG Test`.
2.  **Using `testng.xml`:**
    *   If you have a `testng.xml` file (e.g., in `src/test/resources/`), right-click on it.
    *   Select `Run 'testng.xml'` or `Run As -> TestNG Suite`.

### Via Maven

Open a terminal or command prompt at the root of the project.
*   **To run all tests defined in `testng.xml` (if configured in `pom.xml`'s surefire plugin):**
    ```bash
    mvn clean test
    ```
*   **To run a specific TestNG XML suite file:**
    ```bash
    mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml
    ```

### Selecting a Browser

The framework defaults to Chrome. To run tests on a different browser (e.g., Firefox), you can pass a system property via Maven:

```bash
mvn clean test -Dbrowser=firefox
```
or
```bash
mvn clean test -Dbrowser=edge
```
Ensure `BaseTest.java` supports the browser string you pass.

---

## 9. Viewing Reports

After test execution, TestNG generates reports in the `target/surefire-reports/` directory.
*   **`index.html`**: An HTML report that can be opened in a browser.
*   **`emailable-report.html`**: A more concise HTML report.
*   Console output in your IDE or terminal will also show test results and logs.

---

## 10. Troubleshooting Common Issues

*   **`Log4j2 could not find a logging implementation...`**: This is a warning from Apache POI. It's usually harmless as we use SLF4J/Logback. You can add Log4j2 dependencies if you want to remove the warning (see `pom.xml` comments or previous discussions).
*   **`ExcelReader: File not found` or `InvalidOperationException`**:
    *   Ensure `TestSuite.xlsx` exists at `src/test/resources/testdata/TestSuite.xlsx`.
    *   Verify the file path and sheet names in `TestEngine.java` constants are correct.
    *   Make sure the Excel file is not corrupted and is a `.xlsx` file.
*   **`NoSuchMethodException` in `TestEngine`**:
    *   The `Keyword` in your `TestSteps` sheet does not match any public static method name in `ActionKeywords.java`.
    *   Check for typos, case sensitivity (though `toUpperCase()` is used in `TestEngine`), or incorrect method signature (it expects `(String, String, String)`).
*   **`NoSuchElementException` from `ActionKeywords`**:
    *   The `LocatorStrategy` or `LocatorValue` is incorrect.
    *   The element is not present on the page when Selenium tries to find it.
    *   The page might not have loaded fully; consider adding explicit waits (`WAIT_FOR_SECONDS` keyword, or improve `ActionKeywords` to use more dynamic waits).
*   **Tests Fail with No Obvious Reason**: Check the console logs and `target/surefire-reports/` for detailed error messages. Increase logging levels if necessary.

---

## 11. Page Object Model (POM) - Future/Integration

While this framework is primarily keyword-driven, integrating the Page Object Model (POM) is a good practice for managing web element locators and page-specific methods, especially for larger applications.

*   **How it could work:**
    1.  Create Page Object classes in `src/test/java/com/bahmni/pages/` (e.g., `LoginPage.java`, `RegistrationPage.java`).
    2.  Define `WebElement`s using `@FindBy` annotations or `By` locators within these classes.
    3.  `ActionKeywords` could then be modified or new keywords created to:
        *   Accept a `PageName` and `ElementName` from the Excel sheet.
        *   Use reflection or a dispatcher to get the `WebElement` from the correct Page Object.
        *   Perform actions on that element.
*   **Current State:** Locators are directly specified in the `TestSteps` sheet. The `PageName` column was included in the Excel design as a placeholder for future POM integration.

---
#   K D F _ S e l e n i u m  
 