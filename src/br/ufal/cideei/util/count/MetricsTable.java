package br.ufal.cideei.util.count;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class MetricsTable {

	private final int MAX_SIZE;

	MultiValueMap map = new MultiValueMap();

	/*
	 * Maps int -> String, where int is the column position and the String is
	 * the column name
	 */
	private TreeBidiMap columnMapping;

	/*
	 * Created on the File passed as argument to the ctor.
	 */
	private Workbook workBook;

	/*
	 * Sheet created on the WorkBook.
	 */
	private Sheet sheet;

	/*
	 * Flag to keep track of where the columns headers cells were created.
	 */
	private boolean headersWerePrint = false;

	private int rowCount = 0;

	private final File output;

	public MetricsTable(int max, File output) throws FileNotFoundException, IOException, InvalidFormatException {
		MAX_SIZE = max;
		this.output = output;
		workBook = WorkbookFactory.create(new FileInputStream(output));
		this.sheet = workBook.createSheet();
	}

	public MetricsTable(File output) throws FileNotFoundException, IOException, InvalidFormatException {
		MAX_SIZE = Integer.MAX_VALUE;
		this.output = output;
		if (output.exists()) {
			workBook = WorkbookFactory.create(new FileInputStream(output));
		} else {
			workBook = new HSSFWorkbook();
		}
		this.sheet = workBook.createSheet();
	}

	public void setProperty(String method, String property, String value) {
		map.put(method, new DefaultKeyValue(property, value));
		if (map.size(method) == MAX_SIZE) {
			dumpEntry(method, map.getCollection(method));
			map.remove(method);
		}
	}

	public void setProperty(String method, String property, Double value) {
		map.put(method, new DefaultKeyValue(property, value));
		if (map.size(method) == MAX_SIZE) {
			dumpEntry(method, map.getCollection(method));
			map.remove(method);
		}
	}

	private void dumpEntry(String method, Collection<DefaultKeyValue> properties) {
		if (columnMapping == null) {
			mapColumns(properties);
		}
		if (!headersWerePrint) {
			printHeaders();
			headersWerePrint = true;
		}
		Row entryRow = sheet.createRow(rowCount++);

		Cell methodSignatureCell = entryRow.createCell(0);
		methodSignatureCell.setCellValue(method);

		Iterator<DefaultKeyValue> iterator = properties.iterator();
		while (iterator.hasNext()) {
			DefaultKeyValue nextKeyVal = iterator.next();

			String property = (String) nextKeyVal.getKey();
			Object value = nextKeyVal.getValue();
			Integer columnIndex = (Integer) columnMapping.getKey(property);
			
			if (value instanceof Double) {
				Cell cell = entryRow.createCell(columnIndex);
				cell.setCellValue((Double) value);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				
			} else {
				Cell cell = entryRow.createCell(columnIndex);
				cell.setCellValue((String) value);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			}

		}
	}

	private void printHeaders() {
		MapIterator columnMapIterator = columnMapping.mapIterator();
		Row headerRow = sheet.createRow(0);
		rowCount = 1;

		while (columnMapIterator.hasNext()) {
			Cell headerCell = headerRow.createCell((Integer) columnMapIterator.next());
			headerCell.setCellValue((String) columnMapIterator.getValue());
		}
	}

	private void mapColumns(Collection<DefaultKeyValue> sample) {
		columnMapping = new TreeBidiMap();
		int columnCounter = 1;
		for (DefaultKeyValue keyVal : sample) {
			columnMapping.put(columnCounter++, keyVal.getKey());
		}
	}

	public void dumpEntriesAndClose() throws IOException {
		dumpAllEntries();
		int noOfColumns = columnMapping.keySet().size();
		for (; noOfColumns > 0; noOfColumns--) {
			sheet.autoSizeColumn(noOfColumns);
		}
		FileOutputStream outStream = new FileOutputStream(output);
		workBook.write(outStream);
		outStream.close();
	}

	private void dumpAllEntries() {
		Set entrySet = map.entrySet();
		for (Object entryObj : entrySet) {
			Entry entry = (Entry) entryObj;
			dumpEntry((String) entry.getKey(), (Collection) entry.getValue());
		}
	}

}