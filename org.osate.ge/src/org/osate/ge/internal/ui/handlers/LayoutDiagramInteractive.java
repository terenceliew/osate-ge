package org.osate.ge.internal.ui.handlers;

import java.util.Objects;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osate.ge.internal.elk.LayoutOptionsBuilder;
import org.osate.ge.internal.elk.LayoutUtil;

public class LayoutDiagramInteractive extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart editorPart = Objects.requireNonNull(HandlerUtil.getActiveEditor(event),
				"unable to retrieve active editor");
		LayoutUtil.layout("Layout Diagram", editorPart, null, new LayoutOptionsBuilder().interactive().build());
		return null;
	}
}
