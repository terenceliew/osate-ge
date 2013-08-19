package edu.uah.rsesc.aadl.age.diagrams.common.util;

import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.UpdateContext;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.Element;
import org.osate.aadl2.Feature;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.FlowSpecification;
import org.osate.aadl2.Mode;
import org.osate.aadl2.util.Aadl2Util;

import edu.uah.rsesc.aadl.age.diagrams.common.AadlElementWrapper;

public class ClassifierHelper {
	public static void createUpdateFeatureShapes(final ContainerShape shape, final List<Feature> features, final IFeatureProvider fp) {	
		createUpdateShapesForElements(shape, features, fp, 0, false, 25, 0, true, 5);
	}
	
	public static void createUpdateModeShapes(final ContainerShape shape, final List<Mode> modes, final IFeatureProvider fp) {
		createUpdateShapesForElements(shape, modes, fp, 80, false, 25, 0, true, 5);
	}
	
	public static void createUpdateShapesForElements(final ContainerShape shape, final List<? extends Element> elements, final IFeatureProvider fp, final int startX, final boolean incX, final int xPadding, final int startY, final boolean incY, final int yPadding) {
		// TODO: Could find an X and Y that doens't overlap existing one. Or wait until layout algorithm is implemented.
		int childX = startX;
		int childY = startY;
		for(final Element element : elements) {
			final PictogramElement pictogramElement = ShapeHelper.getChildShapeByElement(shape, element, fp);
			if(pictogramElement == null) {
				final AddContext addContext = new AddContext();
				addContext.setNewObject(new AadlElementWrapper(element));
				addContext.setTargetContainer(shape);
				addContext.setX(childX);
				addContext.setY(childY);
				final IAddFeature feature = fp.getAddFeature(addContext);
				if(feature != null && feature.canAdd(addContext)) {
					final PictogramElement pe = feature.add(addContext);
					if(incX) {
						childX += pe.getGraphicsAlgorithm().getWidth() + xPadding;
					}
					
					if(incY) {
						childY += pe.getGraphicsAlgorithm().getHeight() + yPadding;
					}
				}
			} else {
				final UpdateContext updateContext = new UpdateContext(pictogramElement);
				final IUpdateFeature updateFeature = fp.getUpdateFeature(updateContext);
				
				// Update the shape regardless of whether it is "needed" or not.
				if(updateFeature != null && updateFeature.canUpdate(updateContext)) {
					updateFeature.update(updateContext);
				}
			}
		}
	}
	
	/**
	 * Returns all the features owned by the feature group type or the type it extends. This differs from FeatureGroupType's getAllFeatures because it does not 
	 * return features from the inverse.
	 * @param fgt
	 * @return
	 */
	public static EList<Feature> getAllOwnedFeatures(final Classifier classifier) {
		if(classifier instanceof FeatureGroupType) {
			return getAllOwnedFeatures((FeatureGroupType)classifier);
		} else {
			return classifier.getAllFeatures();
		}
	}
	
	/**
	 * Returns all the features owned by the feature group type or the type it extends. This differs from FeatureGroupType's getAllFeatures because it does not 
	 * return features from the inverse.
	 * @param fgt
	 * @return
	 */
	public static EList<Feature> getAllOwnedFeatures(final FeatureGroupType fgt) {
		final EList<Feature> features = new BasicEList<Feature>();
		FeatureGroupType temp = fgt;
		while(temp != null) {
			features.addAll(temp.getOwnedFeatures());
			temp = temp.getExtended();
		}

		return features;
	}
	
	// Alternative implementation of getAllFeatures. 
	// TODO: Remove if/when it is implemented into OSATE
	public static EList<Feature> getAllFeatures(final FeatureGroupType fgt) {
		final EList<Feature> owned = getAllOwnedFeatures(fgt);
		final FeatureGroupType inverseFgt = fgt.getInverse();
		if (owned.isEmpty() && !Aadl2Util.isNull(inverseFgt)) {
			return getAllOwnedFeatures(inverseFgt);
		}
		
		return owned;
	}
	
	public static void createUpdateFlowSpecifications(final ContainerShape shape, final ComponentType componentType, final IFeatureProvider fp) {
		for(final FlowSpecification fs : componentType.getAllFlowSpecifications()) {				
			// Only show flow specifications that re not in any modes
			if(fs.getAllInModes().size() == 0) {				
				// TODO: Need to  be able to get the connection for a specific flow specification... using the context
				final PictogramElement pictogramElement = ConnectionHelper.getForFlowSpecification(shape, fs, fp);
				if(pictogramElement == null) {			
					final Anchor[] anchors = AnchorUtil.getAnchorsForFlowSpecification(fs, shape, fp);
					
					if(anchors != null) {
						final AddConnectionContext addContext = new AddConnectionContext(anchors[0], anchors[1]);
						addContext.setNewObject(new AadlElementWrapper(fs));
						
						final IAddFeature addFeature = fp.getAddFeature(addContext);
						if(addFeature != null && addFeature.canAdd(addContext)) {
							addFeature.add(addContext);
						}
					}
				} else {
					final UpdateContext updateContext = new UpdateContext(pictogramElement);
					final IUpdateFeature updateFeature = fp.getUpdateFeature(updateContext);
					
					// Update the connection regardless of whether it is "needed" or not.
					if(updateFeature != null && updateFeature.canUpdate(updateContext)) {
						updateFeature.update(updateContext);
					}
				}
			}
		}
	}
	
	/**
	 * Returns whether a feature is inverted by checking parent shapes and the corresponding features and feature group type definitions. Walks the diagram shape
	 * instead of trying to walk declarative model because it is not possible to walk up the declarative model when nested feature groups are used and in cased
	 * involving feature groups implicitly defined using inverse of.
	 * @param featureShape
	 * @param fp
	 * @return
	 */
	public static boolean isFeatureInverted(final Shape featureShape, final IFeatureProvider fp) {
		boolean isInverted = false;
		
		Shape container = featureShape.getContainer();
		while(!(container instanceof Diagram)) {
			final Object containerBo = AadlElementWrapper.unwrap(fp.getBusinessObjectForPictogramElement(container));
			if(containerBo instanceof FeatureGroup) {
				final FeatureGroup fg = (FeatureGroup)containerBo;
				isInverted ^= fg.isInverse();
				
				// This feature group type is not necessarily the one that owned the feature... Could be inverse.. Could be refined, etc..
				// Check if the feature group type was implicitly defined via an inverse
				final FeatureGroupType fgt = fg.getAllFeatureGroupType();
				if(ClassifierHelper.getAllOwnedFeatures(fgt).isEmpty() && !Aadl2Util.isNull(fgt.getInverse())) {
					isInverted = !isInverted;
				}
			}

			container = container.getContainer();
		}
		
		return isInverted;
	}
}