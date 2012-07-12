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

public class FSLazySheet extends SummarySheet {
	
	private int rd_a5;
	private int rd_a4;
	private int uv_a5;
	private int uv_a4;
	private int instrumentation;
	private int color_table;
	private int jimplification;
	private final Workbook workbook;
	private final Benchmark bench;
	
	public FSLazySheet(Benchmark bench) throws InvalidFormatException, IOException {
		if (!bench.lazy())
			throw new IllegalArgumentException(bench.toString() + " must be lazy");
		
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
			if (stringCellValue.equals("rd (a5)")) {
				this.rd_a5 = index - 1;
			} else if ((stringCellValue.equals("uv (a5)"))) {
				this.uv_a5 = index - 1;
			} else if ((stringCellValue.equals("rd (a3)"))) {
				this.rd_a4 = index - 1;
			} else if ((stringCellValue.equals("uv (a3)"))) {
				this.uv_a4 = index - 1;
			} else if ((stringCellValue.equals("instrumentation"))) {
				this.instrumentation = index - 1;
			} else if ((stringCellValue.equals("color table"))) {
				this.color_table = index - 1;
			} else if ((stringCellValue.equals("jimplification"))) {
				this.jimplification = index - 1;
			}
			index++;
		}
		
	}
	
	public void summary() throws IOException {
		List<List<String>> listOfLists = new ArrayList<List<String>>();
		
		List<String> fs_rd_a3 = oneFromEverySheet(workbook, bench.sumFooterRow(), rd_a5);
		fs_rd_a3.add(0, "RD A5");
		listOfLists.add(fs_rd_a3);

		List<String> fs_rd_a2 = oneFromEverySheet(workbook, bench.sumFooterRow(), rd_a4);
		fs_rd_a2.add(0, "RD A4");
		listOfLists.add(fs_rd_a2);

		List<String> fs_uv_a3 = oneFromEverySheet(workbook, bench.sumFooterRow(), uv_a5);
		fs_uv_a3.add(0, "UV A5");
		listOfLists.add(fs_uv_a3);

		List<String> fs_uv_a2 = oneFromEverySheet(workbook, bench.sumFooterRow(), uv_a4);
		fs_uv_a2.add(0, "UV A4");
		listOfLists.add(fs_uv_a2);

		List<String> instrumentation = oneFromEverySheet(workbook, bench.sumFooterRow(), this.instrumentation);
		instrumentation.add(0, "INSTRUMENTATION");
		listOfLists.add(instrumentation);

		List<String> color_table = oneFromEverySheet(workbook, bench.sumFooterRow(), this.color_table);
		color_table.add(0, "COLOR TABLE");
		listOfLists.add(color_table);

		List<String> jimplification = oneFromEverySheet(workbook, bench.sumFooterRow(), this.jimplification);
		jimplification.add(0, "JIMPLIFICATION");
		listOfLists.add(jimplification);
		
		writeTable(workbook, listOfLists);
		writeWorkbookToFile(workbook, bench);
	}
}