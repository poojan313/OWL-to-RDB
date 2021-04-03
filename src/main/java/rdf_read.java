
import org.apache.jena.atlas.iterator.Iter;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.io.InputStream;
import java.util.*;

public class rdf_read {
    public static void main(String[] args) {
        // create an empty model
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String ns = "http://www.w3.org/2002/07/owl#";
        // use the RDFDataMgr to find the input file

//        InputStream in = RDFDataMgr.open("src/main/resources/First.owl");

        InputStream in = RDFDataMgr.open( "src/main/resources/protege.owl" );

        if (in == null) {
            throw new IllegalArgumentException(
                    "File: sample.rdf +  not found");
        }

// read the RDF/XML file
        model.read(in, null);


//        ExtendedIterator<OntClass> iterator = model.listClasses();
//        while (iterator.hasNext()) {
//            OntClass ontClass = (OntClass) iterator.next();
////            ExtendedIterator<? extends OntResource> iterator1 = ontClass.listInstances();
////            while (iterator1.hasNext()){
////                StmtIterator iterator2 = iterator1.next().getModel().listStatements();
////                while (iterator2.hasNext()){
////                    System.out.println(iterator2.);
////                }
////            }
//            System.out.println("Class is : " + ontClass.toString());
//            if (ontClass.hasSubClass()) {
//                System.out.println("SubClass is : " + ontClass.getSubClass());
//            }
//            if (ontClass.hasSuperClass()) {
//                System.out.println("SuperClass is : " + ontClass.getSuperClass());
//            }
//        }

        ExtendedIterator<OntClass> iterator = model.listClasses();
        List<String> classes=new ArrayList<String>();
        HashMap<String,List<String>>ClassSuperclassUris=new HashMap<String,List<String>>();
        HashMap<String,String>UrisToClassLabel=new HashMap<String,String>();
        List<List<String>> AllProps=new ArrayList<List<String>>();
        while (iterator.hasNext()){
            OntClass ontClass = (OntClass) iterator.next();
            String uri=ontClass.getURI();
            System.out.println("Class is : " + ontClass.toString());
//            ontClass.getLocalName();
//            System.out.println("Label is :"+ontClass.getLabel("en"));
            UrisToClassLabel.put(ontClass.toString(), ontClass.getLabel("en"));
            classes.add( ontClass.getLabel("en"));
//            if(ontClass.hasSubClass()){
//                System.out.println("SubClass is : " + ontClass.getSubClass());
//                OntClass subclass=ontClass.getSubClass();
////                System.out.println("Subclass Label is :"+ ontClass.getLabel("en"));
//            }
            if(ontClass.hasSuperClass()){
//                System.out.println("SuperClassLabel is :"+ ontClass.getLabel("en"));
                System.out.println("SuperClass is : " + ontClass.getSuperClass());
                if(ClassSuperclassUris.containsKey(ontClass.toString())){
                    List<String> temp=ClassSuperclassUris.get(ontClass.toString());
                    temp.add(ontClass.getSuperClass().toString());
                    ClassSuperclassUris.put(ontClass.getLabel("en"),temp);
                }
                else{
                    List<String> temp=new ArrayList<String>();
                    temp.add(ontClass.getSuperClass().toString());
                    ClassSuperclassUris.put(ontClass.getLabel("en"),temp);
                }

            }

            //Trying to get properties
            System.out.println("Properties->");
            ExtendedIterator<OntProperty> iterprop = model.listAllOntProperties();

            while (iterprop.hasNext()){
//                System.out.println(iterprop.next());
                OntProperty prop=(OntProperty) iterprop.next();
                String domain="";
                domain=domain+prop.getDomain();

                String range="";
                range=range+prop.getRange();
                List<String>temp=new ArrayList<String>();
                temp.add(prop.getLabel("en"));
                temp.add(domain);
                temp.add(range);
                AllProps.add(temp);
//                System.out.println(prop.getLabel("en")+" "+UrisToClassLabel.get(domain)+" "+UrisToClassLabel.get(range));
//                System.out.println(prop.getLabel("en")+" "+prop.getDomain()+" "+prop.getRange());
            }
        //End of master while
        }

        for(int i=0;i<classes.size();i++){
            System.out.println(classes.get(i));
        }
        HashMap<String,List<String>>ClassSubClass=new HashMap<String, List<String>>();
        Iterator itr = ClassSuperclassUris.entrySet().iterator();

        while(itr.hasNext()){
            Map.Entry ele = (Map.Entry)itr.next();
            List<String>temp= (List<String>) ele.getValue();
            List<String>ans=new ArrayList<String>();
            for(int i=0;i<temp.size();i++){
                String s=UrisToClassLabel.get(temp.get(i));
                if(ClassSubClass.containsKey(s)){
                    List<String>inside=ClassSubClass.get(s);
                    inside.add((String) ele.getKey());
                    ClassSubClass.put(s,inside);
                }
                else{
                    List<String>inside=new ArrayList<String>();
                    inside.add((String) ele.getKey());
                    ClassSubClass.put(s,inside);
                }
            }

        }
        System.out.println(ClassSubClass.size());
        Iterator iter = ClassSubClass.entrySet().iterator();
        for (Map.Entry ele : ClassSubClass.entrySet()){
//            Map.Entry ele = (Map.Entry)itr.next();
            List<String>temp= (List<String>) ele.getValue();
            System.out.println("Class is :" + ele.getKey());
            System.out.println("Subclasses are");
            for(int i=0;i<temp.size();i++){
                System.out.println(temp.get(i));
            }
        }
//=====================================================================================================
        //Properties
        for(int i=0;i<AllProps.size();i++){
            List<String>inside=AllProps.get(i);
            System.out.println(inside.get(0)+" "+UrisToClassLabel.get(inside.get(1))+" "+UrisToClassLabel.get(inside.get(2)));
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

//======================================================================================================
        //Individulas
        System.out.println("Individuals");
        Iterator indi=model.listIndividuals();
        while(indi.hasNext()){
            Individual indiv=(Individual) indi.next();
            System.out.println(indiv.getLabel("en")+ indiv.getOntClass());
        }


        }
// write it to standard out
//        model.write(System.out);
    }
}





