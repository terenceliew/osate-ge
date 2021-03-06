package org.osate.ge.internal.ui.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.Dimension;
import org.osate.ge.internal.ui.util.UiUtil;

public class MatchHeightHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final List<DiagramElement> selectedDiagramElements = AgeHandlerUtil.getSelectedDiagramElements(event);
		final AgeDiagram diagram = UiUtil.getDiagram(selectedDiagramElements);
		if (diagram == null) {
			throw new RuntimeException("Unable to get diagram");
		}

		diagram.modify("Match Height", m -> {
			final double height = AgeHandlerUtil.getPrimaryDiagramElement(selectedDiagramElements).getHeight();
			for (final DiagramElement tmpElement : selectedDiagramElements) {
				m.setSize(tmpElement, new Dimension(tmpElement.getWidth(), height));
			}
		});

		return null;
	}
}
