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
        InputStream in = RDFDataMgr.open("src/main/resources/First.owl");
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
            String str;
            if(ontClass.getLabel("en")!=null){
                str = ontClass.getLabel("en");
            }
            else{
                str = ontClass.getLocalName();
            }
            cols.add(str + "_ID");
            ColumnTypes.put(str + "_ID","int");
            System.out.println("Class is : " + str);
            if (ontClass.hasSuperClass()) {
                List<String> subClasses;
                String superClass;
                if(ontClass.getSuperClass().getLabel("en")!=null && refInt.containsKey(ontClass.getSuperClass().getLabel("en"))){
                    superClass = ontClass.getSuperClass().getLabel("en");
                    subClasses = refInt.get(ontClass.getSuperClass().getLabel("en"));
                }
                else if(ontClass.getSuperClass().getLocalName()!=null && refInt.containsKey(ontClass.getSuperClass().getLocalName())){
                    System.out.println("onfoasndfas");
                    superClass = ontClass.getSuperClass().getLocalName();
                    subClasses = refInt.get(ontClass.getSuperClass().getLocalName());
                }
                else {
                    subClasses = new ArrayList<String>();
                    if (ontClass.getSuperClass().getLabel("en") == null)
                        superClass = ontClass.getSuperClass().getLocalName();
                    else
                        superClass = ontClass.getSuperClass().getLabel("en");
                }
                subClasses.add(str);
                refInt.put(superClass,subClasses);
                String id = TableColumn.get(superClass).get(0);
                cols.add(id);
                System.out.println("SuperClass is : " + superClass);
            }
            TableColumn.put(str,cols);
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
            String opname;
            if(objectProperty.getLabel("en") == null)
                opname = objectProperty.getLocalName();
            else
                opname = objectProperty.getLabel("en");
            System.out.println("Object property is : " + opname);
            List<String> cols = new ArrayList<String>();
            URItoName.put(objectProperty.getLocalName(),objectProperty.getLabel("en"));
//            System.out.println("Domain is: " + objectProperty.getDomain().toString());
            ExtendedIterator<? extends OntProperty> extendedIterator = objectProperty.listInverseOf();
            if (extendedIterator.hasNext()) {
                String iObj, iDomain, iRange;
                if(objectProperty.getInverseOf().getLabel("en")!=null)
                    iObj = objectProperty.getInverseOf().getLabel("en");
                else
                    iObj = objectProperty.getInverseOf().getLocalName();

                if (objectProperty.getInverseOf().getRange().getLabel("en")!=null)
                    iDomain = objectProperty.getInverseOf().getRange().getLabel("en");
                else
                    iDomain = objectProperty.getInverseOf().getRange().getLocalName();

                if(objectProperty.getInverseOf().getDomain().getLabel("en")!=null)
                    iRange = objectProperty.getInverseOf().getDomain().getLabel("en");
                else
                    iRange = objectProperty.getInverseOf().getDomain().getLocalName();


                System.out.println("Inverse Object property of: " + iObj);
                System.out.println("Domain is: " + iDomain);
                System.out.println("Range is: " + iRange);
                cols.add(TableColumn.get(iDomain).get(0));
                cols.add(TableColumn.get(iRange).get(0));
                List<String> references;
                if(refInt.containsKey(iDomain))
                    references = refInt.get(iDomain);
                else
                    references = new ArrayList<String>();
                references.add(iRange);
                refInt.put(iDomain,references);
                TableColumn.put(opname,cols);
            }
            else {
                String domain,range;
                if(objectProperty.getDomain().getLabel("en")== null)
                    domain = objectProperty.getDomain().getLocalName();
                else
                    domain = objectProperty.getDomain().getLabel("en");
                if(objectProperty.getRange().getLabel("en")== null)
                    range = objectProperty.getRange().getLocalName();
                else
                    range = objectProperty.getRange().getLabel("en");
                System.out.println("Domain is: " + domain);
                cols.add(TableColumn.get(domain).get(0));
                System.out.println("Range is: " + range);
                cols.add(TableColumn.get(range).get(0));
                TableColumn.put(opname,cols);
                List<String> str1;
                if(refInt.containsKey(domain)){
                    str1 = refInt.get(domain);
                }
                else
                    str1 = new ArrayList<String>();
                str1.add(opname);
                refInt.put(domain,str1);
                if(refInt.containsKey(range)){
                    str1 = refInt.get(range);
                }
                else
                    str1 = new ArrayList<String>();
                str1.add(opname);
                refInt.put(range,str1);
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
            String dpname,domain,range;
            if(datatypeProperty.getLabel("en") == null)
                dpname = datatypeProperty.getLocalName();
            else
                dpname = datatypeProperty.getLabel("en");
            System.out.println("Datatype property is: " + dpname);
            if(datatypeProperty.getDomain().getLabel("en")==null)
                domain = datatypeProperty.getDomain().getLocalName();
            else
                domain = datatypeProperty.getDomain().getLabel("en");
            if(datatypeProperty.getRange().getLabel("en")==null)
                range = datatypeProperty.getRange().getLocalName();
            else
                range = datatypeProperty.getRange().getLabel("en");


            System.out.println("Domain is: " + domain);
            List<String> str = TableColumn.get(domain);
            str.add(dpname);
            URItoName.put(datatypeProperty.getLocalName(),datatypeProperty.getLabel("en"));
            TableColumn.put(domain,str);
            ColumnTypes.put(dpname,range);
            System.out.println("Range is: " + range);
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
        StmtIterator iterator3 = model.listStatements();
        while (iterator3.hasNext()) {
            System.out.println("*************************************");
            Statement statement = iterator3.nextStatement();
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

