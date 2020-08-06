package soot.jimple.infoflow.results.xml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import soot.jimple.Stmt;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

/**
 * Class for serializing FlowDroid results to XML
 * 
 * @author Steven Arzt
 *
 */
public class InfoflowResultsSerializer {
	
	public static final int FILE_FORMAT_VERSION = 100;
	
	private boolean serializeTaintPath = false;
	private final IInfoflowCFG icfg;
	
	/**
	 * Creates a new instance of the InfoflowResultsSerializer class
	 */
	public InfoflowResultsSerializer() {
		this(null);	
	}
	
	/**
	 * Creates a new instance of the InfoflowResultsSerializer class
	 * @param cfg The control flow graph to be used for obtaining additional
	 * information such as the methods containing source or sink statements
	 */
	public InfoflowResultsSerializer(IInfoflowCFG cfg) {
		this.icfg = cfg;
	}
	
	/**
	 * Serializes the given FlowDroid result object into the given file
	 * @param results The result object to serialize
	 * @param fileName The target file name
	 * @throws FileNotFoundException Thrown if target file cannot be used
	 * @throws XMLStreamException Thrown if the XML data cannot be written
	 */
	public void serialize(InfoflowResults results, String fileName)
			throws FileNotFoundException, XMLStreamException {
		OutputStream out = new FileOutputStream(fileName);
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(out);
		
		writer.writeStartDocument();
		writer.writeStartElement(XmlConstants.Tags.root);
		writer.writeAttribute(XmlConstants.Attributes.fileFormatVersion,
				FILE_FORMAT_VERSION + "");
		
		writer.writeStartElement(XmlConstants.Tags.results);		
		writeDataFlows(results, writer);
		writer.writeEndElement();
		
		writer.writeEndDocument();
		writer.close();
	}
	
	/**
	 * Writes the given data flow results into the given XML stream writer
	 * @param results The results to write out
	 * @param writer The stream writer into which to write the results
	 * @throws XMLStreamException Thrown if the XML data cannot be written
	 */
	private void writeDataFlows(InfoflowResults results,
			XMLStreamWriter writer) throws XMLStreamException {
		for (ResultSinkInfo sink : results.getResults().keySet()) {
			writer.writeStartElement(XmlConstants.Tags.result);
			writeSinkInfo(sink, writer);
			
			// Write out the sources
			writer.writeStartElement(XmlConstants.Tags.sources);
			for (ResultSourceInfo src : results.getResults().get(sink))
				writeSourceInfo(src, writer);
			writer.writeEndElement();
			
			writer.writeEndElement();
		}
		
	}
	
	/**
	 * Writes the given source information into the given XML stream writer
	 * @param source The source information to write out
	 * @param writer The stream writer into which to write the results
	 * @throws XMLStreamException Thrown if the XML data cannot be written
	 */
	private void writeSourceInfo(ResultSourceInfo source, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement(XmlConstants.Tags.source);
		writer.writeAttribute(XmlConstants.Attributes.statement,
				source.getSource().toString());
		if (icfg != null)
			writer.writeAttribute(XmlConstants.Attributes.method,
					icfg.getMethodOf(source.getSource()).getSignature());
		writeAccessPath(source.getAccessPath(), writer);
		
		if (serializeTaintPath && source.getPath() != null) {
			writer.writeStartElement(XmlConstants.Tags.taintPath);
			for (int i = 0; i < source.getPath().length; i++) {
				writer.writeStartElement(XmlConstants.Tags.pathElement);
				
				Stmt curStmt = source.getPath()[i];
				writer.writeAttribute(XmlConstants.Attributes.statement,
						curStmt.toString());
				if (icfg != null)
					writer.writeAttribute(XmlConstants.Attributes.method,
							icfg.getMethodOf(curStmt).getSignature());
				
				AccessPath curAP = source.getPathAccessPaths()[i];
				writeAccessPath(curAP, writer);
				
				writer.writeEndElement();			
			}
			writer.writeEndElement();			
		}
		
		writer.writeEndElement();
	}

	/**
	 * Writes the given sink information into the given XML stream writer
	 * @param sink The sink information to write out
	 * @param writer The stream writer into which to write the results
	 * @throws XMLStreamException Thrown if the XML data cannot be written
	 */
	private void writeSinkInfo(ResultSinkInfo sink, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement(XmlConstants.Tags.sink);
		writer.writeAttribute(XmlConstants.Attributes.statement,
				sink.getSink().toString());
		if (icfg != null)
			writer.writeAttribute(XmlConstants.Attributes.method,
					icfg.getMethodOf(sink.getSink()).getSignature());
		writeAccessPath(sink.getAccessPath(), writer);
		writer.writeEndElement();
	}
	
	/**
	 * Writes the given access path int othe given XML stream writer
	 * @param accessPath The access path to write out
	 * @param writer The stream writer into which to write the data
	 * @throws XMLStreamException Thrown if the XML data cannot be written
	 */
	private void writeAccessPath(AccessPath accessPath, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement(XmlConstants.Tags.accessPath);
		
		if (accessPath.getPlainValue() != null)
			writer.writeAttribute(XmlConstants.Attributes.value,
					accessPath.getPlainValue().toString());
		if (accessPath.getBaseType() != null)
			writer.writeAttribute(XmlConstants.Attributes.type,
					accessPath.getBaseType().toString());
		writer.writeAttribute(XmlConstants.Attributes.taintSubFields,
				accessPath.getTaintSubFields() ? XmlConstants.Values.TRUE
						: XmlConstants.Values.FALSE);
		
		// Write out the fields
		if (accessPath.getFieldCount() > 0) {
			writer.writeStartElement(XmlConstants.Tags.fields);
			for (int i = 0; i < accessPath.getFieldCount(); i++) {
				writer.writeStartElement(XmlConstants.Tags.field);
				writer.writeAttribute(XmlConstants.Attributes.value,
						accessPath.getFields()[i].toString());
				writer.writeAttribute(XmlConstants.Attributes.type,
						accessPath.getFieldTypes()[i].toString());
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		
		writer.writeEndElement();
	}
	
	/**
	 * Sets whether the taint propagation path shall be serialized along with
	 * the respective data flow result
	 * @param serialize True if taint propagation paths shall be serialized,
	 * otherwise false
	 */
	public void setSerializeTaintPath(boolean serialize) {
		this.serializeTaintPath = serialize;
	}

}
