/*******************************************************************************
 * Copyright (C) 2013 University of Alabama in Huntsville (UAH)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The US Government has unlimited rights in this work in accordance with W31P4Q-10-D-0092 DO 0073.
 *******************************************************************************/
package org.osate.ge.services;

import org.eclipse.graphiti.mm.pictograms.Shape;
import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.Subcomponent;

/**
 * Contains methods for working with AADL Subcomponents
 * @author philip.alldredge
 *
 */
public interface SubcomponentService {
	ComponentClassifier getComponentClassifier(Shape shape, Subcomponent sc);

	ComponentCategory getComponentCategory(Shape shape, Subcomponent sc);

	boolean isImplementation(Shape shape, Subcomponent sc);
}