//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class FeatureSensitiveEstimative extends BodyTransformer {

	private static FeatureSensitiveEstimative instance = null;

	private long rdTotal = 0;
	private long uvTotal = 0;
	private long rdTotal2 = 0;
	private long uvTotal2 = 0;
	private long jimplificationTotal = 0;
	
	private Workbook workBook = null;
	private FileOutputStream fileOut = null;
	private String sheetName = "fs-metrics";
	private final short CLASS_COL = 0;
	private final short METHOD_COL = 1;
	private final short CONFIGS_COL = 2;
	private final short RD2_COL = 3;
	private final short UV2_COL = 4;
	private final short RD3_COL = 5;
	private final short UV3_COL = 6;
	private final short JIMPLIFICATION_COL = 7;
	private final short UNITS_COL = 8;

	private int methodCount = 1;
	private int times = 0;
	
	public static FeatureSensitiveEstimative v() {
		if (instance == null)
			instance = new FeatureSensitiveEstimative();
		return instance;
	}
	
	private FeatureSensitiveEstimative() {
		openMetricsFile();
		createNewSheet();
	}

	@Override
	protected void internalTransform(Body body, String phase, Map map) {
		methodCount++;
		
		FeatureTag featureTag = (FeatureTag) body.getTag("FeatureTag");
		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		
		int size = featureTag.size();
		
		long rdAnalysisTime     = profilingTag.getRdAnalysisTime();
		long uvAnalysisTime     = profilingTag.getUvAnalysisTime();
		long rdAnalysisTime2    = profilingTag.getRdAnalysisTime2();
		long uvAnalysisTime2    = profilingTag.getUvAnalysisTime2();
		long jimplificationTime = profilingTag.getJimplificationTime();
		
		this.rdTotal += rdAnalysisTime;
		this.uvTotal += uvAnalysisTime;
		this.rdTotal2 += rdAnalysisTime2;
		this.uvTotal2 += uvAnalysisTime2;
		this.jimplificationTotal += jimplificationTime;
		
		{
			Sheet sheet = workBook.getSheet(sheetName + "-" + times);

			Row methodRow = sheet.createRow(methodCount);
			methodRow.createCell(this.CLASS_COL).setCellValue(body.getMethod().getDeclaringClass().getName());
			methodRow.createCell(this.METHOD_COL).setCellValue(body.getMethod().getName());
			methodRow.createCell(this.CONFIGS_COL).setCellValue(size);
			methodRow.createCell(this.UNITS_COL).setCellValue(body.getUnits().size());

			methodRow.createCell(this.RD2_COL).setCellValue(rdAnalysisTime);
			methodRow.createCell(this.UV2_COL).setCellValue(uvAnalysisTime);
			methodRow.createCell(this.RD3_COL).setCellValue(rdAnalysisTime2);
			methodRow.createCell(this.UV3_COL).setCellValue(uvAnalysisTime2);
			methodRow.createCell(this.JIMPLIFICATION_COL).setCellValue(jimplificationTime);
		}
	}

	public long getRdTotal() {
		return rdTotal;
	}
	
	public long getUvTotal() {
		return uvTotal;
	}
	
	public long getRdTotal2() {
		return rdTotal2;
	}
	
	public long getUvTotal2() {
		return uvTotal2;
	}
	
	public long getJimplificationTotal() {
		return jimplificationTotal;
	}

	public void reset() {
		this.rdTotal = 0;
		this.uvTotal = 0;
		this.rdTotal2 = 0;
		this.uvTotal2 = 0;
		this.jimplificationTotal = 0;
		
		createNewSheet();
	}

	private void openMetricsFile() {
		methodCount = 1;
		workBook = new XSSFWorkbook();
		
		String workBookFilePath = "fs-metrics.xls";
		File file = new File(workBookFilePath);
		try {
			fileOut = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void createNewSheet() {
		methodCount = 1;
		times++;
		
		Sheet sheet = workBook.createSheet(sheetName + "-" + times);
		
		Row headerRow = sheet.createRow((short) 0);
		
		headerRow.createCell(this.CLASS_COL).setCellValue("Class");
		headerRow.createCell(this.METHOD_COL).setCellValue("Method");
		headerRow.createCell(this.CONFIGS_COL).setCellValue("No. Configs");
		headerRow.createCell(this.RD2_COL).setCellValue("RD-2");
		headerRow.createCell(this.UV2_COL).setCellValue("UV-2");
		headerRow.createCell(this.RD3_COL).setCellValue("RD-3");
		headerRow.createCell(this.UV3_COL).setCellValue("UV-3");
		headerRow.createCell(this.JIMPLIFICATION_COL).setCellValue("j12n");
		headerRow.createCell(this.UNITS_COL).setCellValue("Units");
	}
	
	public void closeMetricsFile() {
		try {
			workBook.write(fileOut);
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
//#endif