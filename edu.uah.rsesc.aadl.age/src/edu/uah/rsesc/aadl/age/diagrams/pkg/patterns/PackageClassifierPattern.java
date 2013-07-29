package edu.uah.rsesc.aadl.age.diagrams.pkg.patterns;

import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.context.IRemoveContext;
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
import org.osate.aadl2.AbstractClassifier;
import org.osate.aadl2.BusClassifier;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.DataClassifier;
import org.osate.aadl2.DeviceClassifier;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.MemoryClassifier;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.ProcessClassifier;
import org.osate.aadl2.ProcessorClassifier;
import org.osate.aadl2.SubprogramClassifier;
import org.osate.aadl2.SubprogramGroupClassifier;
import org.osate.aadl2.SystemClassifier;
import org.osate.aadl2.ThreadClassifier;
import org.osate.aadl2.ThreadGroupClassifier;
import org.osate.aadl2.VirtualBusClassifier;
import org.osate.aadl2.VirtualProcessorClassifier;

import edu.uah.rsesc.aadl.age.diagrams.common.AadlElementWrapper;
import edu.uah.rsesc.aadl.age.diagrams.common.patterns.AgePattern;
import edu.uah.rsesc.aadl.age.diagrams.common.util.GraphicsAlgorithmCreator;
import edu.uah.rsesc.aadl.age.diagrams.common.util.PropertyUtil;
import edu.uah.rsesc.aadl.age.util.StyleUtil;

public class PackageClassifierPattern extends AgePattern {
	@Override
	public boolean isMainBusinessObjectApplicable(final Object mainBusinessObject) {
		return AadlElementWrapper.unwrap(mainBusinessObject) instanceof Classifier;
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
	public PictogramElement add(final IAddContext context) {
		final Classifier classifier = (Classifier)AadlElementWrapper.unwrap(context.getNewObject());
		final Diagram diagram = getDiagram();
		
		final IPeCreateService peCreateService = Graphiti.getPeCreateService();

        // Create the container shape
        final ContainerShape container = peCreateService.createContainerShape(diagram, true);
   		PropertyUtil.setTypeName(container, getClassifierTypeName(classifier));
        link(container, new AadlElementWrapper(classifier));
        
		// Create the GA and inner Shape
        createGaAndInnerShapes(container, classifier, context.getX(), context.getY());
        
        // Create anchor
        peCreateService.createChopboxAnchor(container);

        return container;
	}
	
	private void createGaAndInnerShapes(final ContainerShape container, final Classifier classifier, int x, int y) {
		final IGaService gaService = Graphiti.getGaService();
		final IPeCreateService peCreateService = Graphiti.getPeCreateService();
		
		// Determine the label text
        final String labelTxt = getLabelText(classifier);
        
		// Create label
        final Shape labelShape = peCreateService.createShape(container, false);
        final Text text = createLabelGraphicsAlgorithm(labelShape, labelTxt);
        
        // Set the size        
		final int width = Math.max(100, GraphitiUi.getUiLayoutService().calculateTextSize(labelTxt, text.getStyle().getFont()).getWidth() + 30); 
		final int height = 50; 
		gaService.setLocationAndSize(text, 0, 0, width, 20);
				
		// Create the graphics algorithm
        final GraphicsAlgorithm ga = GraphicsAlgorithmCreator.createGraphicsAlgorithm(container, getDiagram(), classifier, width, height);        
        gaService.setLocation(ga, x, y);
	}
	
	private Text createLabelGraphicsAlgorithm(final Shape labelShape, final String labelTxt) {
		final IGaService gaService = Graphiti.getGaService();
		final Text text = gaService.createPlainText(labelShape, labelTxt);
        text.setStyle(StyleUtil.getClassifierLabelStyle(this.getDiagram()));
        return text;
	}
	
	@Override
	public IReason updateNeeded(final IUpdateContext context) {
		final PictogramElement pe = context.getPictogramElement();
		final Classifier classifier = (Classifier)AadlElementWrapper.unwrap(getBusinessObjectForPictogramElement(pe));
		final String actualTypeName = getClassifierTypeName(classifier);
		final String storedTypeName = PropertyUtil.getTypeName(pe);	
		if(!actualTypeName.equals(storedTypeName)) {
			return Reason.createTrueReason("Type is out of date");
		}
		
		final Text label = getLabel((ContainerShape)pe);
		if(label == null || !label.getValue().equals(getLabelText(classifier))) {
			return Reason.createTrueReason("Label is out of date");			
		}

		return Reason.createFalseReason();
	}
	
	@Override
	public boolean canUpdate(final IUpdateContext context) {
		return (context.getPictogramElement() instanceof ContainerShape) && (AadlElementWrapper.unwrap(getBusinessObjectForPictogramElement(context.getPictogramElement())) instanceof Classifier);
	}
	
	private String getClassifierTypeName(final Classifier classifier) {
		if(classifier instanceof SystemClassifier) {
        	return "system";
        } else if(classifier instanceof ProcessClassifier) {
        	return "process";
        } else if(classifier instanceof ThreadGroupClassifier) {
        	return "thread_group";
        } else if(classifier instanceof ThreadClassifier) {
        	return "thread";
        } else if(classifier instanceof SubprogramClassifier) {
        	return "subprogram";
        } else if(classifier instanceof SubprogramGroupClassifier) {
        	return "subprogram_group";
        } else if(classifier instanceof DataClassifier) {
        	return "data";
        } else if(classifier instanceof AbstractClassifier) {
        	return "abstract";
        } else if(classifier instanceof VirtualBusClassifier) {
        	return "virtual_bus";
        } else if(classifier instanceof VirtualProcessorClassifier) {
        	return "virtual_processor";
        } else if(classifier instanceof BusClassifier) {
        	return "bus";
        } else if(classifier instanceof ProcessorClassifier) {
        	return "processor";
        } else if(classifier instanceof DeviceClassifier) {     	
        	return "device";
        } else if(classifier instanceof MemoryClassifier) {
        	return "memory";
        } else if(classifier instanceof FeatureGroupType) {
        	return "feature_group";
        } else {
        	return "unknown";
        }
	}

	private String getLabelText(final Classifier classifier) {
		final Diagram diagram = getDiagram();
		final NamedElement diagramElement = (NamedElement)AadlElementWrapper.unwrap(this.getBusinessObjectForPictogramElement(diagram));
		
		if(diagramElement == null || classifier == null || classifier.getNamespace() == null || classifier.getNamespace().getOwner() == null)
			return "";
		
		 return diagramElement.getQualifiedName().equalsIgnoreCase(((NamedElement)classifier.getNamespace().getOwner()).getQualifiedName()) ? classifier.getName() : classifier.getQualifiedName(); 
	}
	
	@Override
	public boolean update(final IUpdateContext context) {
		final PictogramElement pe = context.getPictogramElement();
		final Classifier classifier = (Classifier)AadlElementWrapper.unwrap(getBusinessObjectForPictogramElement(pe));
		
		// Update the type name property
		PropertyUtil.setTypeName(pe, getClassifierTypeName(classifier));
		
		if(pe instanceof ContainerShape) {
			// Remove child shapes
			((ContainerShape) pe).getChildren().clear();
			
			// Recreate the child shapes and the graphics algorithm for the shape
			createGaAndInnerShapes((ContainerShape)pe, classifier, pe.getGraphicsAlgorithm().getX(), pe.getGraphicsAlgorithm().getY());
		}

		return true;
	}
	
	private Shape getLabelShape(final ContainerShape cs) {
		for(final Shape shape : cs.getChildren()) {
			if(shape.getGraphicsAlgorithm() instanceof Text) {
				return shape;
			}
		}
		return null;
	}
	
	private Text getLabel(final ContainerShape cs) {
		final Shape labelShape = getLabelShape(cs);
		if(labelShape != null) {
			return (Text)labelShape.getGraphicsAlgorithm();
		}
		return null;
	}
	
	@Override
	public boolean canRemove(final IRemoveContext context) {
		return false;
	}	
	
	@Override
	public boolean canDelete(final IDeleteContext context) {
		return false;
	}

	@Override
	public boolean canResizeShape(final IResizeShapeContext context) {
		return super.canResizeShape(context);
	}
	
	public void resizeShape(final IResizeShapeContext context) {
		final PictogramElement pe = context.getPictogramElement();
		final Object bo = AadlElementWrapper.unwrap(getBusinessObjectForPictogramElement(context.getPictogramElement()));
		final ContainerShape container = (ContainerShape)pe;
       	GraphicsAlgorithmCreator.createGraphicsAlgorithm(container, getDiagram(), ((Classifier)bo), context.getWidth(), context.getHeight());
		
		super.resizeShape(context);
	}	
	
	@Override
	public boolean canLayout(final ILayoutContext context) {
		return isPatternControlled(context.getPictogramElement());
	}

	@Override
	public boolean layout(final ILayoutContext context) {
		final PictogramElement pictogramElement = context.getPictogramElement();
		
		if(pictogramElement instanceof ContainerShape) {
			final ContainerShape containerShape = (ContainerShape)pictogramElement;
			final GraphicsAlgorithm outerGraphicsAlgorithm = containerShape.getGraphicsAlgorithm();
			final EList<Shape> children = containerShape.getChildren();
			if (children.size() > 0) {
				final Shape shape = children.get(0);
				final GraphicsAlgorithm graphicsAlgorithm = shape.getGraphicsAlgorithm();
				if (graphicsAlgorithm instanceof Text) {
					Graphiti.getGaLayoutService().setLocationAndSize(graphicsAlgorithm, 0, 0, outerGraphicsAlgorithm.getWidth(), outerGraphicsAlgorithm.getHeight());
					return true;
				}
			}
		}
		return false;
	}
}