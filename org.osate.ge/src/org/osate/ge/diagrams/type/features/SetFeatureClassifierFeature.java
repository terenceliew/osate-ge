/*******************************************************************************
 * Copyright (C) 2013 University of Alabama in Huntsville (UAH)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The US Government has unlimited rights in this work in accordance with W31P4Q-10-D-0092 DO 0073.
 *******************************************************************************/
package org.osate.ge.diagrams.type.features;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.osate.aadl2.Aadl2Factory;
import org.osate.aadl2.Aadl2Package;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.BusSubcomponentType;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.DataSubcomponentType;
import org.osate.aadl2.Feature;
import org.osate.aadl2.FeaturePrototype;
import org.osate.aadl2.FeatureType;
import org.osate.aadl2.PackageSection;
import org.osate.aadl2.Prototype;
import org.osate.aadl2.SubprogramGroupSubcomponentType;
import org.osate.aadl2.SubprogramSubcomponentType;
import org.osate.ge.dialogs.ElementSelectionDialog;
import org.osate.ge.services.AadlModificationService;
import org.osate.ge.services.BusinessObjectResolutionService;
import org.osate.ge.services.AadlModificationService.AbstractModifier;
import org.osate.xtext.aadl2.properties.util.EMFIndexRetrieval;

public class SetFeatureClassifierFeature extends AbstractCustomFeature {
	private static Map<EClass, FeatureClassifierSetterInfo> featureTypeToClassifierSetterMap = new HashMap<EClass, FeatureClassifierSetterInfo>();

	private final AadlModificationService aadlModService;
	private final BusinessObjectResolutionService bor;
	
	static {
		final Aadl2Package p = Aadl2Factory.eINSTANCE.getAadl2Package();
		featureTypeToClassifierSetterMap.put(p.getBusAccess(), new FeatureClassifierSetterInfo(p.getBusSubcomponentType(), BusSubcomponentType.class, "setBusFeatureClassifier"));
		featureTypeToClassifierSetterMap.put(p.getDataAccess(), new FeatureClassifierSetterInfo(p.getDataSubcomponentType(), DataSubcomponentType.class, "setDataFeatureClassifier"));
		featureTypeToClassifierSetterMap.put(p.getSubprogramAccess(), new FeatureClassifierSetterInfo(p.getSubprogramSubcomponentType(), SubprogramSubcomponentType.class, "setSubprogramFeatureClassifier"));
		featureTypeToClassifierSetterMap.put(p.getSubprogramGroupAccess(), new FeatureClassifierSetterInfo(p.getSubprogramGroupSubcomponentType(), SubprogramGroupSubcomponentType.class, "setSubprogramGroupFeatureClassifier"));
		featureTypeToClassifierSetterMap.put(p.getAbstractFeature(), new FeatureClassifierSetterInfo(p.getFeaturePrototype(), FeaturePrototype.class, "setFeaturePrototype"));
		featureTypeToClassifierSetterMap.put(p.getFeatureGroup(), new FeatureClassifierSetterInfo(p.getFeatureType(), FeatureType.class, "setFeatureType"));
		featureTypeToClassifierSetterMap.put(p.getParameter(), new FeatureClassifierSetterInfo(p.getDataSubcomponentType(), DataSubcomponentType.class, "setDataFeatureClassifier"));
		featureTypeToClassifierSetterMap.put(p.getDataPort(), new FeatureClassifierSetterInfo(p.getDataSubcomponentType(), DataSubcomponentType.class, "setDataFeatureClassifier"));
		featureTypeToClassifierSetterMap.put(p.getEventDataPort(), new FeatureClassifierSetterInfo(p.getDataSubcomponentType(), DataSubcomponentType.class, "setDataFeatureClassifier"));
	}
	
	private static class FeatureClassifierSetterInfo {
		private final EClass classifierEClass;
		private final Class<?> classifierClass;
		private final String setterName;
		
		public FeatureClassifierSetterInfo(final EClass classifierEClass, final Class<?> classifierClass, final String setterName) {
			this.classifierEClass = classifierEClass;
			this.classifierClass = classifierClass;
			this.setterName = setterName;
		}
	}
	
	@Inject
	public SetFeatureClassifierFeature(final AadlModificationService aadlModService, final BusinessObjectResolutionService bor, final IFeatureProvider fp) {
		super(fp);
		this.aadlModService = aadlModService;
		this.bor = bor;
	}

	@Override
    public String getName() {
        return "Set Classifier...";
    }
	
	@Override
	public boolean canUndo(final IContext context) {
		return false;
	}
	
    @Override
	public boolean isAvailable(final IContext context) {
		final ICustomContext customCtx = (ICustomContext)context;
		final PictogramElement[] pes = customCtx.getPictogramElements();		
		if(customCtx.getPictogramElements().length != 1 || !(customCtx.getPictogramElements()[0] instanceof Shape)) {
			return false;
		}

		// Check that the shape represents a feature and that the feature is owned by the classifier represented by the shape's container
		final PictogramElement pe = pes[0];		
		final Object bo = bor.getBusinessObjectForPictogramElement(pe);
		final Object containerBo = bor.getBusinessObjectForPictogramElement(((Shape)pe).getContainer());
		if(!(bo instanceof Feature)) {
			return false;
		}

		final Feature feature = (Feature)bo;
		return feature.getContainingClassifier() == containerBo && featureTypeToClassifierSetterMap.containsKey(feature.eClass());
	}
    
    @Override
    public boolean canExecute(final ICustomContext context) {
    	return true;
    }
        
	@Override
	public void execute(final ICustomContext context) {
		final PictogramElement pe = context.getPictogramElements()[0];
		final Feature feature = (Feature)bor.getBusinessObjectForPictogramElement(pe);

		// Prompt the user for the classifier
		final ElementSelectionDialog dlg = new ElementSelectionDialog(Display.getCurrent().getActiveShell(), "Select a Classifier", "Select a classifier.", getPotentialFeatureClassifiers(feature));
		if(dlg.open() == Dialog.CANCEL) {
			return;
		}

		// Set the classifier
		aadlModService.modify(feature, new AbstractModifier<Feature, Object>() {
			@Override
			public Object modify(final Resource resource, final Feature feature) {
				// Import the package if necessary
				EObject selectedType = (EObject)dlg.getFirstSelectedElement();
				if(selectedType != null) {
					// Resolve the reference
					selectedType = EcoreUtil.resolve(selectedType, resource);
					
					// Import its package if necessary
					final AadlPackage pkg = (AadlPackage)feature.getElementRoot();
					if(selectedType instanceof Classifier && ((Classifier)selectedType).getNamespace() != null && pkg != null) {
						final PackageSection section = pkg.getPublicSection();
						final AadlPackage selectedClassifierPkg = (AadlPackage)((Classifier)selectedType).getNamespace().getOwner();
						if(pkg != selectedClassifierPkg && !section.getImportedUnits().contains(selectedClassifierPkg)) {
							section.getImportedUnits().add(selectedClassifierPkg);
						}
					}
				}				
				
				// Set the classifier
				setFeatureClassifier(feature, dlg.getFirstSelectedElement());
				
				// TODO: Update other diagrams as appropriate once capability is added to DiagramService
				return null;
			}			
		});
	}	
	
	/**
	 * Return a list of EObjectDescriptions and NamedElements for potential subcomponent types for the specified subcomponent
	 * @return
	 */
	private List<Object> getPotentialFeatureClassifiers(final Feature feature) {
		final List<Object> featureClassifiers = new ArrayList<Object>();
		featureClassifiers.add(null);
		
		final FeatureClassifierSetterInfo setterInfo = featureTypeToClassifierSetterMap.get(feature.eClass());
		// Populate the list with valid classifier descriptions
		for(final IEObjectDescription desc : EMFIndexRetrieval.getAllClassifiersOfTypeInWorkspace(setterInfo.classifierEClass)) {
			featureClassifiers.add(desc);
		}
		
		// Add any prototypes that are of the appropriate type
		if(feature.getContainingClassifier() instanceof ComponentClassifier) {
			for(final Prototype p : ((ComponentClassifier)feature.getContainingClassifier()).getAllPrototypes()) {
				if(setterInfo.classifierEClass.isInstance(p)) {
					featureClassifiers.add(p);
				}			
			}		
		}

		return featureClassifiers;
	}
	
	private static void setFeatureClassifier(final Feature feature, final Object classifier) {
		final FeatureClassifierSetterInfo setterInfo = featureTypeToClassifierSetterMap.get(feature.eClass());
		try {
			final Method method = feature.getClass().getMethod(setterInfo.setterName, setterInfo.classifierClass);
			method.invoke(feature, classifier);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}