package br.ufal.cideei.ui;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InfoPopup extends PopupDialog {

	private String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public static InfoPopup pop(Shell shell,String content){
		InfoPopup popup = new InfoPopup(shell,PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE,true,false,false,false,false,"Emergent Interface","Analyses results");
		popup.setContent(content);
		popup.open();
		return popup;
	}

	private InfoPopup(Shell parent, int shellStyle, boolean takeFocusOnOpen, boolean persistSize, boolean persistLocation, boolean showDialogMenu, boolean showPersistActions, String titleText, String infoText)  {
		super(parent, shellStyle, takeFocusOnOpen, persistSize, persistLocation, showDialogMenu, showPersistActions, titleText, infoText); 
	}


	/*
	 * Create a text control for showing the info.
	 */
	protected Control createDialogArea(Composite parent) {
		Text text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.NO_FOCUS);
		text.setText(this.content);
		return text;
	}
}
