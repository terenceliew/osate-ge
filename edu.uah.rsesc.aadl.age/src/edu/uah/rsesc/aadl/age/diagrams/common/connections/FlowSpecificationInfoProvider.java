package edu.uah.rsesc.aadl.age.diagrams.common.connections;

import javax.inject.Inject;

import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.FlowEnd;
import org.osate.aadl2.FlowSpecification;
import org.osate.aadl2.Subcomponent;

import edu.uah.rsesc.aadl.age.diagrams.common.patterns.FeaturePattern;
import edu.uah.rsesc.aadl.age.diagrams.common.util.AnchorService;
import edu.uah.rsesc.aadl.age.diagrams.common.util.BusinessObjectResolutionService;
import edu.uah.rsesc.aadl.age.diagrams.common.util.ShapeService;

public class FlowSpecificationInfoProvider extends AbstractConnectionInfoProvider {
	private final AnchorService anchorUtil;
	private final ShapeService shapeHelper;
	
	@Inject
	public FlowSpecificationInfoProvider(final BusinessObjectResolutionService bor, final Diagram diagram, final AnchorService anchorUtil, final ShapeService shapeHelper) {
		super(bor, diagram);
		this.anchorUtil = anchorUtil;
		this.shapeHelper = shapeHelper;
	}

	@Override
	public boolean isBusinessObjectApplicable(final Object bo) {
		return bo instanceof FlowSpecification;
	}

	@Override
	public ContainerShape getOwnerShape(final Connection connection) {		
		if(connection.getStart().getParent() instanceof ContainerShape) {
			ContainerShape temp = (ContainerShape)connection.getStart().getParent();
			while(temp != null) {
				final Object tempBo = getBusinessObjectResolver().getBusinessObjectForPictogramElement(temp);
				if(tempBo instanceof Subcomponent || tempBo instanceof ComponentType) {
					return temp;
				}
				temp = temp.getContainer();
			}
			
		}

		return null;
	}	
	
	@Override
	public Anchor[] getAnchors(final ContainerShape ownerShape, final Object bo) {		
		final FlowSpecification fs = (FlowSpecification)bo;
		Anchor a1 = null;
		Anchor a2 = null;
		
		final PictogramElement inPe = getFlowEndPictogramElement(fs.getAllInEnd(), ownerShape);
		final PictogramElement outPe = getFlowEndPictogramElement(fs.getAllOutEnd(), ownerShape);

		// Determine the anchors to use for the connection
		switch(fs.getKind()) {
		case PATH:
			{
				a1 = anchorUtil.getAnchorByName(inPe, FeaturePattern.innerConnectorAnchorName);
				a2 = anchorUtil.getAnchorByName(outPe, FeaturePattern.innerConnectorAnchorName);
				break;
			}
			
		case SOURCE:
			{
				a1 = anchorUtil.getAnchorByName(outPe, FeaturePattern.innerConnectorAnchorName);
				a2 = anchorUtil.getAnchorByName(outPe, FeaturePattern.flowSpecificationAnchorName);	
				break;
			}

		case SINK:
			{
				a1 = anchorUtil.getAnchorByName(inPe, FeaturePattern.innerConnectorAnchorName);
				a2 = anchorUtil.getAnchorByName(inPe, FeaturePattern.flowSpecificationAnchorName);	
				break;		
			}
			
		default:
			throw new RuntimeException("Unhandled case. Unsupported flow kind: " + fs.getKind());
				
		}

		if(a1 == null || a2 == null) {
			return null;
		}
		
		return new Anchor[] {a1, a2};
	}
	
	private PictogramElement getFlowEndPictogramElement(final FlowEnd fe, final ContainerShape ownerShape) {
		if(fe == null || fe.getFeature() == null) {
			return null;
		}
		
		if(fe.getContext() instanceof FeatureGroup) {
			final FeatureGroup fg = (FeatureGroup)fe.getContext();
			final PictogramElement fgPe = shapeHelper.getChildShapeByElementName(ownerShape, fg);
			if(fgPe instanceof ContainerShape) {
				return shapeHelper.getDescendantShapeByElementName((ContainerShape)fgPe, fe.getFeature());
			}
			
		} else {
			return shapeHelper.getChildShapeByElementName(ownerShape, fe.getFeature());
		}
				
		return null;
	}
}