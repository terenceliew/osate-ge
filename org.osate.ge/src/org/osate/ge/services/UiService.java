package org.osate.ge.services;

/**
 * Contains methods for interacting with the graphical editor's user interface components.
 * All methods must be called from the display thread
 *
 */
public interface UiService {
	void activateTool(Object tool);
	void deactivateActiveTool();
}
