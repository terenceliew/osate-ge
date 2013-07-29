package edu.uah.rsesc.aadl.age.diagrams.common.features;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.NamedElement;

import edu.uah.rsesc.aadl.age.diagrams.common.AadlElementWrapper;
import edu.uah.rsesc.aadl.age.ui.util.DiagramOpener;

/**
 * Drill down feature. Does not inherit from AbstractDrillDownFeature because it will create diagrams if needed.
 * @author philip.alldredge
 *
 */
public class DrillDownFeature extends AbstractCustomFeature {
	public DrillDownFeature(final IFeatureProvider fp) {
		super(fp);
	}

	@Override
    public String getName() {
        return "Open associated diagram";
    }
 
    @Override
    public String getDescription() {
        return "Open the diagram associated with this model element";
    }
 
    @Override
    public boolean canExecute(ICustomContext context) {
        PictogramElement[] pes = context.getPictogramElements();
        if (pes != null && pes.length == 1) {
            Object bo = AadlElementWrapper.unwrap(getBusinessObjectForPictogramElement(pes[0]));
            if(bo instanceof Package || bo instanceof Classifier) {
                return true;
            }
        }
        return false;
    }

    @Override
	public void execute(final ICustomContext context) {
		if(context.getPictogramElements().length > 0) {
			final Object bo = AadlElementWrapper.unwrap(this.getBusinessObjectForPictogramElement(context.getPictogramElements()[0]));
			if(bo instanceof NamedElement) {
				final NamedElement element = (NamedElement)bo;
				DiagramOpener.create().openOrCreateDiagram(element);
			}
		}
	}
}