package edu.uah.rsesc.aadl.age.diagram;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.context.IRemoveContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.context.impl.UpdateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.pattern.DefaultFeatureProviderWithPatterns;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.Element;
import org.osate.aadl2.GroupExtension;
import org.osate.aadl2.ImplementationExtension;
import org.osate.aadl2.Realization;
import org.osate.aadl2.TypeExtension;
import org.osate.xtext.aadl2.properties.util.EMFIndexRetrieval;

import edu.uah.rsesc.aadl.age.features.LayoutDiagramFeature;
import edu.uah.rsesc.aadl.age.features.PackageUpdateDiagramFeature;
import edu.uah.rsesc.aadl.age.features.stub.AddDomainObjectConnectionConnectionFeature;
import edu.uah.rsesc.aadl.age.features.stub.AddDomainObjectFeature;
import edu.uah.rsesc.aadl.age.features.stub.CreateDomainObjectConnectionConnectionFeature;
import edu.uah.rsesc.aadl.age.features.stub.CreateDomainObjectFeature;
import edu.uah.rsesc.aadl.age.features.stub.LayoutDomainObjectFeature;
import edu.uah.rsesc.aadl.age.patterns.PackageClassifierPattern;
import edu.uah.rsesc.aadl.age.patterns.PackageGeneralizationPattern;
import edu.uah.rsesc.aadl.age.xtext.AgeXtextUtil;

public class PackageFeatureProvider extends DefaultFeatureProviderWithPatterns {
	public PackageFeatureProvider(IDiagramTypeProvider dtp) {
		super(dtp);
		addPattern(new PackageClassifierPattern());
		addConnectionPattern(new PackageGeneralizationPattern());
		setIndependenceSolver(new IndependenceProvider());
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}

	// TODO: Remove when deleting is allowed.
	@Override 
	public IRemoveFeature getRemoveFeature(final IRemoveContext context) {
		return null;
	}
	
	@Override 
	public IDeleteFeature getDeleteFeature(final IDeleteContext context) {
		return null;
	}	

	@Override
	public IUpdateFeature getUpdateFeature(IUpdateContext context) {
	   PictogramElement pictogramElement = context.getPictogramElement();
	   if(pictogramElement instanceof Diagram) {
		   return new PackageUpdateDiagramFeature(this);
	   }
	   
	   return super.getUpdateFeature(context);
	 }
	@Override
	public ICustomFeature[] getCustomFeatures(final ICustomContext context) {
		return new ICustomFeature[] { new LayoutDiagramFeature(this) };
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
	
	// TODO: Remove. Hack to handle the fact that getPictogramElement will not use independence solver when working with EObject.
	// Solutions. Request change to Graphiti(but may have other similar problems) or wrap business objects in another object
	/*
	@Override
	public PictogramElement getPictogramElementForBusinessObject(Object businessObject) {
		final PictogramElement[] elements = this.getAllPictogramElementsForBusinessObject(businessObject);
		return elements.length > 0 ? elements[0] : null;		
	}
	*/
}
