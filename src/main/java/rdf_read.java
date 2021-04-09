import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.InputStream;
import java.util.*;

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
        HashMap<String,List<String>> refInt = new HashMap<String, List<String>>();
        HashMap<String,String> URItoName = new HashMap<String, String>();
        HashMap<String, List<String>> TableColumn = new HashMap<String, List<String>>();
        HashMap<String, String> ColumnTypes = new HashMap<String, String>();

        ExtendedIterator<OntClass> iterator = model.listClasses();
        System.out.println("/**************************************************************/");
        while (iterator.hasNext()) {
            OntClass ontClass = (OntClass) iterator.next();
            URItoName.put(ontClass.getLocalName(),ontClass.getLabel("en"));
            List<String> cols = new ArrayList<String>();
            String str = ontClass.getLabel("en")+ "_ID";
            cols.add(str);
            ColumnTypes.put(str,"int");
            System.out.println("Class is : " + ontClass.getLabel("en"));
            if (ontClass.hasSuperClass()) {
                List<String> subClasses;
                if(refInt.containsKey(ontClass.getSuperClass().getLabel("en"))){
                    subClasses = refInt.get(ontClass.getSuperClass().getLabel("en"));
                }
                else {
                    subClasses = new ArrayList<String>();
                }
                subClasses.add(ontClass.getLabel("en"));
                refInt.put(ontClass.getSuperClass().getLabel("en"),subClasses);
                String id = TableColumn.get(ontClass.getSuperClass().getLabel("en")).get(0);
                cols.add(id);
                System.out.println("SuperClass is : " + ontClass.getSuperClass().getLabel("en"));
            }
            TableColumn.put(ontClass.getLabel("en"),cols);
        }
//        System.out.println("/**************************************************************/");
//        Iterator<Map.Entry<String, List<String>>> it = TableColumn.entrySet().iterator();
//        while (it.hasNext()){
//            Map.Entry<String, List<String>> next = it.next();
//            System.out.print("Table is: "+next.getKey()+" Columns are: ");
//            List<String> value = next.getValue();
//            Iterator<String> iterator2 = value.iterator();
//            while (iterator2.hasNext()){
//                System.out.print(iterator2.next()+" ");
//            }
//            System.out.println();
//        }
        System.out.println("/**************************************************************/");
        ExtendedIterator<ObjectProperty> nodeIterator = model.listObjectProperties();
        while (nodeIterator.hasNext()) {
            ObjectProperty objectProperty = (ObjectProperty) nodeIterator.next();
            System.out.println("Object property is : " + objectProperty.getLabel("en"));
            List<String> cols = new ArrayList<String>();
            URItoName.put(objectProperty.getLocalName(),objectProperty.getLabel("en"));
            String colname = objectProperty.getLabel("en") + "_ID";
            cols.add(colname);
            ColumnTypes.put(colname,"int");
//            System.out.println("Domain is: " + objectProperty.getDomain().toString());
            ExtendedIterator<? extends OntProperty> extendedIterator = objectProperty.listInverseOf();
            if (extendedIterator.hasNext()) {
                System.out.println("Inverse Object property of: " + objectProperty.getInverseOf().getLabel("en"));
                System.out.println("Domain is: " + objectProperty.getInverseOf().getRange().getLabel("en"));
                System.out.println("Range is: " + objectProperty.getInverseOf().getDomain().getLabel("en"));
            } else {
                System.out.println("Domain is: " + objectProperty.getDomain().getLabel("en"));
                cols.add(TableColumn.get(objectProperty.getDomain().getLabel("en")).get(0));
                System.out.println("Range is: " + objectProperty.getRange().getLabel("en"));
                cols.add(TableColumn.get(objectProperty.getRange().getLabel("en")).get(0));
                TableColumn.put(objectProperty.getLabel("en"),cols);
                List<String> str1 = new ArrayList<String>();
                str1 = TableColumn.get(objectProperty.getDomain().getLabel("en"));
                str1.add(colname);
                TableColumn.put(objectProperty.getDomain().getLabel("en"),str1);
                str1 = TableColumn.get(objectProperty.getRange().getLabel("en"));
                str1.add(colname);
                TableColumn.put(objectProperty.getRange().getLabel("en"),str1);
                if(refInt.containsKey(objectProperty.getDomain().getLabel("en"))){
                    str1 = refInt.get(objectProperty.getDomain().getLabel("en"));
                }
                else
                    str1 = new ArrayList<String>();
                str1.add(objectProperty.getLabel("en"));
                refInt.put(objectProperty.getDomain().getLabel("en"),str1);
                if(refInt.containsKey(objectProperty.getRange().getLabel("en"))){
                    str1 = refInt.get(objectProperty.getRange().getLabel("en"));
                }
                else
                    str1 = new ArrayList<String>();
                str1.add(objectProperty.getLabel("en"));
                refInt.put(objectProperty.getRange().getLabel("en"),str1);
            }

        }
//        Iterator<Map.Entry<String, List<String>>> iterator1 = refInt.entrySet().iterator();
//        while (iterator1.hasNext()){
//            Map.Entry<String, List<String>> next = iterator1.next();
//            System.out.print("Table is: "+next.getKey()+" References are: ");
//            List<String> value = next.getValue();
//            Iterator<String> iterator2 = value.iterator();
//            while (iterator2.hasNext()){
//                System.out.print(iterator2.next()+" ");
//            }
//            System.out.println();
//        }
        System.out.println("/**************************************************************/");
        ExtendedIterator<DatatypeProperty> iterator2 = model.listDatatypeProperties();
        while (iterator2.hasNext()) {
            DatatypeProperty datatypeProperty = (DatatypeProperty) iterator2.next();
            System.out.println("Datatype property is: " + datatypeProperty.getLabel("en"));
            System.out.println("Domain is: " + datatypeProperty.getDomain().getLabel("en"));
            List<String> str = TableColumn.get(datatypeProperty.getDomain().getLabel("en"));
            str.add(datatypeProperty.getLabel("en"));
            URItoName.put(datatypeProperty.getLocalName(),datatypeProperty.getLabel("en"));
            TableColumn.put(datatypeProperty.getDomain().getLabel("en"),str);
            Iterator<String> iterator1 = refInt.get(datatypeProperty.getDomain().getLabel("en")).iterator();
            while (iterator1.hasNext()){
                String sub = iterator1.next();
                List<String> strings = TableColumn.get(sub);
                strings.add(datatypeProperty.getLabel("en"));
                TableColumn.put(sub,strings);
            }
            ColumnTypes.put(datatypeProperty.getLabel("en"),datatypeProperty.getRange().getLocalName());
            System.out.println("Range is: " + datatypeProperty.getRange().getLocalName());
        }
        System.out.println("/**************************************************************/");
        Iterator<Map.Entry<String, List<String>>> it = TableColumn.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, List<String>> next = it.next();
            System.out.print("Table is: "+next.getKey()+" Columns are: ");
            List<String> value = next.getValue();
            Iterator<String> iterator3 = value.iterator();
            while (iterator3.hasNext()){
                System.out.print(iterator3.next()+" ");
            }
            System.out.println();
        }
        System.out.println("/**************************************************************/");
//        Iterator<Map.Entry<String, List<String>>> it = TableColumn.entrySet().iterator();
//        while (it.hasNext()){
//            Map.Entry<String, List<String>> next = it.next();
//            System.out.print("Table is: "+next.getKey()+" Columns are: ");
//            List<String> value = next.getValue();
//            Iterator<String> iterator2 = value.iterator();
//            while (iterator2.hasNext()){
//                System.out.print(iterator2.next()+" ");
//            }
//            System.out.println();
//        }
//        Iterator<Map.Entry<String, List<String>>> iterator1 = TableColumn.entrySet().iterator();
//        while (iterator1.hasNext()){
//            Map.Entry<String, List<String>> next = iterator1.next();
//            System.out.print("Table is: "+next.getKey()+" Columns are: ");
//            List<String> value = next.getValue();
//            Iterator<String> iterator3 = value.iterator();
//            while (iterator3.hasNext()){
//                System.out.print(iterator3.next()+" ");
//            }
//            System.out.println();
//        }
//        Iterator<Map.Entry<String, String>> iterator1 = ColumnTypes.entrySet().iterator();
//        while (iterator1.hasNext()){
//            Map.Entry<String, String> next = iterator1.next();
//            System.out.println("Column is: " + next.getKey()+ " with Type: "+next.getValue());
//        }

//        System.out.println("/**************************************************************/");
//        ExtendedIterator<Individual> individualExtendedIterator = model.listIndividuals();
//        while (individualExtendedIterator.hasNext()){
//            Individual next = individualExtendedIterator.next();
//            System.out.println(next.getLabel("en"));
//            System.out.println("Belongs to: "+ next.getOntClass().getLabel("en"));
//            StmtIterator iterator1 = next.listProperties();
//            while (iterator1.hasNext()){
//                Statement next1 = iterator1.next();
//                System.out.println("Subject is : " +next1.getSubject().getLocalName());
//                System.out.println("Predicte is: "+ next1.getPredicate().getLocalName());
//
//                if(next1.getObject().isResource()){
//                    System.out.println("Object asResource is: "+ URItoName.get(next1.getObject().asResource().getLocalName()));
//                }
//                else{
//                    System.out.println("Object is: "+ next1.getObject().toString());
//                }
//            }
////
//        }
//        ExtendedIterator<AllDifferent> allDifferentExtendedIterator = model.listAllDifferent();
//        while (allDifferentExtendedIterator.hasNext()){
//            RDFList members = allDifferentExtendedIterator.next().getDistinctMembers();
//            System.out.println(members.toString());
//        }
//        StmtIterator iterator2 = model.listStatements();
//        while (iterator2.hasNext()) {
//            System.out.println("*************************************");
//            Statement statement = iterator2.nextStatement();
//            Resource subject = statement.getSubject();
//            Property predicate = statement.getPredicate();
//            RDFNode object = statement.getObject();
////            System.out.println(statement.getProperty(predicate).toString());
//            System.out.println("Subject is: " + subject.getLocalName());
//            System.out.println("Predicate is: " + predicate.getLocalName());
//            if(object.isLiteral())
//            {
////                System.out.println("These values have to be added in the database");
//                System.out.println("Object is: " + object.asLiteral().getString());
//            }
//
//            else if(object.isResource())
//            {
////                StmtIterator iterator = subject.listProperties();
////                while (iterator.hasNext()){
////                    System.out.println("Properties for "+ subject.getLocalName()+ " is: "+ iterator.next().getSubject().getLocalName());
////                }
//                System.out.println("Object asResource is: " + object.asResource().getLocalName());
//            }
//
//            else
//                System.out.println("Object is: " + object.toString());
//
//        }
// write it to standard out
//        model.write(System.out);
    }
}

