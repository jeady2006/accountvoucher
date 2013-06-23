package jing.util.excel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class POIWriter {
	public static final String DEFAULT_FOLDER = "export/";
	private String fileName;
	private int currentRow = -1;
	private int currentCell = -1;
	private BufferedOutputStream outputStream;
	private Workbook wb = null;
	private Sheet sheet = null;
	private Row row = null;
	private Cell cell = null;
	private DataFormat dataFormat;
	private CellStyle numbericStyle;
	private List<Sheet> sheets = new ArrayList<Sheet>();

	public POIWriter(String fileName) throws IOException {
		this.fileName = fileName;
		File file = new File(DEFAULT_FOLDER + fileName);
		file.createNewFile();
		outputStream = new BufferedOutputStream(new FileOutputStream(file),
				1024);
		wb = new HSSFWorkbook();
		dataFormat = wb.createDataFormat();
		numbericStyle = wb.createCellStyle();
		numbericStyle.setDataFormat(dataFormat.getFormat("#,###.00"));
	}

	public void createNewSheet(String sheetName) {
		sheet = wb.createSheet(sheetName);
		sheets.add(sheet);
		this.currentCell = -1;
		this.currentRow = -1;
	}

	public void createRow() {
		row = sheet.createRow(++this.currentRow);
		this.currentCell = -1;
	}

	public void createCell() {
		cell = row.createCell(++this.currentCell);
	}

	public void createCell(int i) {
		this.currentCell = i;
		cell = row.createCell(this.currentCell);
	}

	public void skipRow(int i) {
		this.currentRow += i;
	}

	public void skipCell(int i) {
		this.currentCell += i;
	}

	public void setStringData(String data) {
		this.cell.setCellType(Cell.CELL_TYPE_STRING);
		this.cell.setCellValue(data);
	}

	public void setNumbericData(BigDecimal numberic) {
		this.cell.setCellType(Cell.CELL_TYPE_NUMERIC);

		DecimalFormat format = new DecimalFormat("0.00");
		this.cell.setCellValue(Double.valueOf(format.format(numberic
				.doubleValue())));
		this.cell.setCellStyle(numbericStyle);
		// this.cell.setCellValue(1234.34567);
	}

	public void setNextStringData(String data) {
		this.createCell(this.currentCell + 1);
		setStringData(data);
	}

	public void setNextNumbericData(BigDecimal numberic) {
		this.createCell(this.currentCell + 1);
		setNumbericData(numberic);
	}

	public void setNextFormule(String formule) {
		this.createCell(this.currentCell + 1);
		this.cell.setCellType(Cell.CELL_TYPE_FORMULA);
		this.cell.setCellValue(formule);
	}

	public void flush() throws IOException {
		wb.write(outputStream);
	}

	public void destroy() throws IOException {
		if (outputStream != null) {
			outputStream.close();
		}
		outputStream = null;
		cell = null;
		row = null;
		sheet = null;
		sheets.clear();
		sheets = null;
		wb = null;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
