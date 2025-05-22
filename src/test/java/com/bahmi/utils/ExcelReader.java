package com.bahmi.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ExcelReader {

    private static final Logger logger =  LoggerFactory.getLogger(ExcelReader.class);
    private Workbook workbook;
    private Sheet sheet;
    private String filePath;
    private String sheetName;

    public ExcelReader(String filePath, String sheetName){
        this.filePath= filePath;
        this.sheetName= sheetName;

        File excelFile = new File(filePath);

        try(FileInputStream fis= new FileInputStream(excelFile)){
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheet(sheetName);

            if(sheet==null){
                logger.error("Sheet '{}' not found in the work book '{}'", sheetName,filePath);
                throw new IllegalArgumentException("Sheet '"+ sheetName + "' not found in the workbook '"+ filePath + "'.");

            }
            logger.info("Successfully loaded Excel file: {} and sheet {}", filePath,sheetName);


        }catch(IOException e){
            logger.error("Error reading excel file: {} and sheet: {}. Error:{}", filePath,sheetName, e.getMessage(),e);
            throw new RuntimeException("Error reading excel file:"+ filePath,e);
        }
    }
    /**
     * @return The number of data rows.
     */
    public int getRowCount() {
        if (sheet == null) return 0;
        return sheet.getLastRowNum();

    }
    /**
     * @return The total number of rows.
     */
    public int getTotalRowCount() {
        if (sheet == null) return 0;
        return sheet.getPhysicalNumberOfRows();
    }

    /**
     * @param rowNum     The row number (0-based, where row 0 is the header).
     * @param columnName The name of the column (case-insensitive).
     * @return The cell data as a String, or an empty string if the cell is blank or column not found.
     */
    public String getCellData(int rowNum, String columnName) {
        if (sheet == null) return "";
        if (rowNum < 0) {
            logger.warn("Row number cannot be negative: {}", rowNum);
            return "";
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            logger.warn("Header row (row 0) not found in sheet: {}", sheetName);
            return "";
        }

        int colNum = -1;
        for (Cell cell : headerRow) {
            if (cell.getCellType() == CellType.STRING &&
                    columnName.trim().equalsIgnoreCase(cell.getStringCellValue().trim())) {
                colNum = cell.getColumnIndex();
                break;
            }
        }

        if (colNum == -1) {
            logger.warn("Column '{}' not found in sheet '{}'", columnName, sheetName);
            return "";
        }

        return getCellData(rowNum, colNum);
    }

    /**
     * @param rowNum The row number (0-based).
     * @param colNum The column index (0-based).
     * @return The cell data as a String, or an empty string if the cell is blank or out of bounds.
     */
    public String getCellData(int rowNum, int colNum) {
        if (sheet == null) return "";
        if (rowNum < 0 || colNum < 0) {
            logger.warn("Row number ({}) or column number ({}) cannot be negative.", rowNum, colNum);
            return "";
        }

        try {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                // logger.trace("Row {} is null in sheet '{}'", rowNum, sheetName);
                return "";
            }
            Cell cell = row.getCell(colNum, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (cell == null) {
                return "";
            }

            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {

                        return cell.getDateCellValue().toString();
                    } else {

                        DataFormatter formatter = new DataFormatter();
                        return formatter.formatCellValue(cell).trim();
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue()).trim();
                case FORMULA:
                    // Evaluate formula and return the result as a string
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(cell);
                    switch (cellValue.getCellType()) {
                        case STRING:
                            return cellValue.getStringValue().trim();
                        case NUMERIC:
                            DataFormatter formatter = new DataFormatter();
                            return formatter.formatCellValue(cell, evaluator).trim();
                        case BOOLEAN:
                            return String.valueOf(cellValue.getBooleanValue()).trim();
                        default:
                            return "";
                    }
                default:
                    return "";
            }
        } catch (Exception e) {
            logger.error("Error getting cell data from row {}, col {} in sheet '{}'. Error: {}", rowNum, colNum, sheetName, e.getMessage());
            return "";
        }
    }

    /**
     * @param rowNum The row number (0-based, data rows start from 1).
     * @return A Map where keys are column names and values are cell data. Returns an empty map if rowNum is invalid or header not found.
     */
    public Map<String, String> getRowData(int rowNum) {
        Map<String, String> rowData = new HashMap<>();
        if (sheet == null || rowNum <= 0) { // Data rows start from 1, row 0 is header
            logger.warn("Invalid row number ({}) or sheet is null. Data rows start from 1.", rowNum);
            return rowData;
        }

        Row headerRow = sheet.getRow(0);
        Row dataRow = sheet.getRow(rowNum);

        if (headerRow == null || dataRow == null) {
            logger.warn("Header row or data row {} not found in sheet '{}'.", rowNum, sheetName);
            return rowData;
        }

        for (int colNum = 0; colNum < headerRow.getLastCellNum(); colNum++) {
            Cell headerCell = headerRow.getCell(colNum);
            if (headerCell != null && headerCell.getCellType() == CellType.STRING) {
                String columnName = headerCell.getStringCellValue().trim();
                String cellValue = getCellData(rowNum, colNum);
                rowData.put(columnName, cellValue);
            }
        }
        return rowData;
    }


    public void closeWorkbook() {
        if (workbook != null) {
            try {
                workbook.close();
                logger.info("Workbook '{}' closed successfully.", filePath);
            } catch (IOException e) {
                logger.error("Error closing workbook '{}'. Error: {}", filePath, e.getMessage(), e);
            }
        }
    }

    // Main method for quick testing
    public static void main(String[] args) {
        String testExcelPath = "src/test/resources/testdata/TestSuite.xlsx";
        File excelFile = new File(testExcelPath);
        if (!excelFile.exists()) {
            System.err.println("ERROR: Excel file does not exist at path: " + excelFile.getAbsolutePath());
            return; // Exit if file not found
        }
        if (!excelFile.canRead()) {
            System.err.println("ERROR: Cannot read Excel file at path: " + excelFile.getAbsolutePath());
            return;
        }
        System.out.println("Attempting to read Excel file from: " + excelFile.getAbsolutePath());


        String sheetName = "TestCases";

        try {
            ExcelReader reader = new ExcelReader(testExcelPath, sheetName);
            logger.info("Total data rows (excluding header): {}", reader.getRowCount());
            logger.info("Total physical rows (including header): {}", reader.getTotalRowCount());

            if (reader.getRowCount() > 0) {
                // Assuming headers are "TestCaseID", "Keyword", "Value"
                // And data starts from row 1 (0-indexed for POI, so actual row 2 in Excel)
                logger.info("Data from row 1, column 'Keyword': {}", reader.getCellData(1, "Keyword"));
                logger.info("Data from row 1, column index 2: {}", reader.getCellData(1, 2)); // 3rd column

                logger.info("Data from row 2, column 'TestCaseID': {}", reader.getCellData(2, "TestCaseID"));
                logger.info("Data from row 2, column index 0: {}", reader.getCellData(2, 0)); // 1st column

                Map<String, String> rowDataMap = reader.getRowData(1); // Get data from first data row
                logger.info("Row 1 data as map: {}", rowDataMap);

                if (rowDataMap.containsKey("Value")) {
                    logger.info("Value from row 1 map: {}", rowDataMap.get("Value"));
                }
            }
            reader.closeWorkbook(); // Important to close the workbook
        } catch (Exception e) {
            logger.error("Error in ExcelReader main method: {}", e.getMessage(), e);
        }
    }
}
