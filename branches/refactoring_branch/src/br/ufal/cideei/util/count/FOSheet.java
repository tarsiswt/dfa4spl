package br.ufal.cideei.util.count;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class FOSheet extends SummarySheet {

	private final Benchmark bench;
	private final Workbook workbook;
	private int rd;
	private int uv;
	private int jimplification;

	public FOSheet(Benchmark bench) throws InvalidFormatException, FileNotFoundException, IOException {
		if (!bench.oblivious())
			throw new IllegalArgumentException("Benchmark is not feature-oblivious");
		
		this.bench = bench;
		FileInputStream fileInputStream = new FileInputStream(new File(bench.file()));
		this.workbook = WorkbookFactory.create(fileInputStream);
		fileInputStream.close();

		Sheet someSheet = workbook.getSheetAt(0);
		Row firstRow = someSheet.getRow(0);
		int index = 1;
		Cell cell = firstRow.getCell(index);;
		while (cell != null) {
			String stringCellValue = cell.getStringCellValue();
			cell = firstRow.getCell(index);
			
			if ((stringCellValue.equals("rd"))) {
				this.rd = index - 1;
			} else if ((stringCellValue.equals("uv"))) {
				this.uv = index - 1;
			} else if ((stringCellValue.equals("jimplification"))) {
				this.jimplification = index - 1;
			}
			index++;
		}
	}

	@Override
	public void summary() throws IOException {
List<List<String>> listOfLists = new ArrayList<List<String>>();
		
		List<String> fs_rd_a3 = oneFromEverySheet(workbook, bench.sumFooterRow(), rd);
		fs_rd_a3.add(0, "RD A1");
		listOfLists.add(fs_rd_a3);

		List<String> fs_uv_a3 = oneFromEverySheet(workbook, bench.sumFooterRow(), uv);
		fs_uv_a3.add(0, "UV A1");
		listOfLists.add(fs_uv_a3);

		List<String> jimplification = oneFromEverySheet(workbook, bench.sumFooterRow(), this.jimplification);
		jimplification.add(0, "JIMPLIFICATION");
		listOfLists.add(jimplification);
		
		writeTable(workbook, listOfLists);
		writeWorkbookToFile(workbook, bench);
	}

}
