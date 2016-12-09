// Based on OSATE Graphical Editor. Modifications are: 
/*
Copyright (c) 2016, Rockwell Collins.
Developed with the sponsorship of Defense Advanced Research Projects Agency (DARPA).

Permission is hereby granted, free of charge, to any person obtaining a copy of this data, 
including any software or models in source or binary form, as well as any drawings, specifications, 
and documentation (collectively "the Data"), to deal in the Data without restriction, including
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Data, and to permit persons to whom the Data is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or 
substantial portions of the Data.

THE DATA IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS, SPONSORS, DEVELOPERS, CONTRIBUTORS, OR COPYRIGHT HOLDERS BE LIABLE 
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
ARISING FROM, OUT OF OR IN CONNECTION WITH THE DATA OR THE USE OR OTHER DEALINGS IN THE DATA.
*/
/*******************************************************************************
 * Copyright (C) 2013 University of Alabama in Huntsville (UAH)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The US Government has unlimited rights in this work in accordance with W31P4Q-10-D-0092 DO 0073.
 *******************************************************************************/
package org.osate.ge.internal.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.osate.ge.internal.services.LayoutService.DockArea;

/**
 * Contains methods for aiding in laying out shapes
 *
 */
public interface LayoutService {		
	enum DockArea {
		LEFT("left"),
		RIGHT("right"),
		TOP("top"),
		BOTTOM("bottom"),
		GROUP("feature_group"); // Container is a group of docked shapes. String "feature_group" is for backwards compatibility purposes
		
		private static final Map<String, DockArea> idToDockAreaMap;
		static {
			final Map<String, DockArea> modifiableMap = new HashMap<String, DockArea>();
			for(final DockArea area : DockArea.values()) {
				modifiableMap.put(area.id, area);
			}
			idToDockAreaMap = Collections.unmodifiableMap(modifiableMap);
		}		
		
		public static DockArea getById(final String dockAreaId) {
			return idToDockAreaMap.get(dockAreaId);
		}
		
		public final String id;
		
		DockArea(final String id) {
			this.id = id;
		}		
	}

	/**
	 * Checks that the shape fits in its container.  If it does not, the container's layout feature is called and the ancestor is checked as well.
	 * @param shape
	 * @return
	 */
	boolean checkShapeBoundsWithAncestors(ContainerShape shape);
	boolean checkShapeBounds(final ContainerShape shape);
	
	void layoutChildren(ContainerShape shape);

	/**
	 * Gets the minimum size of the shape. Taken into account child shapes. Never returns a size smaller than the current size.
	 * The first element is the x-coordinate, the second element is the y-coordinate.
	 * @param shape
	 * @param fp
	 */
	int[] getMinimumSize(ContainerShape shape);
	
	/**
	 * Returns the minimum width and height for a shape layed out in accordance to adjustChildShapePosition
	 * @return
	 */
	int getMinimumWidth();
	int getMinimumHeight();
	
	DockArea getDockArea(Shape shape);

	/**
	 * Builds a mapping between DockArea as returned by getDockArea(String) and a list of child shapes.
	 * @param shape
	 * @return
	 */
	Map<DockArea, List<Shape>> buildDockAreaToChildrenMap(ContainerShape shape, boolean includeUndockedShapes);
	
	void cleanupOverlappingDockedShapes(Map<DockArea, List<Shape>> dockAreaToShapesMap, int yStartOffset);	
}