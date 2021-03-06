package org.osate.ge.internal.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.osate.aadl2.Aadl2Factory;
import org.osate.aadl2.Aadl2Package;
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentImplementationReference;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.SubcomponentType;
import org.osate.ge.BusinessObjectContext;

public class AadlSubcomponentUtil {
	private static final Map<EClass, String> subcomponentTypeToCreateMethodNameMap = createConnectionTypeToMethodMap();

	/**
	 * Returns an unmodifiable map that contains the subcomponent type to create method name mapping
	 */
	private static Map<EClass, String> createConnectionTypeToMethodMap() {
		final LinkedHashMap<EClass, String> map = new LinkedHashMap<EClass, String>();
		final Aadl2Package p = Aadl2Factory.eINSTANCE.getAadl2Package();
		map.put(p.getAbstractSubcomponent(), "createOwnedAbstractSubcomponent");
		map.put(p.getBusSubcomponent(), "createOwnedBusSubcomponent");
		map.put(p.getDataSubcomponent(), "createOwnedDataSubcomponent");
		map.put(p.getDeviceSubcomponent(), "createOwnedDeviceSubcomponent");
		map.put(p.getMemorySubcomponent(), "createOwnedMemorySubcomponent");
		map.put(p.getProcessSubcomponent(), "createOwnedProcessSubcomponent");
		map.put(p.getProcessorSubcomponent(), "createOwnedProcessorSubcomponent");
		map.put(p.getSubprogramSubcomponent(), "createOwnedSubprogramSubcomponent");
		map.put(p.getSubprogramGroupSubcomponent(), "createOwnedSubprogramGroupSubcomponent");
		map.put(p.getSystemSubcomponent(), "createOwnedSystemSubcomponent");
		map.put(p.getThreadSubcomponent(), "createOwnedThreadSubcomponent");
		map.put(p.getThreadGroupSubcomponent(), "createOwnedThreadGroupSubcomponent");
		map.put(p.getVirtualBusSubcomponent(), "createOwnedVirtualBusSubcomponent");
		map.put(p.getVirtualProcessorSubcomponent(), "createOwnedVirtualProcessorSubcomponent");

		return Collections.unmodifiableMap(map);
	}

	public static Collection<EClass> getSubcomponentTypes() {
		return subcomponentTypeToCreateMethodNameMap.keySet();
	}

	/**
	 * Returns whether the specified component implementation supports subcomponents of the specified type
	 * @param subcomponentOwner
	 * @param subcomponentClass
	 * @return
	 */
	public static boolean canContainSubcomponentType(final ComponentImplementation subcomponentOwner,
			final EClass subcomponentClass) {
		return getSubcomponentCreateMethod(subcomponentOwner, subcomponentClass) != null;
	}

	private static Method getSubcomponentCreateMethod(final ComponentImplementation subcomponentOwner,
			final EClass subcomponentType) {
		// Determine the method name of the type of subcomponent
		final String methodName = subcomponentTypeToCreateMethodNameMap.get(subcomponentType);
		if (methodName == null) {
			return null;
		}

		// Get the method
		try {
			final Method method = subcomponentOwner.getClass().getMethod(methodName);
			return method;
		} catch (final Exception ex) {
			return null;
		}
	}

	public static Subcomponent createSubcomponent(final ComponentImplementation subcomponentOwner,
			final EClass subcomponentClass) {
		try {
			return (Subcomponent) getSubcomponentCreateMethod(subcomponentOwner, subcomponentClass)
					.invoke(subcomponentOwner);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static ComponentClassifier getComponentClassifier(final BusinessObjectContext boc, final Subcomponent sc) {
		if(sc.getPrototype() == null) {
			if(sc.getClassifier() == null && sc.getRefined() != null) {
				return getComponentClassifier(boc, sc.getRefined());
			} else {
				return sc.getClassifier();
			}
		} else {
			return AadlPrototypeUtil.getComponentClassifier(AadlPrototypeUtil.getPrototypeBindingContext(boc), sc);
		}
	}

	/**
	 * Returns null if it is unable to build a non-empty string.
	 * @param sc
	 * @return
	 */
	public static String getSubcomponentTypeDescription(final Subcomponent sc, final BusinessObjectContext scBoc) {
		// Get top level component implementation.
		NamedElement implRoot = null;
		for(BusinessObjectContext tmpBoc = scBoc.getParent(); tmpBoc != null; tmpBoc = tmpBoc.getParent()) {
			if (tmpBoc.getBusinessObject() instanceof ComponentImplementation) {
				implRoot = ((ComponentImplementation) tmpBoc.getBusinessObject()).getElementRoot();
			}
		}

		String retVal = "";
		final SubcomponentType scType = getAllSubcomponentType(sc);

		if(scType != null) {
			retVal += scType.getElementRoot() == implRoot ? scType.getName() : scType.getQualifiedName();
		}

		// Add text for each of the implementation references (for arrays)
		final List<ComponentImplementationReference> implRefs = getArrayComponentImplementationReferences(sc);
		if(implRefs.size() != 0) {
			retVal += "\n(";
			for(int i = 0; i < implRefs.size(); i++) {
				final ComponentImplementationReference ref = implRefs.get(i);
				if(ref.getImplementation() != null) {
					if(ref.getImplementation().eIsProxy()) {
						retVal += "<unresolved>";
					} else {
						retVal += ref.getImplementation().getElementRoot() == implRoot
								? ref.getImplementation().getName()
										: ref.getImplementation().getQualifiedName();
					}
				}

				if(i == (implRefs.size() - 1)) {
					retVal += ")";
				} else {
					retVal += ",\n";
				}

			}
		}

		return retVal.length() == 0 ? null : retVal;
	}

	// Helper Methods
	private static SubcomponentType getAllSubcomponentType(Subcomponent sc) {
		SubcomponentType scType;
		do {
			scType = sc.getSubcomponentType();
			sc = sc.getRefined();
		} while(sc != null && scType == null);

		return scType;
	}

	private static List<ComponentImplementationReference> getArrayComponentImplementationReferences(final Subcomponent sc) {
		Subcomponent tmpSc = sc;
		List<ComponentImplementationReference> refs;

		do {
			refs = tmpSc.getImplementationReferences();
			tmpSc = tmpSc.getRefined();
		} while(tmpSc != null && refs.size() == 0);

		return refs;
	}
}
