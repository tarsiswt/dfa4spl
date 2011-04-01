package br.ufal.cideei.soot.count;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class FeatureObliviousEstimative extends BodyTransformer {

	private static FeatureObliviousEstimative instance = null;
	private Workbook workBook = null;
	private FileOutputStream fileOut = null;
	private String sheetName = "method-by-method metrics";
	private final short CLASS_COL = 0;
	private final short METHOD_COL = 1;
	private final short CONFIGS_COL = 2;
	private final short RD_COL = 3;
	private final short UV_COL = 4;
	private final short PP_COL = 5;
	private final short JIMPLIFICATION_COL = 6;
	private int methodCount = 1;
	
	private long rdTotal = 0;
	private long uvTotal = 0;
	private long jimplificationTotal = 0;
	private long preprocessingTotal = 0;

	public static FeatureObliviousEstimative v() {
		if (instance == null)
			instance = new FeatureObliviousEstimative();
		return instance;
	}

	private FeatureObliviousEstimative() {
		init();
	}
	
	protected void init() {
		try {
			methodCount = 1;
			workBook = new XSSFWorkbook();
			Sheet sheet = workBook.createSheet(sheetName);
			String workBookFilePath = System.getProperty("java.home") + File.separator + "workbook.xlsx";
			fileOut = new FileOutputStream(workBookFilePath);

			// CreationHelper createHelper = workBook.getCreationHelper();
			Row headerRow = sheet.createRow((short) 0);

			headerRow.createCell(0).setCellValue("Class");
			headerRow.createCell(1).setCellValue("Method");
			headerRow.createCell(2).setCellValue("No. Configs");
			headerRow.createCell(3).setCellValue("RD");
			headerRow.createCell(4).setCellValue("UV");
			headerRow.createCell(5).setCellValue("PP");
			headerRow.createCell(6).setCellValue("j12n");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		methodCount++;

		FeatureTag featureTag = (FeatureTag) body.getTag("FeatureTag");
		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");

		int size = featureTag.size();
		long rdAnalysisTime = profilingTag.getRdAnalysisTime();
		long uvAnalysisTime = profilingTag.getUvAnalysisTime();
		long jimplificationTime = profilingTag.getJimplificationTime();
		long preprocessingTime = profilingTag.getPreprocessingTime();

		// if contains color
		if (size > 1) {
			long numberOfConfigurations = (long) (Math.log(size) / Math.log(2));

			this.rdTotal += (numberOfConfigurations * rdAnalysisTime);
			this.uvTotal += (numberOfConfigurations * uvAnalysisTime);
			this.jimplificationTotal += (numberOfConfigurations * jimplificationTime);
			this.preprocessingTotal += (numberOfConfigurations * preprocessingTime);

		} else {
			this.rdTotal += rdAnalysisTime;
			this.uvTotal += uvAnalysisTime;
			this.jimplificationTotal += jimplificationTime;
			this.preprocessingTotal += preprocessingTime;
		}
		// write row to excel file
		{
			Sheet sheet = workBook.getSheet(sheetName);
			Row methodRow = sheet.createRow(methodCount);
			methodRow.createCell(this.CLASS_COL);
			methodRow.createCell(this.METHOD_COL);
			methodRow.createCell(this.CONFIGS_COL);
			methodRow.createCell(this.RD_COL);
			methodRow.createCell(this.UV_COL);
			methodRow.createCell(this.PP_COL);
			methodRow.createCell(this.JIMPLIFICATION_COL);
			
		}
	}

	public long getRdTotal() {
		return rdTotal;
	}

	public long getUvTotal() {
		return uvTotal;
	}

	public long getJimplificationTotal() {
		return jimplificationTotal;
	}

	public long getPreprocessingTotal() {
		return preprocessingTotal;
	}

	public void reset() {
		this.rdTotal = 0;
		this.uvTotal = 0;
		this.jimplificationTotal = 0;
		this.preprocessingTotal = 0;
		try {
			workBook.write(fileOut);
			fileOut.close();
			init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}