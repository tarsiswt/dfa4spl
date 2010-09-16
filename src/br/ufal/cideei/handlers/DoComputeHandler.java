package br.ufal.cideei.handlers;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import br.ufal.cideei.algorithms.assignment.AssignmentAlgorithm;
import br.ufal.cideei.algorithms.coa.ChainOfAssignmentAlgorithm;
import br.ufal.cideei.algorithms.declaration.DeclarationAlgorithm;
import br.ufal.cideei.algorithms.unique.UniqueUsesAlgorithm;
import br.ufal.cideei.features.CIDEFeatureExtracter;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.ui.InfoPopup;
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
	public Object execute(ExecutionEvent event) throws ExecutionException {

		/*
		 * In order to perform analyses on the selected code, there are few
		 * thing we need to collect first in order to configure the Soot
		 * framework environment. They are: - Which ASTNodes are in the text
		 * selection - The casspath entry to the package root of the text
		 * selection - The method name which contains the text selection - The
		 * ColoredSourceFile object of the text selection
		 */
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		Shell shell = HandlerUtil.getActiveShellChecked(event);

		if (!(selection instanceof ITextSelection))
			throw new ExecutionException("Not a text selection");

		/*
		 * used to find out the project name and later to create a compilation
		 * unit from it
		 */
		IFile textSelectionFile = (IFile) HandlerUtil.getActiveEditorChecked(event).getEditorInput().getAdapter(IFile.class);

		// used to compute the ASTNodes corresponding to the text selection
		ITextSelection textSelection = (ITextSelection) selection;

		// this visitor will compute the ASTNodes that were selected by the user
		SelectionNodesVisitor selectionNodesVisitor = new SelectionNodesVisitor(textSelection);
		/*
		 * Now we need to create a compilation unit for the file, and then parse
		 * it to generate an AST in which we will perform our analyses.
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
		 * Not yet used. We'll need to query for the colors(features) associated
		 * with the ASTNodes we are analysing. For now, we are only checking for
		 * the lice number.
		 */
		ColoredSourceFile coloredSourceFile = null;

		/*
		 * Some algorithms might need to compare some features related to
		 * ASTNodes. This is the only current implementation and it provides a
		 * way to query for features from CIDE.
		 */
		IFeatureExtracter extracter = new CIDEFeatureExtracter(textSelectionFile);

		try {
			DeclarationAlgorithm declarationAlgorithm = new DeclarationAlgorithm(selectionNodes, jdtCompilationUnit, coloredSourceFile, extracter);
			declarationAlgorithm.execute();
			System.out.println("--Declaration--Start");
			System.out.println(declarationAlgorithm.getMessage());
			// InfoPopup.pop(shell, declarationAlgorithm.getMessage());
			declarationAlgorithm.getMessage();
			System.out.println("--Declaration--End");

//			AssignmentAlgorithm assignmentAlgorithm = new AssignmentAlgorithm(selectionNodes, jdtCompilationUnit, coloredSourceFile);
//			assignmentAlgorithm.sootExecute(textSelectionFile);
//			System.out.println("--Assignment--Start");
//			System.out.println(assignmentAlgorithm.getMessage());
//			System.out.println("--Assignment--End");

//			UniqueUsesAlgorithm uniqueAlgorithm = new UniqueUsesAlgorithm(selectionNodes, jdtCompilationUnit, coloredSourceFile);
//			uniqueAlgorithm.execute();
//			System.out.println("--Unique--Start");
//			System.out.println(uniqueAlgorithm.getMessage());
//			System.out.println("--Unique--End");

			ChainOfAssignmentAlgorithm chainOfAssignmentAlgorithm = new ChainOfAssignmentAlgorithm(selectionNodes, jdtCompilationUnit, coloredSourceFile, extracter);
//			chainOfAssignmentAlgorithm.sootExecute(textSelectionFile);
			chainOfAssignmentAlgorithm.instrument(textSelectionFile);
			System.out.println("--Chain--Start");
			System.out.println(chainOfAssignmentAlgorithm.getMessage());
			System.out.println("--Chain--End");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
}
