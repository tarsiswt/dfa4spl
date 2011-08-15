package br.ufal.cideei.util.count;

import java.io.IOException;

import soot.Body;

public class MetricsSink extends AbstractMetricsSink {

	private MetricsTable table;

	public MetricsSink(MetricsTable table) {
		this.table = table;
	}

	@Override
	protected void handle(Body body, String property, String value) {
		table.setProperty(body.getMethod().getSignature(), property, value);
	}
	
	protected void handle(Body body, String property, double value) {
		table.setProperty(body.getMethod().getSignature(), property, value);
	}
	
	public void terminate() {
		try {
			table.dumpEntriesAndClose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}