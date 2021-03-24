import org.apache.jena.ontology.AllDifferent;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.InputStream;

public class rdf_read {
    public static void main(String[] args) {
        // create an empty model
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String ns = "http://www.w3.org/2002/07/owl#";
        // use the RDFDataMgr to find the input file
        InputStream in = RDFDataMgr.open( "src/main/resources/First.owl" );
        if (in == null) {
            throw new IllegalArgumentException(
                    "File: sample.rdf +  not found");
        }

// read the RDF/XML file
        model.read(in, null);

        ExtendedIterator<OntClass> iterator = model.listClasses();
        while (iterator.hasNext()){
            OntClass ontClass = (OntClass) iterator.next();
            System.out.println("Class is : " + ontClass.toString());
            if(ontClass.hasSubClass()){
                System.out.println("SubClass is : " + ontClass.getSubClass());
            }
            if(ontClass.hasSuperClass()){
                System.out.println("SuperClass is : " + ontClass.getSuperClass());
            }
        }










//        StmtIterator iterator = model.listStatements();
//        while (iterator.hasNext())
//        {
//            System.out.println("*************************************");
//            Statement statement = iterator.nextStatement();
//            Resource subject = statement.getSubject();
//            Property predicate = statement.getPredicate();
//            RDFNode object = statement.getObject();
//            System.out.println("Subject is: " + subject.toString());
//            System.out.println("Predicate is: " + predicate.toString());
//            System.out.println("Object is: " + object.toString());
//            System.out.println("Class is: " + statement.getClass().toString());
//            if(object instanceof OntClass){
//
//            }
        }
// write it to standard out
//        model.write(System.out);
    }

