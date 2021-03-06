package org.osate.ge.internal.diagram.runtime.boTree;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.RelativeBusinessObjectReference;
import org.osate.ge.internal.diagram.runtime.updating.FutureElementInfo;

import com.google.common.collect.ImmutableSet;

/**
 * Helper class which builds a business object tree from a AgeDiagram
 * The nodes generated by this class will not have their business object fields set. They should be processed by a TreeExpander to set the business object.
 */
public class DiagramToBusinessObjectTreeConverter {
	public static BusinessObjectNode createBusinessObjectNode(final AgeDiagram diagram) {
		return createBusinessObjectNode(diagram, Collections.emptyMap(), Collections.emptyMap());
	}

	/**
	 *
	 * @param diagram
	 * @param futureElementInfoMap a mapping from parent DiagramNode objects to a map for which a business object node will be created for each entry with the position specified in the map.
	 * @return
	 */
	public static BusinessObjectNode createBusinessObjectNode(final AgeDiagram diagram,
			final Map<DiagramNode, Map<RelativeBusinessObjectReference, FutureElementInfo>> futureElementInfoMap,
			final Map<DiagramNode, Map<RelativeBusinessObjectReference, DiagramElement>> containerToRelativeReferenceToGhostMap) {
		BusinessObjectNode rootNode = new BusinessObjectNode(null, null, null, null, false, ImmutableSet.of(),
				Completeness.UNKNOWN);
		createBusinessObjectNodesForElements(rootNode, diagram.getDiagramElements(), futureElementInfoMap,
				containerToRelativeReferenceToGhostMap);
		createBusinessObjectNodesForGhostedElements(rootNode, diagram, futureElementInfoMap,
				containerToRelativeReferenceToGhostMap);
		createBusinessObjectNodesForFutureElements(rootNode, diagram, futureElementInfoMap);

		return rootNode;
	}


	private static void createBusinessObjectNodesForElements(final BusinessObjectNode parent,
			final Collection<DiagramElement> elements,
			final Map<DiagramNode, Map<RelativeBusinessObjectReference, FutureElementInfo>> futureElementInfoMap,
			final Map<DiagramNode, Map<RelativeBusinessObjectReference, DiagramElement>> containerToRelativeReferenceToGhostMap) {
		for(final DiagramElement e : elements) {
			createBusinessObjectNodesForElements(parent, e, futureElementInfoMap,
					containerToRelativeReferenceToGhostMap);
		}
	}

	private static void createBusinessObjectNodesForElements(final BusinessObjectNode parent,
			final DiagramElement e,
			final Map<DiagramNode, Map<RelativeBusinessObjectReference, FutureElementInfo>> futureElementInfoMap,
			final Map<DiagramNode, Map<RelativeBusinessObjectReference, DiagramElement>> containerToRelativeReferenceToGhostMap) {
		// Don't keep the business object when building the business object tree. This will ensure that tree expander or other user of the tree updates
		// the business object based on the model.
		final BusinessObjectNode childNode = new BusinessObjectNode(parent, e.getId(), e.getRelativeReference(), null,
				e.isManual(), e.getContentFilters(), Completeness.UNKNOWN);
		createBusinessObjectNodesForElements(childNode, e.getDiagramElements(), futureElementInfoMap,
				containerToRelativeReferenceToGhostMap);
		createBusinessObjectNodesForGhostedElements(childNode, e, futureElementInfoMap,
				containerToRelativeReferenceToGhostMap);
		createBusinessObjectNodesForFutureElements(childNode, e, futureElementInfoMap);
	}

	private static void createBusinessObjectNodesForGhostedElements(final BusinessObjectNode parent,
			final DiagramNode diagramNode,
			final Map<DiagramNode, Map<RelativeBusinessObjectReference, FutureElementInfo>> futureElementInfoMap,
			final Map<DiagramNode, Map<RelativeBusinessObjectReference, DiagramElement>> containerToRelativeReferenceToGhostMap) {

		final Map<RelativeBusinessObjectReference, DiagramElement> ghostedElements = containerToRelativeReferenceToGhostMap.get(diagramNode);
		if(ghostedElements != null) {
			for(final DiagramElement ghostedElement : ghostedElements.values()) {
				// Don't create a node if a node has already been created for the relative reference.
				if (parent.getChild(ghostedElement.getRelativeReference()) == null) {
					createBusinessObjectNodesForElements(parent, ghostedElement, futureElementInfoMap,
							containerToRelativeReferenceToGhostMap);
				}
			}
		}
	}

	public static void createBusinessObjectNodesForFutureElements(final BusinessObjectNode parent,
			final DiagramNode diagramNode,
			final Map<DiagramNode, Map<RelativeBusinessObjectReference, FutureElementInfo>> futureElementInfoMap) {
		final Map<RelativeBusinessObjectReference, FutureElementInfo> futureElements = futureElementInfoMap
				.get(diagramNode);
		if(futureElements != null) {
			for (final Entry<RelativeBusinessObjectReference, FutureElementInfo> futureElementEntry : futureElements
					.entrySet()) {
				// An incomplete node is created. The tree expander will fill in missing fields.
				final RelativeBusinessObjectReference ref = futureElementEntry.getKey();
				if (parent.getChild(ref) == null) {
					new BusinessObjectNode(parent, null, ref, null, futureElementEntry.getValue().manual, null,
							Completeness.UNKNOWN);
				}
			}
		}
	}
}
