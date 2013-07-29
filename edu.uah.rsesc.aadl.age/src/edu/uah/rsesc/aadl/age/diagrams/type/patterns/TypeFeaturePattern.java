package edu.uah.rsesc.aadl.age.diagrams.type.patterns;

import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.osate.aadl2.Feature;
import edu.uah.rsesc.aadl.age.diagrams.common.AadlElementWrapper;
import edu.uah.rsesc.aadl.age.diagrams.common.patterns.AgePattern;
import edu.uah.rsesc.aadl.age.diagrams.common.util.GraphicsAlgorithmCreator;
import edu.uah.rsesc.aadl.age.util.StyleUtil;

/**
 * Pattern for controlling Feature shapes
 * Note: Child shapes are recreated during updates so they should not be referenced.
 * @author philip.alldredge
 */
public class TypeFeaturePattern extends AgePattern {
	@Override
	public boolean isMainBusinessObjectApplicable(final Object mainBusinessObject) {
		return AadlElementWrapper.unwrap(mainBusinessObject) instanceof Feature;
	}
	
	@Override
	public boolean canAdd(final IAddContext context) {
		if(isMainBusinessObjectApplicable(context.getNewObject())) {
			if(context.getTargetContainer() instanceof Diagram) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean canResizeShape(final IResizeShapeContext ctx) {
		return false;
	}
	
	@Override
	public PictogramElement add(final IAddContext context) {
		final Feature feature = (Feature)AadlElementWrapper.unwrap(context.getNewObject());
		final Diagram diagram = getDiagram();
		
		final IPeCreateService peCreateService = Graphiti.getPeCreateService();

		// Create the container shape
        final ContainerShape container = peCreateService.createContainerShape(diagram, true);
        link(container, new AadlElementWrapper(feature));
        
        createGaAndInnerShapes(container, feature, context.getX(), context.getY());
           
        // Create anchor
        peCreateService.createChopboxAnchor(container);        

        return container;
        
	}
	
	private void createGaAndInnerShapes(final ContainerShape container, final Feature feature, int x, int y) {
		final IGaService gaService = Graphiti.getGaService();
		final IPeCreateService peCreateService = Graphiti.getPeCreateService();
		
		// Determine the label text
        final String labelTxt = getLabelText(feature);
        
		// Create label
        final Shape labelShape = peCreateService.createShape(container, false);
        final Text label = createLabelGraphicsAlgorithm(labelShape, labelTxt);
        
        // Set the size        
        final IDimension labelSize = GraphitiUi.getUiLayoutService().calculateTextSize(labelTxt, label.getStyle().getFont());
		gaService.setLocationAndSize(label, 0, 0, labelSize.getWidth(), labelSize.getHeight());
		
		// Create symbol
        final Shape featureShape = peCreateService.createShape(container, false);
        final GraphicsAlgorithm featureGa = GraphicsAlgorithmCreator.createGraphicsAlgorithm(featureShape, getDiagram(), feature);
        gaService.setLocation(featureGa,  0,  labelSize.getHeight());
                
		// Set the graphics algorithm for the container to an invisible rectangle to set the bounds				
        final GraphicsAlgorithm ga = gaService.createPlainRectangle(container);
        ga.setTransparency(1.0);

        // Set size as appropriate
        gaService.setLocationAndSize(ga, x, y, Math.max(getWidth(label), getWidth(featureShape.getGraphicsAlgorithm())), 
        		Math.max(getHeight(label), getHeight(featureShape.getGraphicsAlgorithm())));
	}
	
	private int getWidth(final GraphicsAlgorithm ga) {
		return ga.getX() + ga.getWidth();
	}
	
	private int getHeight(final GraphicsAlgorithm ga) {
		return ga.getY() + ga.getHeight();
	}
	
	public final String getLabelText(final Feature feature) {
		return feature.getName();
	}
	
	// TODO: Have a generic label style and share between all patterns	
	private Text createLabelGraphicsAlgorithm(final Shape labelShape, final String labelTxt) {
		final IGaService gaService = Graphiti.getGaService();
		final Text text = gaService.createPlainText(labelShape, labelTxt);
        text.setStyle(StyleUtil.getClassifierLabelStyle(this.getDiagram()));
        return text;
	}
	
	@Override
	public boolean canUpdate(final IUpdateContext context) {
		return isMainBusinessObjectApplicable(getBusinessObjectForPictogramElement(context.getPictogramElement()));
	}
	
	@Override
	public IReason updateNeeded(final IUpdateContext context) {
		return Reason.createFalseReason();
	}
	
	@Override
	public boolean update(final IUpdateContext context) {
		// Cause problems for connections?
		final PictogramElement pe = context.getPictogramElement();
		final Feature feature = (Feature)AadlElementWrapper.unwrap(getBusinessObjectForPictogramElement(pe));
	
		if(pe instanceof ContainerShape) {
			// Remove child shapes
			((ContainerShape) pe).getChildren().clear();
			
			// Recreate the child shapes and the graphics algorithm for the shape
			createGaAndInnerShapes((ContainerShape)pe, feature, pe.getGraphicsAlgorithm().getX(), pe.getGraphicsAlgorithm().getY());
		}

		return true;
	}
}