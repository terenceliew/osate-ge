package org.osate.ge.internal.ui.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.services.UiService;
import org.osate.ge.internal.ui.util.SelectionUtil;

class AgeHandlerUtil {
	// Returns the current selection as diagram elements.
	// If one or more of the selected objects cannot be adapted to DiagramElement then an empty list is returned.
	public static List<DiagramElement> getSelectedDiagramElements(final ExecutionEvent event) {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		return SelectionUtil.getSelectedDiagramElements(selection);
	}

	public static DiagramElement getPrimaryDiagramElement(final List<DiagramElement> elements) {
		if (elements.size() == 0) {
			return null;
		}

		return elements.get(elements.size() - 1);
	}

	public static void activateTool(final ExecutionEvent event, final Object tool) {
		final IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		final UiService uiService = Adapters.adapt(editorPart, UiService.class);
		if (uiService == null) {
			throw new RuntimeException("Unable to get UiService");
		}

		uiService.activateTool(tool);
	}

	public static List<DiagramElement> getSelectedDiagramElementsFromContext(final Object evaluationContext) {
		final ISelection selection = getSelectionFromContext(evaluationContext);
		return SelectionUtil.getSelectedDiagramElements(selection);
	}

	public static ISelection getSelectionFromContext(final Object evaluationContext) {
		if (!(evaluationContext instanceof IEvaluationContext)) {
			return StructuredSelection.EMPTY;
		}

		final IEvaluationContext context = (IEvaluationContext) evaluationContext;
		final Object selectionObj = context.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (!(selectionObj instanceof ISelection)) {
			return StructuredSelection.EMPTY;
		}

		return (ISelection) selectionObj;
	}

	public static IEditorPart getActiveEditorFromContext(final Object evaluationContext) {
		if (!(evaluationContext instanceof IEvaluationContext)) {
			return null;
		}

		final IEvaluationContext context = (IEvaluationContext) evaluationContext;
		final Object editorObj = context.getVariable(ISources.ACTIVE_EDITOR_NAME);
		return editorObj instanceof IEditorPart ? (IEditorPart) editorObj : null;
	}
}
