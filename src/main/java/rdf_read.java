import org.apache.jena.ontology.*;
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
        InputStream in = RDFDataMgr.open("src/main/resources/protege.owl");
        if (in == null) {
            throw new IllegalArgumentException(
                    "File: sample.rdf +  not found");
        }

// read the RDF/XML file
        model.read(in, null);

        ExtendedIterator<OntClass> iterator = model.listClasses();
        while (iterator.hasNext()) {
            OntClass ontClass = (OntClass) iterator.next();
//            ExtendedIterator<? extends OntResource> iterator1 = ontClass.listInstances();
//            while (iterator1.hasNext()){
//                StmtIterator iterator2 = iterator1.next().getModel().listStatements();
//                while (iterator2.hasNext()){
//                    System.out.println(iterator2.);
//                }
//            }
            System.out.println("Class is : " + ontClass.toString());
            if (ontClass.hasSubClass()) {
                System.out.println("SubClass is : " + ontClass.getSubClass());
            }
            if (ontClass.hasSuperClass()) {
                System.out.println("SuperClass is : " + ontClass.getSuperClass());
            }
        }

//        ExtendedIterator<ObjectProperty> nodeIterator = model.listObjectProperties();
//        while (nodeIterator.hasNext()) {
//            ObjectProperty objectProperty = (ObjectProperty) nodeIterator.next();
//            System.out.println("Object property is : " + objectProperty.toString());
////            System.out.println("Domain is: " + objectProperty.getDomain().toString());
//            ExtendedIterator<? extends OntProperty> extendedIterator = objectProperty.listInverseOf();
//            if (extendedIterator.hasNext()) {
//                System.out.println("Inverse Object property of: " + objectProperty.getInverseOf().toString());
//                System.out.println("Domain is: " + objectProperty.getInverseOf().getRange().toString());
//                System.out.println("Range is: " + objectProperty.getInverseOf().getDomain().toString());
//            } else {
//                System.out.println("Domain is: " + objectProperty.getDomain().toString());
//                System.out.println("Range is: " + objectProperty.getRange().toString());
//            }
//
//        }
//        ExtendedIterator<DatatypeProperty> iterator1 = model.listDatatypeProperties();
//        while (iterator1.hasNext()) {
//            DatatypeProperty datatypeProperty = (DatatypeProperty) iterator1.next();
//            System.out.println("Datatype property is: " + datatypeProperty.toString());
//            System.out.println("Domain is: " + datatypeProperty.getDomain().toString());
//            System.out.println("Range is: " + datatypeProperty.getRange().toString());
//        }
////        ExtendedIterator<Individual> individualExtendedIterator = model.listIndividuals();
////        while (individualExtendedIterator.hasNext()){
////            System.out.println(individualExtendedIterator.next().toString());
////        }
//        ExtendedIterator<AllDifferent> allDifferentExtendedIterator = model.listAllDifferent();
//        while (allDifferentExtendedIterator.hasNext()){
//            RDFList members = allDifferentExtendedIterator.next().getDistinctMembers();
//            System.out.println(members.toString());
//        }
        StmtIterator iterator2 = model.listStatements();
        while (iterator2.hasNext()) {
            System.out.println("*************************************");
            Statement statement = iterator2.nextStatement();
            Resource subject = statement.getSubject();
            Property predicate = statement.getPredicate();
            RDFNode object = statement.getObject();
//            System.out.println(statement.getProperty(predicate).toString());
            System.out.println("Subject is: " + subject.getLocalName());
            System.out.println("Predicate is: " + predicate.getLocalName());
            if(object.isLiteral())
            {
//                System.out.println("These values have to be added in the database");
                System.out.println("Object is: " + object.asLiteral().getString());
            }

            else if(object.isResource())
            {
//                StmtIterator iterator = subject.listProperties();
//                while (iterator.hasNext()){
//                    System.out.println("Properties for "+ subject.getLocalName()+ " is: "+ iterator.next().getSubject().getLocalName());
//                }
                System.out.println("Object asResource is: " + object.asResource().getLocalName());
            }

            else
                System.out.println("Object is: " + object.toString());

        }
// write it to standard out
//        model.write(System.out);
    }
}

