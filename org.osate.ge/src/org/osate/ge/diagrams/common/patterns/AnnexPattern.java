package org.osate.ge.diagrams.common.patterns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osate.aadl2.Aadl2Factory;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.AnnexLibrary;
import org.osate.aadl2.AnnexSubclause;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.DefaultAnnexLibrary;
import org.osate.aadl2.DefaultAnnexSubclause;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.diagrams.common.AadlElementWrapper;
import org.osate.ge.diagrams.common.AgeImageProvider;
import org.osate.ge.services.AadlModificationService;
import org.osate.ge.services.AnchorService;
import org.osate.ge.services.BusinessObjectResolutionService;
import org.osate.ge.services.DiagramModificationService;
import org.osate.ge.services.GhostingService;
import org.osate.ge.services.LabelService;
import org.osate.ge.services.LayoutService;
import org.osate.ge.services.NamingService;
import org.osate.ge.services.PropertyService;
import org.osate.ge.services.RefactoringService;
import org.osate.ge.services.ShapeCreationService;
import org.osate.ge.services.ShapeService;
import org.osate.ge.services.StyleService;
import org.osate.ge.services.UserInputService;
import org.osate.ge.services.AadlModificationService.AbstractModifier;

/**
 * Pattern for handling AnnexLibraries and AnnexSubclauses
 */
public class AnnexPattern extends AgePattern {
	private final static String annexLabelName = "annex_label";
	private final static int tabHeight = 9;
	private final static int maxTabWidth = 100;
	private final static double tabOffsetAngle = 30.0;
	private final static double topOfTabOffsetValue = 0.3;
	private static final String annexLabelStartBracket = "{**";
	private static final String annexLabelEndBracket = "**}";
	private final GhostingService ghostingService;
	private final AnchorService anchorService;
	private final ShapeService shapeService;
	private final LabelService labelService;
	private final LayoutService layoutService;
	private final BusinessObjectResolutionService bor;
	private final PropertyService propertyService;
	private final AadlModificationService aadlModService;
	private final DiagramModificationService diagramModService;
	private final ShapeCreationService shapeCreationService;
	private final NamingService namingService;
	private final EClass annexType;
	private final UserInputService userInputService;
	private final RefactoringService refactoringService;
	private final StyleService styleService;

	@Inject
	public AnnexPattern(final GhostingService ghostingService, final AnchorService anchorService, final ShapeService shapeService, final LabelService labelService, final UserInputService userInputService, final StyleService styleService, 
			final LayoutService layoutService, final BusinessObjectResolutionService bor, final PropertyService propertyService, final AadlModificationService aadlModService, final RefactoringService refactoringService,
			final DiagramModificationService diagramModService, final ShapeCreationService shapeCreationService, final NamingService namingService, final @Named("Annex Type") EClass annexType) {
		this.ghostingService = ghostingService;
		this.anchorService = anchorService;
		this.shapeService = shapeService;
		this.labelService = labelService;
		this.layoutService = layoutService;
		this.propertyService = propertyService;
		this.aadlModService = aadlModService;
		this.diagramModService = diagramModService;
		this.shapeCreationService = shapeCreationService;
		this.namingService = namingService;
		this.bor = bor;
		this.annexType = annexType;
		this.userInputService = userInputService;
		this.refactoringService = refactoringService;
		this.styleService = styleService;
	}

	@Override
	public String getCreateName() {
		if(annexType == getDefaultAnnexLibrary()) {
			return "Annex Library";
		} else if(annexType == getDefaultAnnexSubclause()) {
			return "Annex Subclause";
		} else {
			throw throwUnhandledTypeRuntimeException();
		}
	}

	@Override
	public String getCreateImageId(){
		if(annexType == getDefaultAnnexLibrary() || annexType == getDefaultAnnexSubclause()) {
			return AgeImageProvider.getImage(annexType.getName());
		}
		
		return null;
	}

	@Override
	public boolean isMainBusinessObjectApplicable(final Object mainBusinessObject) {
		final Object bo = AadlElementWrapper.unwrap(mainBusinessObject);
		return bo instanceof AnnexLibrary || bo instanceof AnnexSubclause;
	}

	@Override
	public boolean isPaletteApplicable() {
		if(annexType == getDefaultAnnexLibrary()) {
			return isPackageDiagram();
		} else if(annexType == getDefaultAnnexSubclause()) {
			return isClassifierDiagram();
		} else {
			throw throwUnhandledTypeRuntimeException();
		}
	}

	@Override
	public boolean canAdd(final IAddContext context) {
		if(isMainBusinessObjectApplicable(context.getNewObject())) {
			final Object targetBo = bor.getBusinessObjectForPictogramElement(context.getTargetContainer());
			return targetBo instanceof Classifier || targetBo instanceof Subcomponent || targetBo instanceof AadlPackage;
		}

		return false;
	}

	@Override
	public boolean canDelete(final IDeleteContext context) {
		final PictogramElement pe = context.getPictogramElement();
		final Shape shape = (Shape)pe;
		final Object bo = bor.getBusinessObjectForPictogramElement(pe);
		if(pe instanceof Shape) {
			if(bo instanceof AnnexLibrary) {
				final AnnexLibrary annexLibrary = (AnnexLibrary)bo;
				final NamedElement containerBo = (NamedElement)bor.getBusinessObjectForPictogramElement(shape.getContainer());
				if(containerBo instanceof AadlPackage) {
					final AadlPackage aadlPackage = getAadlPackage(containerBo);
					
					return annexLibrary.getOwner() == aadlPackage.getOwnedPublicSection();
				}
			} else if(bo instanceof AnnexSubclause) {
				final AnnexSubclause annexSubclause = (AnnexSubclause)bo;
				final Object containerBo = bor.getBusinessObjectForPictogramElement(shape.getContainer());
				
				return annexSubclause.getContainingClassifier() == containerBo;
			}
		}
		
		return false;
	}
	
	@Override
	public void delete(final IDeleteContext context) {
		if(!userInputService.confirmDelete(context)) {
			return;
		}
		
		final NamedElement annex = (NamedElement)bor.getBusinessObjectForPictogramElement(context.getPictogramElement());
		aadlModService.modify(annex, new AbstractModifier<NamedElement, NamedElement>() {
			private DiagramModificationService.Modification diagramMod;
			
			@Override
			public NamedElement modify(final Resource resource, final NamedElement annex) {
	 			diagramMod = diagramModService.startModification();
	 			if(annex.getContainingClassifier() != null) {
	 				diagramMod.markOpenRelatedDiagramsAsDirty(annex.getContainingClassifier());
	 			}
	 			
				EcoreUtil.remove(annex);
				
				return null;
			}		
			
	 		@Override
			public void beforeCommit(final Resource resource, final NamedElement annex, final NamedElement modificationResult) {
				diagramMod.commit();
			}
		});
	
		// Clear selection
		getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getDiagramContainer().selectPictogramElements(new PictogramElement[0]);
	}

	@Override
	public final PictogramElement add(final IAddContext context) {
		final NamedElement neNewAnnex = (NamedElement)AadlElementWrapper.unwrap(context.getNewObject());
		final IPeCreateService peCreateService = Graphiti.getPeCreateService();
		
		// Create the container shape for the generic representation
		final ContainerShape containerShape = peCreateService.createContainerShape(context.getTargetContainer(), true);
		link(containerShape, new AadlElementWrapper(neNewAnnex));
		
		createStubGraphicsAlgorithm(containerShape, context.getX(), context.getY());		
		
		// Finish creating
		refresh(containerShape);

		return containerShape;
	}

	private GraphicsAlgorithm createStubGraphicsAlgorithm(final Shape shape, final int x, final int y) {
		final IGaService gaService = Graphiti.getGaService();
        final GraphicsAlgorithm ga = gaService.createInvisibleRectangle(shape);
        gaService.setLocation(ga, x, y);
        return ga;
	}
	
	/**
	 * Creating the graphics algorithm and setting the location for the new pictogram element
	 * @param containerShape the ContainerShape we are attaching the graphics algorithm to
	 * @param bo the new 
	 * @param x the x-coordinate the new shape will be placed
	 * @param y the y-coordinate the new shape will be placed
	 * @param diagram the current diagram
	 */
	private void createGraphicsAlgorithm(final ContainerShape containerShape, final int x, final int y, final Diagram diagram) {
		final IGaService gaService = Graphiti.getGaService();
		final GraphicsAlgorithm csGraphicsAlgorithm = containerShape.getGraphicsAlgorithm();
		final int width = csGraphicsAlgorithm.getWidth();
		final int height = csGraphicsAlgorithm.getHeight();
		
		final GraphicsAlgorithm ga = createFolderShape(containerShape, width, height, diagram);
		gaService.setLocationAndSize(ga, x, y, width, height);
	}

	/**
	 * Drawing and setting the style for the new GraphicsAlgorithm
	 * @param containerShape the ContainerShape we are attaching the graphics algorithm to
	 * @param width the width of the shape
	 * @param height the height of the shape
	 * @param diagram the current diagram
	 * @return the drawn graphics algorithm
	 */
	private GraphicsAlgorithm createFolderShape(final ContainerShape containerShape, final int width, final int height, final Diagram diagram) {
		final IGaService gaService = Graphiti.getGaService();
		//Width of tab
		int widthOfTab = Math.min(maxTabWidth, (int)(width*topOfTabOffsetValue));
		//The tab start and end slope 
		int tabOffset = getShapeOffsetHeight(tabOffsetAngle);
		final GraphicsAlgorithm ga = gaService.createPlainPolygon(containerShape, 
				new int[] {
				0, height,
				0, tabHeight,
				tabOffset, 0,
				widthOfTab, 0,
				widthOfTab+tabOffset, tabHeight,
				width, tabHeight,
				width, height});
		
		ga.setStyle(styleService.getDefaultAnnexStyle());

		return ga;
	}
	
	// Update
	@Override
	public final boolean update(final IUpdateContext context) {
		final PictogramElement pe = context.getPictogramElement();
		if(pe instanceof ContainerShape) {
			final ContainerShape shape = (ContainerShape)pe;
			final GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
			if(ga == null) {
				createStubGraphicsAlgorithm(shape, 25, 25);
			}

			refresh((ContainerShape)pe);
		}

		return true;
	}

	// Refresh
	private void refresh(final ContainerShape shape) {
		ghostingService.setIsGhost(shape, false);

		final NamedElement annexElement = (NamedElement)bor.getBusinessObjectForPictogramElement(shape);
		final Set<Shape> childShapesToGhost = new HashSet<Shape>(shapeService.getNonGhostChildren(shape));
		final List<Shape> touchedShapes = new ArrayList<>();

		// Ghost Shapes
		childShapesToGhost.removeAll(touchedShapes);
		for(final Shape child : childShapesToGhost) {
			ghostingService.setIsGhost(child, true);
		}

		// Create label shape
		labelService.createLabelShape(shape, annexLabelName,  annexElement, getLabelText(getAnnexName(annexElement)));
		
		// Layout
		layoutPictogramElement(shape);

		anchorService.createOrUpdateChopboxAnchor(shape, chopboxAnchorName);
	}

	@Override
	public boolean canLayout(final ILayoutContext context) {
		return isMainBusinessObjectApplicable(getBusinessObjectForPictogramElement(context.getPictogramElement())) && context.getPictogramElement() instanceof ContainerShape;
	}

	@Override
	public boolean canDirectEdit(final IDirectEditingContext context) {
		final PictogramElement pe = context.getPictogramElement();
        final Object bo = bor.getBusinessObjectForPictogramElement(pe);
        if((bo instanceof AnnexLibrary || bo instanceof AnnexSubclause) && annexLabelName.equals(propertyService.getName(pe))) {
        	return true;
        }
        
		return false;
	}
	
	@Override
	 public String getInitialValue(final IDirectEditingContext context) {
		final String annexName = getAnnexName(((NamedElement)bor.getBusinessObjectForPictogramElement(context.getPictogramElement())));
		return annexName;
	 }
	
	private static String getAnnexName(final NamedElement annex) {
		return annex.getName();
	}

	@Override
	public void setValue(final String value, final IDirectEditingContext context) {
		final NamedElement annex = (NamedElement)bor.getBusinessObjectForPictogramElement(context.getPictogramElement());
		refactoringService.renameElement(annex, value);
	}
	
	@Override
	public int getEditingType() {
		return TYPE_TEXT;
	}
	
	@Override
	public boolean stretchFieldToFitText() {
		return true;
	}
	
    @Override
    public String checkValueValid(final String newAnnexName, final IDirectEditingContext context) {
    	return isValidAnnexName(newAnnexName, namingService) ? newAnnexName : null;
    }
	
	@Override
	public boolean canResizeShape(final IResizeShapeContext context) {
		return !propertyService.isTransient(context.getPictogramElement()); // Don't allow resizing of transient shapes such as labels
	};

	@Override
	public void resizeShape(final IResizeShapeContext context) {
		final ContainerShape shape = (ContainerShape)context.getPictogramElement();	

		super.resizeShape(context);

		layoutService.checkContainerSize(shape);

		getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().refresh();

		// When the graphics algorithm is recreated, the selection is lost. This triggers the selection to be restored on the next editor refresh 
		getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getDiagramContainer().setPictogramElementsForSelection(getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getDiagramContainer().getSelectedPictogramElements());		
	};

	@Override
	public boolean layout(final ILayoutContext context) {
		final ContainerShape containerShape = (ContainerShape)context.getPictogramElement();
		final int x = containerShape.getGraphicsAlgorithm().getX();
		final int y = containerShape.getGraphicsAlgorithm().getY();

		final IGaService gaService = Graphiti.getGaService();
		final Shape nameShape = Objects.requireNonNull(getNameShape(containerShape), "unable to retrieve name shape");

		// Determine size of the shape
		final int[] newSize = layoutService.adjustChildShapePositions(containerShape); 

		final GraphicsAlgorithm nameShapeGraphicsAlgorithm = nameShape.getGraphicsAlgorithm();

		// Enforce a minimum size for classifiers
		newSize[0] = Math.max(Math.max(newSize[0], layoutService.getMinimumWidth()), nameShapeGraphicsAlgorithm.getWidth() + 30);
		newSize[1] = Math.max(Math.max(newSize[1], layoutService.getMinimumHeight()), nameShapeGraphicsAlgorithm.getHeight() + 30);
		
		final GraphicsAlgorithm csGraphicsAlgorithm = containerShape.getGraphicsAlgorithm();
		gaService.setSize(csGraphicsAlgorithm, newSize[0], newSize[1]);

		// Create new graphics algorithm
		createGraphicsAlgorithm(containerShape, x, y, getDiagram());

		final int shapeWidth = csGraphicsAlgorithm.getWidth();
		
		// Layout Labels
		gaService.setLocation(nameShapeGraphicsAlgorithm, (shapeWidth - nameShapeGraphicsAlgorithm.getWidth()) / 2, 17);		
	
		// Refresh. For some reason if it is not refreshed, some shapes may not be drawn correctly.
		getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().refresh();

		return true;
	}

	private static int getShapeOffsetHeight(final double deg) {
		return (int)(Math.ceil(Math.tan(Math.toRadians(deg))*tabHeight));
	}

	private Shape getNameShape(final ContainerShape shape) {
		return shapeService.getChildShapeByName(shape, annexLabelName);
	}

	@Override
	public boolean canCreate(final ICreateContext context) {
		//Connection highlights when hovering, cannot drop on connection
		if(context.getTargetConnection() == null) {
			if(annexType == null) {
				return false;
			} else if(annexType == getDefaultAnnexLibrary()) {
				return isPackageDiagram() && context.getTargetContainer() instanceof Diagram;
			} else if(annexType == getDefaultAnnexSubclause()) {
				return isClassifierDiagram() && isValidClassifier(context);
			} else {
				throw throwUnhandledTypeRuntimeException();
			}
		}

		return false;
	};

	private boolean isValidClassifier(final ICreateContext context) {
		return !(context.getTargetContainer() instanceof Diagram) && bor.getBusinessObjectForPictogramElement(getDiagram()) == bor.getBusinessObjectForPictogramElement(context.getTargetContainer());
	}

	@Override
	public Object[] create(final ICreateContext context) {
		final NamedElement containerElement = (NamedElement)bor.getBusinessObjectForPictogramElement(context.getTargetContainer());
		final Object newAnnexObject = createAndModifyAnnex(context, containerElement, annexType);

		return newAnnexObject == null ? null : new Object[] {newAnnexObject};
	}

	private Object createAndModifyAnnex(final ICreateContext context, final NamedElement targetContainer, final EClass annexType) {
		final NamedElement newAnnex = aadlModService.modify(targetContainer, new AbstractModifier<NamedElement, NamedElement>() {
			private DiagramModificationService.Modification diagramMod;

			@Override
			public NamedElement modify(final Resource resource, final NamedElement newAnnex) {
				diagramMod = diagramModService.startModification();
				final NamedElement newCreatedAnnex = createAnnex(targetContainer, annexType);
				
				return newCreatedAnnex;
			}

			@Override
			public void beforeCommit(final Resource resource, final NamedElement aadlPackage, final NamedElement newAnnex) {
				diagramMod.commit();
				shapeCreationService.createShape(context.getTargetContainer(), newAnnex, context.getX(), context.getY());
			}
		});

		return newAnnex;
	}

	/**
	 * Determine which type of Annex to create then return the new Annex
	 * @param targetContainer the element the new AnnexLibrary or AnnexSubclause will be added to
	 * @param newAnnexName the name of the new AnnexLibrary or AnnexSubclause
	 */
	protected NamedElement createAnnex(final NamedElement targetContainer, final EClass annexType) {
		final AnnexNameDialog annexNameDialog = new AnnexNameDialog(Display.getCurrent().getActiveShell(), targetContainer, namingService, getDialogTitleAndMessage(annexType));
		if (annexNameDialog.open() == Dialog.CANCEL || annexNameDialog.getValue() == null) {
			return null;
		}
		
		final String newAnnexName = annexNameDialog.getValue();
		final NamedElement neContainer = getNamedElementContainer(targetContainer);

		return neContainer instanceof AadlPackage ? createAnnexLibrary(neContainer, newAnnexName)
			: createAnnexSubclause(neContainer, newAnnexName);
	}

	private static NamedElement getNamedElementContainer(NamedElement targetContainer) {
		return getClassifier(targetContainer) != null ? getClassifier(targetContainer)
				: Objects.requireNonNull(getAadlPackage(targetContainer), "AadlPackage cannot be null.");
	}

	private static EClass getDefaultAnnexLibrary() {
		return Aadl2Factory.eINSTANCE.getAadl2Package().getDefaultAnnexLibrary();
	}

	private static Classifier getClassifier(final NamedElement targetContainer) {
		return targetContainer instanceof Classifier ? (Classifier)targetContainer : null;
	}

	/**
	 * Creating the new AnnexLibrary
	 * @param neContainer the element the new AnnexSubclause is being added to
	 * @param newAnnexLibraryName the name of the new AnnexLibrary
	 * @return
	 */
	private static AnnexLibrary createAnnexLibrary(final NamedElement neContainer, final String newAnnexLibraryName) {
		final DefaultAnnexLibrary annexLibrary = (DefaultAnnexLibrary)((AadlPackage)neContainer).getPublicSection().createOwnedAnnexLibrary(getDefaultAnnexLibrary());
		annexLibrary.setSourceText("{** **}");
		annexLibrary.setName(newAnnexLibraryName);

		return annexLibrary;
	}

	/**
	 * Creating the new AnnexSubclause
	 * @param neContainer the element the new AnnexSubclause is being added to
	 * @param newAnnexSubclauseName the name of the new AnnexSubclause
	 * @return
	 */
	private static AnnexSubclause createAnnexSubclause(final NamedElement neContainer, final String newAnnexSubclauseName) {
		final DefaultAnnexSubclause annexSubclause = (DefaultAnnexSubclause)((Classifier)neContainer).createOwnedAnnexSubclause(getDefaultAnnexSubclause());
		annexSubclause.setName(newAnnexSubclauseName);
		annexSubclause.setSourceText("{** **}");

		return annexSubclause;
	}

	private static EClass getDefaultAnnexSubclause() {
		return Aadl2Factory.eINSTANCE.getAadl2Package().getDefaultAnnexSubclause();
	}

	private static AadlPackage getAadlPackage(final NamedElement targetContainer) {
		return targetContainer instanceof AadlPackage ? (AadlPackage)targetContainer : null;
	}

	/**
	 * The dialog for naming the new AnnexLibrary or AnnexSubclause
	 */
	private static class AnnexNameDialog extends InputDialog {
		public AnnexNameDialog(final Shell parentShell, final NamedElement targetContainer, final NamingService namingService, final String[] dialogTitleAndMessage) {
			super(parentShell, dialogTitleAndMessage[0], dialogTitleAndMessage[1], "", new IInputValidator() {
				@Override
				public String isValid(final String newName) {
					return isValidAnnexName(newName, namingService) ? "The specified name is not valid." : null;
				}
			});
		}
	}
	
	/**
	 * 
	 * @param newAnnexName
	 * @param namingService
	 * @return
	 */
	final static boolean isValidAnnexName(final String newAnnexName, final NamingService namingService) {
		boolean invalid = false;
		if(!namingService.isValidIdentifier(newAnnexName)) {
			invalid = true;
		}
		
		return invalid;
	}
	/**
	 * Determine title and message for input dialog
	 * @param annexType AnnexLibrary or AnnexSubclause
	 * @return the title and message for the input dialog
	 */
	private static String[] getDialogTitleAndMessage(final EClass annexType) {
		final String[] dialogTitleAndMessage = new String[2];
		if(annexType == getDefaultAnnexLibrary()) {
			dialogTitleAndMessage[0] = "Create Annex Library";
			dialogTitleAndMessage[1] = "Enter a name for the new Annex Library.";
		} else if(annexType == getDefaultAnnexSubclause()) {
			dialogTitleAndMessage[0] = "Create Annex Subclause";
			dialogTitleAndMessage[1] = "Enter a name for the new Annex Subclause.";
		} else {
			throw throwUnhandledTypeRuntimeException();
		}

		return dialogTitleAndMessage;
	}
	
	private static RuntimeException throwUnhandledTypeRuntimeException() {
		return new RuntimeException("Unhandled case.  Must be DefaultAnnexLibrary or DefaultAnnexSubclause.");
	}
	
	/**
	 * Create the label text
	 * @param annexName the name of the AnnexLibrary or AnnexSubclause
	 * @return the label text
	 */
	private static String getLabelText(String annexName) {
		return annexLabelStartBracket + annexName + annexLabelEndBracket;
	}
}