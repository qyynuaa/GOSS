package pnnl.goss.rdf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.nodebreaker.Breaker;
import pnnl.goss.powergrid.topology.nodebreaker.Network;
import pnnl.goss.powergrid.topology.nodebreaker.TopologicalNode;
import pnnl.goss.rdf.server.BuildPowergrid;
import pnnl.goss.rdf.server.Esca60Vocab;
import pnnl.goss.topology.nodebreaker.dao.BreakerDao;
import pnnl.goss.topology.nodebreaker.dao.NodeBreakerDao;

public class EscaMain {
	
	private static final String PERSISTANCE_UNIT = "nodebreaker_cass_pu";
	
	private static final String ESCA_TEST = "esca60_cim.xml";
	private static boolean bufferedOut = false;
	private static BufferedOutputStream outStream = null;
	
	private static void setBufferedOut() throws FileNotFoundException{
		bufferedOut = true;
		File file = new File("c:\\scratch\\rdf_output.txt");
		if (file.exists()){
			file.delete();
		}
		outStream = new BufferedOutputStream(new FileOutputStream(file));
		System.setOut(new PrintStream(outStream));
	}
	
	private static void populateIdentityObjects(EscaType escaType, IdentifiedObject ident){
		Resource resource = escaType.getResource();
		ident.setIdentMrid(escaType.getMrid());
		ident.setIdentDataType(escaType.getDataType());
		
		ident.setIdentAlias(resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_ALIASNAME).getString());
		ident.setIdentName(resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_NAME).getString());
		ident.setIdentPathName(resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME).getString());
		if(resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_DESCRIPTION) != null){
			ident.setIdentDescription(resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_DESCRIPTION).getString());
		}
		
	}
	
	private static String getPropertyString(Resource resource, Property property){
		if (resource.getProperty(property) != null){
			// Look up the connecting resources mrid.
			if (resource.getProperty(property).getResource() != null){
				return resource.getProperty(property).getResource().getLocalName();
			}
			
			// String literal
			return resource.getProperty(property).getString();
		}
		return null;
	}
	
	private static void storeBreaker(BreakerDao dao, EscaType breaker){
		
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(breaker, ident);
		
		Breaker entity = new Breaker();
		
		entity.setIdentifiedObject(ident);
		
		
		Resource resource = breaker.getResource();
		
		entity.setSwitchNormalOpen(resource.getProperty(Esca60Vocab.SWITCH_NORMALOPEN).getBoolean());
		entity.setRatedCurrent(resource.getProperty(Esca60Vocab.BREAKER_RATEDCURRENT).getDouble());
		
		entity.setMemberOfEquipmentContainer(
				getPropertyString(resource, Esca60Vocab.EQUIPMENT_MEMBEROF_EQUIPMENTCONTAINER));
		entity.setConductingEquipmentBaseVoltage(
				getPropertyString(resource, Esca60Vocab.CONDUCTINGEQUIPMENT_BASEVOLTAGE));
		
		dao.persist(entity);
		
//		while(stmtIter.hasNext()){
//			Statement stmt = stmtIter.next();
//			Resource subject = stmt.getSubject();
//			Property predicate = stmt.getPredicate();
//			RDFNode object =  stmt.getObject();
//			
//			System.out.println("Subject to save: "+subject.getLocalName());
//			System.out.println("Predicatet to save: "+predicate.getLocalName());
//			System.out.println("Object to save: "+object.toString());
//		}
		
		System.out.println("\n");
	}

	public static void main(String[] args) throws InvalidArgumentException, IOException {
		
		//setBufferedOut();
		EscaTreeWindow window = new EscaTreeWindow(ESCA_TEST, true, "C:\\scratch\\esca_tree.txt");
		window.loadData();
		window.loadTypeMap();
		
		BreakerDao breakerDao = new BreakerDao(PERSISTANCE_UNIT);
		Map<String, EscaType> typeMap = window.getEscaTypeMap(); 
		
		for (String d : typeMap.keySet()){
			
			String dataType = typeMap.get(d).getDataType();
			if (Esca60Vocab.BREAKER_OBJECT.getLocalName().equals(dataType)){
				storeBreaker(breakerDao, typeMap.get(d));
			}
			//System.out.println(d+typeMap.get(d).getDataType());
		}
		
//		List<EscaType> connectivityNodes = window.getType("ConnectivityNode");
//		
//		Network network = new Network();
//		
//
//		for(EscaType cn:connectivityNodes){
//			TopologicalNode topoNode = new TopologicalNode();
//			
//			network.addTopologicalNode(topoNode);
//			
//			for(EscaType type:cn.getChildren()){
//				
//				System.out.println(type);
//			}
//			
//			
//			System.out.println(cn);
//		}
		 
		
		
//		
//		for(String k: typeMap.keySet()){
//			EscaType type = typeMap.get(k);
//			availableTypes.add(type.getDataType());
//		}
//		
//		for(String k: availableTypes){
//			System.out.println(k);
//		}
		
		//if(true)return;
		
//		for(String key:typeMap.keySet()){
//			System.out.println(typeMap.get(key).getDataType());
//		}
		// This is puzzling the way I have configured this.
		//window.loadSubjectTree("_7138742088364182230");
		
		//window.invertFromLevel(Esca60Vocab.SUBSTATION_OBJECT);
		// A Substation
		//BuildPowergrid grid = new BuildPowergrid();
		//grid.buildPowergrid(window.getEscaTypeMap(), window.getEscaTypeSubstationMap());
		
		// A Substation
		//window.printInvertedTree("_7138742088364182230");

		
		// A breaker
		//window.printInvertedTree("_6086371616589253666");
		// A VoltageLevel
		//window.printInvertedTree("_7385660062756494042");
		
		
		//window.printTree("_7138742088364182230");
		// A Terminal
		//window.printTree("_2463136265274055557");
		if (bufferedOut){
			outStream.flush();
		}
		//window.printSubstations();
		
		//window.printTerminalTree("_1859399559611018070");
		//EscaTreeWindow window = new EscaTreeWindow("C:\\scratch\\esca_tree.txt", false, "C:\\scratch\\esca_tree_out.txt");
	}

}
