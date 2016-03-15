package org.osate.ge.internal.services;

import java.util.Collection;
import java.util.List;

public interface ExtensionRegistryService {
	/**
	 * Tools are objects that allow the user to perform a specific action using the editor. Only a single tool may be active at a time and tools should have their own 
	 * user interface to provide feedback to the user. 
	 * The following annotations are used with tools:
	 * Activate - Required
	 * CanActivate - Optional
	 * Deactivate - Required
	 * Description - Required
	 * Icon - Required
	 * Id - Required
	 * SelectionChanged - Optional
	 */
    Collection<Object> getTools();
	
	/**
	 * Returns a list of registered categories for the palette.
	 */
	List<Category> getCategories();
	
	/**
	 * Interface for palette categories
	 */
	public static interface Category {
		String getId();
		String getName();
	}
}
