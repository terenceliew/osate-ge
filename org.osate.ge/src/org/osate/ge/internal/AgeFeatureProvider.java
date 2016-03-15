/*******************************************************************************
 * Copyright (C) 2013 University of Alabama in Huntsville (UAH)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The US Government has unlimited rights in this work in accordance with W31P4Q-10-D-0092 DO 0073.
 *******************************************************************************/
package org.osate.ge.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddBendpointFeature;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IDirectEditingFeature;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IMoveBendpointFeature;
import org.eclipse.graphiti.features.IMoveShapeFeature;
import org.eclipse.graphiti.features.IReconnectionFeature;
import org.eclipse.graphiti.features.IRemoveBendpointFeature;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.IResizeShapeFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddBendpointContext;
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.context.IMoveBendpointContext;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.context.IReconnectionContext;
import org.eclipse.graphiti.features.context.IRemoveBendpointContext;
import org.eclipse.graphiti.features.context.IRemoveContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.features.impl.DefaultAddBendpointFeature;
import org.eclipse.graphiti.features.impl.DefaultMoveBendpointFeature;
import org.eclipse.graphiti.features.impl.DefaultRemoveBendpointFeature;
import org.eclipse.graphiti.func.IDelete;
import org.eclipse.graphiti.func.IReconnection;
import org.eclipse.graphiti.func.IUpdate;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.pattern.CreateConnectionFeatureForPattern;
import org.eclipse.graphiti.pattern.DefaultFeatureProviderWithPatterns;
import org.eclipse.graphiti.pattern.IConnectionPattern;
import org.eclipse.graphiti.pattern.IPattern;
import org.eclipse.graphiti.pattern.ReconnectionFeatureForPattern;
import org.eclipse.graphiti.pattern.UpdateFeatureForPattern;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
import org.osate.aadl2.Aadl2Factory;
import org.osate.aadl2.Aadl2Package;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.AccessType;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.DirectionType;
import org.osate.aadl2.Element;
import org.osate.aadl2.FlowKind;
import org.osate.aadl2.FlowSpecification;
import org.osate.aadl2.ModeTransition;
import org.osate.ge.ExtensionPaletteEntry;
import org.osate.ge.ExtensionPaletteEntry.Type;
import org.osate.ge.internal.features.ChangeFeatureTypeFeature;
import org.osate.ge.internal.features.ComponentImplementationToTypeFeature;
import org.osate.ge.internal.features.GoToPackageDiagramFeature;
import org.osate.ge.internal.features.ConfigureInModesFeature;
import org.osate.ge.internal.features.DrillDownFeature;
import org.osate.ge.internal.features.GraphicalToTextualFeature;
import org.osate.ge.internal.features.InstantiateComponentImplementationFeature;
import org.osate.ge.internal.features.LayoutDiagramFeature;
import org.osate.ge.internal.features.PictogramHandlerAddFeature;
import org.osate.ge.internal.features.PictogramHandlerCreateConnectionFeature;
import org.osate.ge.internal.features.PictogramHandlerCreateFeature;
import org.osate.ge.internal.features.PictogramHandlerDeleteFeature;
import org.osate.ge.internal.features.PictogramHandlerDirectEditFeature;
import org.osate.ge.internal.features.PictogramHandlerLayoutFeature;
import org.osate.ge.internal.features.PictogramHandlerUpdateFeature;
import org.osate.ge.internal.features.SwitchDirectionOfConnectionFeature;
import org.osate.ge.internal.features.UpdateLayoutFromClassifierDiagramFeature;
import org.osate.ge.internal.features.RenameModeTransitionFeature;
import org.osate.ge.internal.features.SetDerivedModesFeature;
import org.osate.ge.internal.features.SetDimensionsFeature;
import org.osate.ge.internal.features.SetFeatureClassifierFeature;
import org.osate.ge.internal.features.SetInitialModeFeature;
import org.osate.ge.internal.features.SetModeTransitionTriggersFeature;
import org.osate.ge.internal.features.UpdateClassifierDiagramFeature;
import org.osate.ge.internal.patterns.AgeConnectionPattern;
import org.osate.ge.internal.patterns.AnnexPattern;
import org.osate.ge.internal.patterns.ClassifierPattern;
import org.osate.ge.internal.patterns.FeaturePattern;
import org.osate.ge.internal.patterns.FlowSpecificationPattern;
import org.osate.ge.internal.patterns.ModePattern;
import org.osate.ge.internal.patterns.ModeTransitionPattern;
import org.osate.ge.internal.features.ChangeSubcomponentTypeFeature;
import org.osate.ge.internal.features.EditFlowsFeature;
import org.osate.ge.internal.features.MoveSubprogramCallDownFeature;
import org.osate.ge.internal.features.MoveSubprogramCallUpFeature;
import org.osate.ge.internal.features.RefineConnectionFeature;
import org.osate.ge.internal.features.RefineSubcomponentFeature;
import org.osate.ge.internal.features.RenameConnectionFeature;
import org.osate.ge.internal.features.SetConnectionBidirectionalityFeature;
import org.osate.ge.internal.features.SetSubcomponentClassifierFeature;
import org.osate.ge.internal.patterns.SubprogramCallOrder;
import org.osate.ge.internal.patterns.SubprogramCallOrderPattern;
import org.osate.ge.internal.patterns.SubprogramCallPattern;
import org.osate.ge.internal.patterns.SubprogramCallSequencePattern;
import org.osate.ge.internal.patterns.ConnectionPattern;
import org.osate.ge.internal.features.PackageSetExtendedClassifierFeature;
import org.osate.ge.internal.features.PackageUpdateDiagramFeature;
import org.osate.ge.internal.patterns.PackageClassifierPattern;
import org.osate.ge.internal.patterns.PackageGeneralizationPattern;
import org.osate.ge.internal.features.CreateSimpleFlowSpecificationFeature;
import org.osate.ge.internal.features.RefineFeatureFeature;
import org.osate.ge.internal.features.RefineFlowSpecificationFeature;
import org.osate.ge.internal.features.RenameFlowSpecificationFeature;
import org.osate.ge.internal.features.SetAccessFeatureKindFeature;
import org.osate.ge.internal.features.SetFeatureDirectionFeature;
import org.osate.ge.internal.features.SetFeatureGroupInverseFeature;
import org.osate.ge.di.GetPaletteEntries;
import org.osate.ge.di.Names;
import org.osate.ge.internal.services.AadlModificationService;
import org.osate.ge.internal.services.BusinessObjectResolutionService;
import org.osate.ge.internal.services.CachingService;
import org.osate.ge.internal.services.ConnectionService;
import org.osate.ge.internal.services.DiagramModificationService;
import org.osate.ge.internal.services.ExtensionService;
import org.osate.ge.internal.services.GhostingService;
import org.osate.ge.internal.services.LabelService;
import org.osate.ge.internal.services.PropertyService;
import org.osate.ge.internal.services.ReferenceBuilderService;
import org.osate.ge.internal.services.ShapeCreationService;
import org.osate.ge.internal.services.ShapeService;

public class AgeFeatureProvider extends DefaultFeatureProviderWithPatterns {
	private final boolean enableIndependenceProviderCaching = true;
	private IEclipseContext eclipseContext;
	private ConnectionService connectionService;
	private ExtensionService extService;
	private ReferenceBuilderService refBuilder;
	private GhostingService ghostingService;
	private AadlModificationService aadlModService;
	private ShapeService shapeService;
	private DiagramModificationService diagramModService;
	private LabelService labelService;
	private ShapeCreationService shapeCreationService;
	private BusinessObjectResolutionService bor;
	private PropertyService propertyService;
	private PictogramHandlerDeleteFeature defaultDeleteFeature;
	private PictogramHandlerDirectEditFeature defaultDirectEditFeature;
	private PictogramHandlerLayoutFeature defaultLayoutFeature;
	
	public AgeFeatureProvider(final IDiagramTypeProvider dtp) {
		super(dtp);
	}
	
	public void initialize(final IEclipseContext context) {
		this.eclipseContext = context.createChild();
		this.eclipseContext.set(IFeatureProvider.class, this);
		this.connectionService = Objects.requireNonNull(eclipseContext.get(ConnectionService.class), "unable to get connection service");		
		this.extService = Objects.requireNonNull(eclipseContext.get(ExtensionService.class), "unable to retrieve extension service");
		this.refBuilder = Objects.requireNonNull(eclipseContext.get(ReferenceBuilderService.class), "unable to retrieve reference builder service");
		this.ghostingService = Objects.requireNonNull(context.get(GhostingService.class), "unable to retrieve ghosting service");
		this.aadlModService = Objects.requireNonNull(eclipseContext.get(AadlModificationService.class), "unable to retrieve aadl modification service");
		this.shapeService = Objects.requireNonNull(eclipseContext.get(ShapeService.class), "unable to retrieve shape service");
		this.diagramModService = Objects.requireNonNull(eclipseContext.get(DiagramModificationService.class), "unable to retrieve diagram modification service");
		this.labelService = Objects.requireNonNull(eclipseContext.get(LabelService.class), "unable to retrieve label service");
		this.shapeCreationService = Objects.requireNonNull(eclipseContext.get(ShapeCreationService.class), "unable to retrieve shape creation service");
		this.bor = Objects.requireNonNull(context.get(BusinessObjectResolutionService.class), "unable to retrieve business object resolution service");
		this.propertyService = Objects.requireNonNull(eclipseContext.get(PropertyService.class), "unable to retrieve property service");
		
		final IndependenceProvider nonCachingIndependenceProvider = make(IndependenceProvider.class);
		if(enableIndependenceProviderCaching) {
			final CachingIndependenceProvider cachingIndependenceProvider = new CachingIndependenceProvider(nonCachingIndependenceProvider);
			eclipseContext.get(CachingService.class).registerCache(cachingIndependenceProvider);
			setIndependenceSolver(cachingIndependenceProvider);
		} else {
			setIndependenceSolver(nonCachingIndependenceProvider);
		}
		
		// Add patterns
		addAadlFeaturePatterns();
		addConnectionPattern(make(FlowSpecificationPattern.class));
		addPattern(make(ModePattern.class));
		addConnectionPattern(make(ModeTransitionPattern.class));
		// Package
		addConnectionPattern(make(PackageGeneralizationPattern.class));
		
		addPackageClassifierPatterns();	
		addAadlConnectionPatterns();
		
		// Classifiers
		addPattern(createClassifierPattern(null));
		addSubcomponentPatterns();
		
		addAnnexPatterns();
		
		// Subprogram Calls
		addPattern(make(SubprogramCallSequencePattern.class));
		addPattern(make(SubprogramCallPattern.class));
		addConnectionPattern(make(SubprogramCallOrderPattern.class));
		
		// Create the feature to use for pictograms which do not have a specialized feature. Delegates to pictogram handlers.
		defaultDeleteFeature = make(PictogramHandlerDeleteFeature.class);
		defaultDirectEditFeature = make(PictogramHandlerDirectEditFeature.class);
		defaultLayoutFeature = make(PictogramHandlerLayoutFeature.class);
	}

	@Override
	public void dispose() {
		if(eclipseContext != null) {
			eclipseContext.dispose();
		}
		
		super.dispose();
	}
	

	private IEclipseContext getContext() {
		return eclipseContext;
	}
	
	/**
	 * Instantiates an object and injects the current context into it
	 * @param clazz
	 * @return
	 */
	protected final <T> T make(final Class<T> clazz) {
		return ContextInjectionFactory.make(clazz, eclipseContext);
	}
	
	// Don't allow removing, just deleting.
	@Override 
	public IRemoveFeature getRemoveFeature(final IRemoveContext context) {
		return null;
	}

	// As of 2013-07-03 Graphiti doesn't support connection patterns handling deletes so check if the pattern implements IDeleteFeature and return a feature based on the pattern
	@Override 
	public IDeleteFeature getDeleteFeature(final IDeleteContext context) {
		PictogramElement pictogramElement = context.getPictogramElement();
		if(pictogramElement instanceof Connection) {
			for(final IConnectionPattern conPattern : getConnectionPatterns()) {
				if(conPattern instanceof IDelete) {
					final IDelete deleter = (IDelete)conPattern;
					if(deleter.canDelete(context)) {
						// Create a new feature that wraps the connection pattern
						final IDeleteFeature f = new DefaultDeleteFeature(this) {
							@Override
							public boolean canDelete(IDeleteContext context) {
								return deleter.canDelete(context);
							}

							@Override
							public void preDelete(IDeleteContext context) {
								deleter.preDelete(context);
							}

							@Override
							public void delete(IDeleteContext context) {
								deleter.delete(context);
							}

							@Override
							public void postDelete(IDeleteContext context) {
								deleter.postDelete(context);
							}
						};
						
						// Check the Feature
						if (checkFeatureAndContext(f, context)) {
							return f;
						}
					}
				}
			}
		}
		
		return super.getDeleteFeature(context);
	}
	
	@Override
	protected IDeleteFeature getDeleteFeatureAdditional(final IDeleteContext context) {
		return defaultDeleteFeature;
	}
	
	@Override
	public ICustomFeature[] getCustomFeatures(final ICustomContext context) {
		final ArrayList<ICustomFeature> features = new ArrayList<ICustomFeature>();
		addCustomFeatures(features);
		return features.toArray(new ICustomFeature[] {});
	}	
	
	/**
	 * Method used to additively build a list of custom features. Subclasses can override to add additional custom features while including those supported by parent classes.
	 * @param features
	 */
	protected void addCustomFeatures(final List<ICustomFeature> features) {
		features.add(make(DrillDownFeature.class));
		features.add(make(ComponentImplementationToTypeFeature.class));
		features.add(make(GoToPackageDiagramFeature.class));
		features.add(make(GraphicalToTextualFeature.class));
		features.add(make(LayoutDiagramFeature.class));
		features.add(make(InstantiateComponentImplementationFeature.class));
		features.add(make(SwitchDirectionOfConnectionFeature.class));
		features.add(make(UpdateLayoutFromClassifierDiagramFeature.class));
		features.add(make(ConfigureInModesFeature.class));
		features.add(createSetInitialModeFeature(true));
		features.add(createSetInitialModeFeature(false));
		features.add(createSetDerivedModesFeature(true));
		features.add(createSetDerivedModesFeature(false));
		features.add(make(SetModeTransitionTriggersFeature.class));		
		features.add(make(SetFeatureClassifierFeature.class));
		features.add(make(SetDimensionsFeature.class));
		
		for(final EClass featureType : FeaturePattern.getFeatureTypes()) {
			final IEclipseContext childCtx = getContext().createChild();
			try {
				try {
					childCtx.set("Feature Type", featureType);
					features.add(ContextInjectionFactory.make(ChangeFeatureTypeFeature.class, childCtx));
				} finally {
					childCtx.dispose();
				}
			} finally {
				childCtx.dispose();
			}
		}
		
		// Component Implementation
		features.add(make(EditFlowsFeature.class));
		features.add(make(SetSubcomponentClassifierFeature.class));
		features.add(make(RefineSubcomponentFeature.class));
		features.add(make(RefineConnectionFeature.class));
		
		for(final EClass subcomponentType : ClassifierPattern.getSubcomponentTypes()) {
			final IEclipseContext childCtx = getContext().createChild();
			try {
				childCtx.set("Subcomponent Type", subcomponentType);
				features.add(ContextInjectionFactory.make(ChangeSubcomponentTypeFeature.class, childCtx));	
			} finally {
				childCtx.dispose();
			}
		}
		
		features.add(createSetConnectionBidirectionalityFeature(false));
		features.add(createSetConnectionBidirectionalityFeature(true));
		
		// Package
		features.add(make(PackageSetExtendedClassifierFeature.class));
		
		// Type
		features.add(make(RefineFeatureFeature.class));
		features.add(make(RefineFlowSpecificationFeature.class));
		
		features.add(createSetFeatureGroupInverseFeature(true));
		features.add(createSetFeatureGroupInverseFeature(false));
		features.add(createSetFeatureDirectionFeature(DirectionType.IN));
		features.add(createSetFeatureDirectionFeature(DirectionType.OUT));
		features.add(createSetFeatureDirectionFeature(DirectionType.IN_OUT));		
		features.add(createSetFeatureKindFeature(AccessType.PROVIDES));
		features.add(createSetFeatureKindFeature(AccessType.REQUIRES));
		
		// Subprogram Call
		features.add(make(MoveSubprogramCallUpFeature.class));
		features.add(make(MoveSubprogramCallDownFeature.class));
	}
	
	private ICustomFeature createSetInitialModeFeature(final Boolean isInitial) {
		final IEclipseContext childCtx = getContext().createChild();
		try {
			childCtx.set("Is Initial", isInitial);
			return ContextInjectionFactory.make(SetInitialModeFeature.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
	
	private ICustomFeature createSetDerivedModesFeature(final Boolean derivedModes) {
		final IEclipseContext childCtx = getContext().createChild();
		try {
			childCtx.set("Derived Modes", derivedModes);
			return ContextInjectionFactory.make(SetDerivedModesFeature.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
	
	@Override
	public IUpdateFeature getUpdateFeature(IUpdateContext context) {	
		PictogramElement pictogramElement = context.getPictogramElement();
		
		if(pictogramElement instanceof Diagram) {
			final Object bo = bor.getBusinessObjectForPictogramElement(context.getPictogramElement());
			if(bo instanceof Classifier) {
				return make(UpdateClassifierDiagramFeature.class);
			} else if(bo instanceof AadlPackage) {
				return make(PackageUpdateDiagramFeature.class);
			}
		}
		   
		// As of 2013-07-08 Graphiti doesn't support connection patterns handling updates so check if the pattern implements IUpdate and return a feature based on the pattern
		if(pictogramElement instanceof Connection) {
			for(final IConnectionPattern conPattern : getConnectionPatterns()) {
				if(conPattern instanceof IUpdate) {
					if(((IUpdate)conPattern).canUpdate(context)) {
						final IUpdateFeature f = new UpdateFeatureForPattern(this, (IUpdate)conPattern);
						if (checkFeatureAndContext(f, context)) {
							return f;
						}
					}
				}
			}
		}
 
		return super.getUpdateFeature(context);
	}

	// Helper methods to hide the fact that we are wrapping our AADL Elements to hide the fact they are EMF objects from Graphiti. See AadlElementWrapper
	@Override
	public PictogramElement getPictogramElementForBusinessObject(Object businessObject) {
		if(businessObject instanceof Element) {
			businessObject =  new AadlElementWrapper((Element)businessObject);
		}
		
		return super.getPictogramElementForBusinessObject(businessObject);
	}
	
	public PictogramElement[] getAllPictogramElementsForBusinessObject(Object businessObject) {
		if(businessObject instanceof Element) {
			businessObject =  new AadlElementWrapper((Element)businessObject);
		}
		
		return super.getAllPictogramElementsForBusinessObject(businessObject);
	}
	
	private IPattern createFeaturePattern(final EClass featureType) {
		final IEclipseContext childCtx = getContext().createChild();
		try {
			childCtx.set("Feature Type", featureType);
			return ContextInjectionFactory.make(FeaturePattern.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
	
	/**
	 * Creates and adds patterns related to AADL Features
	 */
	protected final void addAadlFeaturePatterns() {
		// Create the feature patterns
		for(final EClass featureType : FeaturePattern.getFeatureTypes()) {
			this.addPattern(createFeaturePattern(featureType));	
		}
	}

	@Override
	protected IDirectEditingFeature getDirectEditingFeatureAdditional(final IDirectEditingContext context) {
		final Object bo = bor.getBusinessObjectForPictogramElement(context.getPictogramElement());			
		if(bo instanceof org.osate.aadl2.Connection) {
			return make(RenameConnectionFeature.class);
		} else if(bo instanceof ModeTransition) {
			return make(RenameModeTransitionFeature.class);
		} else if(bo instanceof FlowSpecification) {
			return make(RenameFlowSpecificationFeature.class);
		} else {	
			return defaultDirectEditFeature;
		}
	}
	
	@Override
	protected ICreateFeature[] getCreateFeaturesAdditional() {
		final IContext ctx = new CreateContext();
		final List<ICreateFeature> features = new ArrayList<>();
		addIfAvailable(features, createCreateSimpleFlowSpecificationFeature(FlowKind.SOURCE), ctx);
		addIfAvailable(features, createCreateSimpleFlowSpecificationFeature(FlowKind.SINK), ctx);
		
		final IEclipseContext childCtx = createGetPaletteEntriesContext();
		try {
			for(final Object pictogramHandler : extService.getPictogramHandlers()) {
				final ExtensionPaletteEntry[] extPaletteEntries = (ExtensionPaletteEntry[])ContextInjectionFactory.invoke(pictogramHandler, GetPaletteEntries.class, childCtx, null);
				if(extPaletteEntries != null) {
					for(final ExtensionPaletteEntry entry : extPaletteEntries) {
						if(entry.getType() == Type.CREATE) {
							features.add(new PictogramHandlerCreateFeature(bor, extService, aadlModService, shapeService, this, entry, pictogramHandler));
						}
					}
				}
			}		
		} finally {
			childCtx.dispose();
		}
		
		return features.toArray(new ICreateFeature[0]);
	}
			
	@Override
	protected IAddFeature getAddFeatureAdditional(final IAddContext addCtx) {
		if(addCtx instanceof IAddConnectionContext) {
			// TODO: Support connections
		} else {
			final Object pictogramHandler = extService.getApplicablePictogramHandler(AadlElementWrapper.unwrap(addCtx.getNewObject()));
			if(pictogramHandler != null) {
				return new PictogramHandlerAddFeature(extService, ghostingService, labelService, shapeCreationService, shapeService, propertyService, this, pictogramHandler);
			}
		}

		return super.getAddFeatureAdditional(addCtx);
	}
	
	@Override
	protected IUpdateFeature getUpdateFeatureAdditional(final IUpdateContext updateCtx) {
		final PictogramElement pe = updateCtx.getPictogramElement(); 
		if(pe instanceof Shape) {
			final Object pictogramHandler = extService.getApplicablePictogramHandler(bor.getBusinessObjectForPictogramElement(pe));
			if(pictogramHandler != null) {
				return new PictogramHandlerUpdateFeature(extService, refBuilder, ghostingService, bor, labelService, shapeCreationService, shapeService, propertyService, this, pictogramHandler);
			}
		} else if(pe instanceof Connection) {
			// TODO: Support connections
		}
		
		return super.getUpdateFeatureAdditional(updateCtx);
	}
	
	private void addIfAvailable(final List<ICreateFeature> features, final ICreateFeature feature, final IContext context) {
		if(feature.isAvailable(context)) {
			features.add(feature);
		}
	}
	
	/**
	 * Override of getCreateConnectionFeatures() that allow connection patterns to be hidden by implementing isPaletteApplicable()
	 * As of 2014-09-18 Graphiti's connection pattern interface does not contain such a mechanism.
	 */
	@Override
	public ICreateConnectionFeature[] getCreateConnectionFeatures() {
		final ICreateConnectionFeature[] ret = new ICreateConnectionFeature[0];
		final List<ICreateConnectionFeature> retList = new ArrayList<ICreateConnectionFeature>();

		for (IConnectionPattern conPattern : getConnectionPatterns()) {
			if(conPattern instanceof AgeConnectionPattern) {
				if(((AgeConnectionPattern) conPattern).isPaletteApplicable()) {
					retList.add(new CreateConnectionFeatureForPattern(this, conPattern));					
				}
			}
		}
		
		final ICreateConnectionFeature[] a = getCreateConnectionFeaturesAdditional();
		for (ICreateConnectionFeature element : a) {
			retList.add(element);
		}
		
		// Add extension create connection features		
		final IEclipseContext childCtx = createGetPaletteEntriesContext();
		try {
			for(final Object pictogramHandler : extService.getPictogramHandlers()) {
				final ExtensionPaletteEntry[] extPaletteEntries = (ExtensionPaletteEntry[])ContextInjectionFactory.invoke(pictogramHandler, GetPaletteEntries.class, childCtx, null);
				if(extPaletteEntries != null) {
					for(final ExtensionPaletteEntry entry : extPaletteEntries) {
						if(entry.getType() == Type.CREATE_CONNECTION) {
							retList.add(new PictogramHandlerCreateConnectionFeature(extService, aadlModService, diagramModService, this, entry, pictogramHandler));
						}
					}
				}
			}		
		} finally {
			childCtx.dispose();
		}

		return retList.toArray(ret);
	}
	
	@Override
	public IReconnectionFeature getReconnectionFeature(final IReconnectionContext context) {
		for(final IConnectionPattern conPattern : getConnectionPatterns()) {
			if(conPattern instanceof IReconnection) {
				final IReconnection reconnection = (IReconnection)conPattern;
				if(reconnection.canReconnect(context)) {
					final ReconnectionFeatureForPattern f = new ReconnectionFeatureForPattern(this, reconnection);
					if (checkFeatureAndContext(f, context)) {
						return f;
					}
				}
			}
		}
		
		// Disable all other reconnection
		return null;
	 }
	
	// Specialized handling for manipulating bendpoints.
	// Currently only allow editing when working with AadlConnections
	// This will disable manipulating connections associated with flow specifications and other model elements
	
	private final IMoveBendpointFeature moveBendpointFeature = new DefaultMoveBendpointFeature(this) {
		@Override
		public boolean canMoveBendpoint(IMoveBendpointContext context) {
			return allowBendpointManipulation(context.getConnection());
		}
		
		@Override
		public boolean moveBendpoint(final IMoveBendpointContext ctx) {
			boolean result = super.moveBendpoint(ctx);			
			connectionService.createUpdateMidpointAnchor(ctx.getConnection());						
			return result;
		}
	};
	
	@Override 
	public IMoveBendpointFeature getMoveBendpointFeature(final IMoveBendpointContext context) {
		return moveBendpointFeature;
	}
	
	private final IAddBendpointFeature addBendpointFeature = new DefaultAddBendpointFeature(this) {
		@Override
		public boolean canAddBendpoint(IAddBendpointContext context) {
			return allowBendpointManipulation(context.getConnection());
		}
		
		@Override
		public void addBendpoint(final IAddBendpointContext ctx) {
			super.addBendpoint(ctx);			
			connectionService.createUpdateMidpointAnchor(ctx.getConnection());						
		}
	};
	
	@Override 
	public IAddBendpointFeature getAddBendpointFeature(final IAddBendpointContext context) {
		return addBendpointFeature;
	}
	
	private final IRemoveBendpointFeature removeBendpointFeature = new DefaultRemoveBendpointFeature(this) {
		@Override
		public boolean canRemoveBendpoint(IRemoveBendpointContext context) {
			return allowBendpointManipulation(context.getConnection());
		}
		
		@Override
		public void removeBendpoint(final IRemoveBendpointContext ctx) {
			super.removeBendpoint(ctx);			
			connectionService.createUpdateMidpointAnchor(ctx.getConnection());						
		}
	};
	
	@Override 
	public IRemoveBendpointFeature getRemoveBendpointFeature(final IRemoveBendpointContext context) {
		return removeBendpointFeature;
	}

	private boolean allowBendpointManipulation(final PictogramElement pe) {
		final Object bo = bor.getBusinessObjectForPictogramElement(pe);
		return bo instanceof org.osate.aadl2.Connection || bo instanceof org.osate.aadl2.FlowSpecification || bo instanceof SubprogramCallOrder;
	}
	
	// ComponentImplementation
	/**
	 * Creates and adds patterns related to AADL Connections
	 */
	private void addAadlConnectionPatterns() {
		// Create the connection patterns
		for(final EClass connectionType : ConnectionPattern.getConnectionTypes()) {
			addConnectionPattern(createConnectionPattern(connectionType));
		}
	}
	
	private IConnectionPattern createConnectionPattern(final EClass connectionType) {
		final IEclipseContext childCtx = getContext().createChild();
		try {
			childCtx.set("Connection Type", connectionType);
			return ContextInjectionFactory.make(ConnectionPattern.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
	
	private IPattern createClassifierPattern(final EClass scType) {
		final IEclipseContext childCtx = getContext().createChild();
		try {
			childCtx.set("Subcomponent Type", scType);
			return ContextInjectionFactory.make(ClassifierPattern.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
	
	private void addAnnexPatterns() {
		EClass annexType = Aadl2Factory.eINSTANCE.getAadl2Package().getDefaultAnnexLibrary();
		this.addPattern(createAnnexPattern(annexType));
		
		annexType = Aadl2Factory.eINSTANCE.getAadl2Package().getDefaultAnnexSubclause();
		this.addPattern(createAnnexPattern(annexType));
	}
	
	private IPattern createAnnexPattern(final EClass annexType) {
		final IEclipseContext childCtx = getContext().createChild();
		try {
			childCtx.set("Annex Type", annexType);
			return ContextInjectionFactory.make(AnnexPattern.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
		
	/**
	 * Creates and adds patterns related to AADL Features
	 */
	protected final void addSubcomponentPatterns() {
		// Create the subcomponent patterns
		for(final EClass scType : ClassifierPattern.getSubcomponentTypes()) {
			this.addPattern(createClassifierPattern(scType));
		}
	}
	
	private ICustomFeature createSetConnectionBidirectionalityFeature(final Boolean bidirectionalityValue) {
		final IEclipseContext childCtx = getContext().createChild();
		try {
			childCtx.set("Value", bidirectionalityValue);
			return ContextInjectionFactory.make(SetConnectionBidirectionalityFeature.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
	
	// Package
	private void addPackageClassifierPatterns() {
		final Aadl2Package p = Aadl2Factory.eINSTANCE.getAadl2Package();
		addPattern(createPackageClassifierPattern(p.getAbstractType()));
		addPattern(createPackageClassifierPattern(p.getAbstractImplementation()));
		addPattern(createPackageClassifierPattern(p.getBusType()));
		addPattern(createPackageClassifierPattern(p.getBusImplementation()));
		addPattern(createPackageClassifierPattern(p.getDataType()));
		addPattern(createPackageClassifierPattern(p.getDataImplementation()));
		addPattern(createPackageClassifierPattern(p.getDeviceType()));
		addPattern(createPackageClassifierPattern(p.getDeviceImplementation()));
		addPattern(createPackageClassifierPattern(p.getFeatureGroupType()));
		addPattern(createPackageClassifierPattern(p.getMemoryType()));
		addPattern(createPackageClassifierPattern(p.getMemoryImplementation()));
		addPattern(createPackageClassifierPattern(p.getProcessType()));
		addPattern(createPackageClassifierPattern(p.getProcessImplementation()));
		addPattern(createPackageClassifierPattern(p.getProcessorType()));
		addPattern(createPackageClassifierPattern(p.getProcessorImplementation()));
		addPattern(createPackageClassifierPattern(p.getSubprogramType()));
		addPattern(createPackageClassifierPattern(p.getSubprogramImplementation()));
		addPattern(createPackageClassifierPattern(p.getSubprogramGroupType()));
		addPattern(createPackageClassifierPattern(p.getSubprogramGroupImplementation()));
		addPattern(createPackageClassifierPattern(p.getSystemType()));
		addPattern(createPackageClassifierPattern(p.getSystemImplementation()));
		addPattern(createPackageClassifierPattern(p.getThreadType()));
		addPattern(createPackageClassifierPattern(p.getThreadImplementation()));
		addPattern(createPackageClassifierPattern(p.getThreadGroupType()));
		addPattern(createPackageClassifierPattern(p.getThreadGroupImplementation()));
		addPattern(createPackageClassifierPattern(p.getVirtualBusType()));
		addPattern(createPackageClassifierPattern(p.getVirtualBusImplementation()));
		addPattern(createPackageClassifierPattern(p.getVirtualProcessorType()));
		addPattern(createPackageClassifierPattern(p.getVirtualProcessorImplementation()));
	}
	
	private IPattern createPackageClassifierPattern(final EClass classifierType) {
		final IEclipseContext childCtx = getContext().createChild();
		try {
			childCtx.set("Classifier Type", classifierType);
			return ContextInjectionFactory.make(PackageClassifierPattern.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
	
	// Type
	private SetFeatureDirectionFeature createSetFeatureDirectionFeature(final DirectionType dirType) 
	{
		final IEclipseContext childCtx = getContext().createChild();
		try {
			childCtx.set("Direction", dirType);
			return ContextInjectionFactory.make(SetFeatureDirectionFeature.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
	
	private SetFeatureGroupInverseFeature createSetFeatureGroupInverseFeature(final boolean inverse) 
	{
		final IEclipseContext childCtx = getContext().createChild();
		try {
			childCtx.set("Inverse", inverse);
			return ContextInjectionFactory.make(SetFeatureGroupInverseFeature.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
	
	private SetAccessFeatureKindFeature createSetFeatureKindFeature(final AccessType accType) 
	{
		final IEclipseContext childCtx = getContext().createChild();
		try {
			childCtx.set("Access", accType);
			return ContextInjectionFactory.make(SetAccessFeatureKindFeature.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
	
	private CreateSimpleFlowSpecificationFeature createCreateSimpleFlowSpecificationFeature(final FlowKind flowKind) 
	{
		final IEclipseContext childCtx = getContext().createChild();
		try {			
			childCtx.set("Kind", flowKind);
			return ContextInjectionFactory.make(CreateSimpleFlowSpecificationFeature.class, childCtx);
		} finally {
			childCtx.dispose();
		}
	}
	
	private IEclipseContext createGetPaletteEntriesContext() {
		final Object diagramBo = bor.getBusinessObjectForPictogramElement(getDiagramTypeProvider().getDiagram());
		final IEclipseContext childCtx = extService.createChildContext();
		childCtx.set(Names.DIAGRAM_BO, diagramBo);
		return childCtx;
	}
	
	// Don't allow moving transient shapes
	@Override
	protected IMoveShapeFeature getMoveShapeFeatureAdditional(final IMoveShapeContext context) {
		if(propertyService.isTransient(context.getShape())) {
			return null;
		}
		
		return super.getMoveShapeFeatureAdditional(context);
	}
	
	// Don't allow resizing transient shapes
	@Override
	protected IResizeShapeFeature getResizeShapeFeatureAdditional(final IResizeShapeContext context) {
		if(propertyService.isTransient(context.getShape())) {
			return null;
		}
		
		return super.getResizeShapeFeatureAdditional(context);
	}
	
	@Override
	protected ILayoutFeature getLayoutFeatureAdditional(final ILayoutContext context) {
		return defaultLayoutFeature;
	}
}
