package br.ufal.cideei.handlers;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import br.ufal.cideei.algorithms.assignment.Assignment;
import br.ufal.cideei.algorithms.declaration.Declaration;
import br.ufal.cideei.visitors.SelectionNodesVisitor;
import de.ovgu.cide.features.source.ColoredSourceFile;

/**
 * Handler for the br.ufal.cideei.commands.DoCompute extension command.
 * 
 * @author Társis
 * 
 */
public class DoComputeHandler extends AbstractHandler implements IHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent ee) throws ExecutionException {

		/*
		 * In order to perform analyses on the selected code, there are few
		 * thing we need to collect first in order to configure the Soot
		 * framework environment. They are: - Which ASTNodes are in the text
		 * selection - The casspath entry to the package root of the text
		 * selection - The method name which contains the text selection - The
		 * ColoredSourceFile object of the text selection
		 */
		Shell shell = HandlerUtil.getActiveShellChecked(ee);
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(ee);

		if (!(selection instanceof ITextSelection))
			throw new ExecutionException("Not a text selection");

		// used to find out the project name and later to create a compilation unit from it
		IFile textSelectionFile = (IFile) HandlerUtil.getActiveEditorChecked(ee).getEditorInput().getAdapter(IFile.class);

		// used to compute the ASTNodes corresponding to the text selection
		ITextSelection textSelection = (ITextSelection) selection;

		
		// this visitor will compute the ASTNodes that were selected by the user
		SelectionNodesVisitor selectionNodesVisitor = new SelectionNodesVisitor(textSelection);
		/*
		 * Now we need to create a compilation unit for the file, and then parse it to
		 * generate an AST in which we will perform our analyses.
		 */
		ICompilationUnit compilationUnit = null;
		CompilationUnit jdtCompilationUnit = null;
		compilationUnit = JavaCore.createCompilationUnitFrom(textSelectionFile);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(compilationUnit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		jdtCompilationUnit = (CompilationUnit) parser.createAST(null);
		jdtCompilationUnit.accept(selectionNodesVisitor);

		Set<ASTNode> selectionNodes = selectionNodesVisitor.getNodes();
		
		/*
		 *  Not yet used. We'll need to query for the colors(features) associated with the ASTNodes we are analysing.
		 *  For now, we are only checking for the lice number. 
		 */
		ColoredSourceFile coloredSourceFile = null;
		
		/*
		 * This is the only algorithm implementated so far.
		 */
		Declaration declarationAlgorithm = new Declaration(selectionNodes, jdtCompilationUnit, coloredSourceFile);
		declarationAlgorithm.execute();
//		declarationAlgorithm.executeWithSoot(textSelectionFile);
		System.out.println("--Declaration--Start");
		System.out.println(declarationAlgorithm.getMessage());
		System.out.println("--Declaration--End");
		
		Assignment assignmentAlgorithm = new Assignment(selectionNodes, jdtCompilationUnit, coloredSourceFile);
//		assignmentAlgorithm.executeWithSoot(textSelectionFile);
		assignmentAlgorithm.execute();
		System.out.println("--Assignment--Start");
		System.out.println(assignmentAlgorithm.getMessage());
		System.out.println("--Assignment--End");

		return null;
	}
}
