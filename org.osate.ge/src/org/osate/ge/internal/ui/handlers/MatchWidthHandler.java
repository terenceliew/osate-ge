package org.osate.ge.internal.ui.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.Dimension;

public class MatchWidthHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final List<DiagramElement> selectedDiagramElements = AgeHandlerUtil.getSelectedDiagramElements(event);
		final AgeDiagram diagram = AgeHandlerUtil.getDiagram(selectedDiagramElements);
		if (diagram == null) {
			throw new RuntimeException("Unable to get diagram");
		}

		diagram.modify("Match Width", m -> {
			final int width = AgeHandlerUtil.getPrimaryDiagramElement(selectedDiagramElements).getWidth();
			for (final DiagramElement tmpElement : selectedDiagramElements) {
				m.setSize(tmpElement, new Dimension(width, tmpElement.getHeight()));
			}
		});

		return null;
	}
}