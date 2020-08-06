package soot.jimple.infoflow.rifl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.SAXException;

import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.rifl.RIFLDocument.Assignable;
import soot.jimple.infoflow.rifl.RIFLDocument.Category;
import soot.jimple.infoflow.rifl.RIFLDocument.JavaFieldSpec;
import soot.jimple.infoflow.rifl.RIFLDocument.JavaMethodSourceSinkSpec;
import soot.jimple.infoflow.rifl.RIFLDocument.JavaParameterSpec;
import soot.jimple.infoflow.rifl.RIFLDocument.JavaReturnValueSpec;
import soot.jimple.infoflow.rifl.RIFLDocument.SourceSinkSpec;
import soot.jimple.infoflow.rifl.RIFLDocument.SourceSinkType;
import soot.jimple.infoflow.source.data.AccessPathTuple;
import soot.jimple.infoflow.source.data.ISourceSinkDefinitionProvider;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;

/**
 * Source/sink definition provider class for RIFL
 * 
 * @author Steven Arzt
 *
 */
public class RIFLSourceSinkDefinitionProvider implements
		ISourceSinkDefinitionProvider {
	
	private final Set<SourceSinkDefinition> sources = new HashSet<>();
	private final Set<SourceSinkDefinition> sinks = new HashSet<>();
	private Set<SourceSinkDefinition> allMethods = null;
	
	/**
	 * Creates a new instance of the RIFLSourceSinkDefinitionProvider class
	 * @param file The file from which to read the RIFL specification
	 * @throws IOException Thrown if the given RIFL file cannot be read
	 * @throws SAXException Thrown in the case of an XML error while parsing the
	 * RIFL file
	 */
	public RIFLSourceSinkDefinitionProvider(String file) throws SAXException, IOException {
		RIFLParser parser = new RIFLParser();
		RIFLDocument doc = parser.parseRIFL(new File(file));
		
		// Collect the sources and sinks
		for (Assignable assign : doc.getInterfaceSpec().getSourcesSinks()) {
			parseRawDefinition(assign.getElement());
		}
	}
	
	/**
	 * Parses a definition depending on its type (source, sink, category)
	 * @param element The source/sink specification to parse
	 */
	private void parseRawDefinition(SourceSinkSpec element) {
		if (element.getType() == SourceSinkType.Source)
			sources.add(parseDefinition(element, SourceSinkType.Source));
		else if (element.getType() == SourceSinkType.Sink)
			sinks.add(parseDefinition(element, SourceSinkType.Sink));
		else if (element.getType() == SourceSinkType.Category) {
			Category cat = (Category) element;
			for (SourceSinkSpec spec : cat.getElements())
				parseRawDefinition(spec);
		}
		else
			throw new RuntimeException("Invalid element type");
	}
	
	/**
	 * Parses the contents of a source/sink specification element
	 * @param element The element to parse
	 * @param sourceSinkType Specifies whether the current element is a source
	 * or a sink
	 * @return The source/sink definition that corresponds to the given RIFL
	 * specification element
	 */
	private SourceSinkDefinition parseDefinition(SourceSinkSpec element,
			SourceSinkType sourceSinkType) {
		if (element instanceof JavaMethodSourceSinkSpec) {
			JavaMethodSourceSinkSpec javaElement = (JavaMethodSourceSinkSpec) element;
			
			// Get the method signature in Soot format
			String methodName = SootMethodRepresentationParser.v()
					.getMethodNameFromSubSignature(javaElement.getHalfSignature());
			String[] parameters = (SootMethodRepresentationParser.v()
					.getParameterTypesFromSubSignature(javaElement.getHalfSignature()));
			
			// Build the parameter list
			List<String> parameterTypes = new ArrayList<>(parameters.length);
			for (String p : parameters)
				parameterTypes.add(p);
			
			if (element instanceof JavaParameterSpec) {
				JavaParameterSpec paramSpec = (JavaParameterSpec) element;
				
				@SuppressWarnings("unchecked")
				Set<AccessPathTuple>[] parameterTuples = new Set[parameters.length];
				parameterTuples[paramSpec.getParamIdx()] = Collections.singleton(
						AccessPathTuple.fromPathElements(null, null,
								sourceSinkType == SourceSinkType.Source,
								sourceSinkType == SourceSinkType.Sink));
				
				SootMethodAndClass am = new SootMethodAndClass(methodName,
						javaElement.getClassName(), "", parameterTypes);
				SourceSinkDefinition def = new SourceSinkDefinition(am,
						null, parameterTuples, null);
				return def;
			}
			else if (element instanceof JavaReturnValueSpec) {
				AccessPathTuple apt = AccessPathTuple.fromPathElements(null, null,
						sourceSinkType == SourceSinkType.Source,
						sourceSinkType == SourceSinkType.Sink);
				
				SootMethodAndClass am = new SootMethodAndClass(methodName,
						javaElement.getClassName(), "", parameterTypes);
				SourceSinkDefinition def = new SourceSinkDefinition(am,
						null, null, Collections.singleton(apt));
				return def;
			}
		}
		else if (element instanceof JavaFieldSpec) {
			// JavaFieldSpec javaElement = (JavaFieldSpec) element;
			/* TODO: Does not really fit into the architecture */
		}
		throw new RuntimeException("Invalid source/sink specification element");
	}

	@Override
	public Set<SourceSinkDefinition> getSources() {
		return sources;
	}

	@Override
	public Set<SourceSinkDefinition> getSinks() {
		return sinks;
	}

	@Override
	public Set<SourceSinkDefinition> getAllMethods() {
		if (allMethods == null) {
			allMethods = new HashSet<>(sources.size() + sinks.size());
			allMethods.addAll(sources);
			allMethods.addAll(sinks);
		}
		return allMethods;
	}

}
