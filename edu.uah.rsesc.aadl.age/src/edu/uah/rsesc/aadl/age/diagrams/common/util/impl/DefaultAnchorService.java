package edu.uah.rsesc.aadl.age.diagrams.common.util.impl;

import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
import org.eclipse.graphiti.mm.pictograms.FixPointAnchor;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import edu.uah.rsesc.aadl.age.diagrams.common.util.AnchorService;
import edu.uah.rsesc.aadl.age.diagrams.common.util.PropertyService;

/**
 * Class the contains miscellaneous methods for dealing with anchors
 * @author philip.alldredge
 *
 */
public class DefaultAnchorService implements AnchorService {
	private final PropertyService propertyUtil;

	public DefaultAnchorService(final PropertyService propertyUtil) {
		this.propertyUtil = propertyUtil;
	}
	
	/* (non-Javadoc)
	 * @see edu.uah.rsesc.aadl.age.diagrams.common.util.AnchorService#getAnchorByName(org.eclipse.graphiti.mm.pictograms.PictogramElement, java.lang.String)
	 */
	@Override
	public Anchor getAnchorByName(final PictogramElement pe, final String name) {
		if(pe instanceof AnchorContainer) {
			for(final Anchor anchor : ((AnchorContainer)pe).getAnchors()) {
				if(name.equals(propertyUtil.getName(anchor))) {
					return anchor;
				}
			}	
		}

		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uah.rsesc.aadl.age.diagrams.common.util.AnchorService#createOrUpdateChopboxAnchor(org.eclipse.graphiti.mm.pictograms.AnchorContainer, java.lang.String)
	 */
	@Override
	public ChopboxAnchor createOrUpdateChopboxAnchor(final AnchorContainer container, final String name) {
		final IPeCreateService peCreateService = Graphiti.getPeCreateService();
		final Anchor retrievedAnchor = getAnchorByName(container, name);

		if(retrievedAnchor == null) {
			final ChopboxAnchor anchor = peCreateService.createChopboxAnchor(container);
	        propertyUtil.setName(anchor, name);
	        return anchor;
		}
		else if(retrievedAnchor instanceof ChopboxAnchor) {
			return (ChopboxAnchor)retrievedAnchor;
		}
		else {
			throw new RuntimeException("Retrieved anchor is of invalid type: " + retrievedAnchor.getClass().getName());	
		}        
	}
	
	/* (non-Javadoc)
	 * @see edu.uah.rsesc.aadl.age.diagrams.common.util.AnchorService#createOrUpdateFixPointAnchor(org.eclipse.graphiti.mm.pictograms.AnchorContainer, java.lang.String, int, int)
	 */
	@Override
	public FixPointAnchor createOrUpdateFixPointAnchor(final AnchorContainer shape, final String name, final int x, final int y) {
		final IPeCreateService peCreateService = Graphiti.getPeCreateService();
		final IGaService gaService = Graphiti.getGaService();
		
		// Create or get the anchor by name
		final Anchor retrievedAnchor = getAnchorByName(shape, name);
		final FixPointAnchor anchor;
		if(retrievedAnchor == null) {
			anchor = peCreateService.createFixPointAnchor(shape);
			propertyUtil.setName(anchor, name);
			// Theoretically this could be done for the retrieved anchor as well to ensure it has the proper graphical algorithm. Practically it causes problem for Graphiti
			// for an unknown reason when moving feature groups. We do it only when creating the anchor for that reason
			gaService.createInvisibleRectangle(anchor);			
		} else {
			if(!(retrievedAnchor instanceof FixPointAnchor)) {
				throw new RuntimeException("Retrieved anchor is of invalid type: " + retrievedAnchor.getClass().getName());	
			}
			
			anchor = (FixPointAnchor)retrievedAnchor;
		}

		// Configure the anchor
        anchor.setLocation(gaService.createPoint(x, y));
        anchor.setUseAnchorLocationAsConnectionEndpoint(true);
        
        return anchor;
	}
}