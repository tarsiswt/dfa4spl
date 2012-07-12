package br.ufal.cideei.util.count;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

public abstract class SummarySheet {
	
	public final static int ROW_OFFSET = 9;
	public final static int COL_OFFSET = 2;
	
	public static SummarySheet make(Benchmark bench) throws InvalidFormatException, FileNotFoundException, IOException {
		if (bench.oblivious())
			return new FOSheet(bench);
		
		if (bench.lazy())
			return new FSLazySheet(bench);
		else
			return new FSSheet(bench);
	}
	
	protected List<String> oneFromEverySheet(Workbook workbook, int avgFooterRow, int column) {
		List<String> list = new ArrayList<String>();
		int numberOfSheets = workbook.getNumberOfSheets();
		for (int index = 0; index < numberOfSheets; index++) {
			Sheet currentSheet = workbook.getSheetAt(index);
			Row row = currentSheet.getRow(avgFooterRow);
			Cell cell = row.getCell(column);
			CellReference cellReference = new CellReference(cell);
			String sheetFormulaReference = "=$" + currentSheet.getSheetName() + "." + cellReference.formatAsString();
			list.add(sheetFormulaReference);
		}
		return list;
	}
	
	protected String join(List<String> list, String separator) {
		StringBuilder builder = new StringBuilder();
		for (String string : list) {
			builder.append(string).append(separator);
		}
		String joined = builder.toString();
		return joined.substring(0, joined.length() - separator.length());
	}
	
	public abstract void summary() throws IOException;
	
	public static void writeTable(Workbook workbook, List<List<String>> listOfLists) throws IOException {
		Iterator<List<String>> iterator = listOfLists.iterator();
		
		// main data summary
		Sheet summarySheet = workbook.createSheet("summary");
		for (int rowIndex = ROW_OFFSET; iterator.hasNext(); rowIndex++) {
			List<String> rowAsListOfStrings = iterator.next();
			Row row = summarySheet.createRow(rowIndex);
			Iterator<String> valIterator = rowAsListOfStrings.iterator();
			for (int colIndex = COL_OFFSET; valIterator.hasNext(); colIndex++) {
				String value = valIterator.next();
				Cell cell = row.createCell(colIndex);
				if (value.startsWith("="))
					cell.setCellFormula(value.substring(2).replace(".", "!"));
				else 
					cell.setCellValue(value);
			}
		}
		
		// average (outlier-removing) summary
		iterator = listOfLists.iterator();
		for (int rowIndex = ROW_OFFSET; iterator.hasNext(); rowIndex++) {
			List<String> rowAsListOfStrings = iterator.next();
			Row row = summarySheet.getRow(rowIndex);
			Cell avgCell = row.createCell(rowAsListOfStrings.size() + 4);
			
			Cell avgWithoutOutlierCell = row.createCell(rowAsListOfStrings.size() + 3);
			CellReference firstValue = new CellReference(row.getCell(COL_OFFSET + 1));
			CellReference lastValue = new CellReference(row.getCell(COL_OFFSET + rowAsListOfStrings.size() - 1));

			String range = firstValue.formatAsString() + ":" + lastValue.formatAsString();
			
			avgWithoutOutlierCell.setCellFormula("(SUM(" + range + ") - MAX(" + range + ") - MIN(" + range + "))/" + (rowAsListOfStrings.size() - 1 - 2));
			avgCell.setCellFormula("AVERAGE(" + range + ")");
		}
	}
	
	protected void writeWorkbookToFile(Workbook workbook, Benchmark bench) throws IOException {
		FileOutputStream fileInputStream;
		String file = bench.file();
		String fileWithoutExtension = file.substring(0, file.length()-4);
		String fileWithExtenstion = fileWithoutExtension + "-summary" +file.substring(file.length()-4, file.length());
		
		fileInputStream = new FileOutputStream(fileWithExtenstion);
		workbook.write(fileInputStream);
	}
	
}
