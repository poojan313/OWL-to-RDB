
import org.apache.jena.atlas.iterator.Iter;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.Sessionutil;

import java.io.InputStream;
import java.util.*;

public class rdf_read {
    public void CreateTable(String name,List<String> cols,HashMap<String,String> type){
        HashMap<String,String>sql=new HashMap<String, String>();
        sql.put("int","int");
        sql.put("string","varchar(40)");

        StringBuilder createTableQuery = new StringBuilder(
                "CREATE TABLE IF NOT EXISTS " + name + " (");
        for(String s:cols){
            createTableQuery.append(s+" ");
            createTableQuery.append(sql.get(type.get(s))+" ");
            if(name+"_ID"==s) {
                createTableQuery
                        .append("Primary key"+", ");
                        }
            else{
                createTableQuery.append(",");
            }
            }


        createTableQuery.replace(createTableQuery.lastIndexOf(","),
                createTableQuery.length(), ")");
        Session session= Sessionutil.getSession();
        Transaction transaction= session.beginTransaction();
        int count = session.createSQLQuery(createTableQuery.toString()).executeUpdate();
        transaction.commit();

    }
    public static void main(String[] args) {
        // create an empty model
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String ns = "http://www.w3.org/2002/07/owl#";
        // use the RDFDataMgr to find the input file


//        InputStream in = RDFDataMgr.open("src/main/resources/First.owl");

        InputStream in = RDFDataMgr.open( "src/main/resources/protege.owl" );

//        InputStream in = RDFDataMgr.open( "src/main/resources/First.owl" );

//        InputStream in = RDFDataMgr.open("src/main/resources/protege.owl");


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


        //Trying to Do some inferencing=====================================================
        HashMap<String,List<String>>SubjectToPredicate=new HashMap<String, List<String>>();
        HashMap<String,List<String>>PredicateToObject=new HashMap<String, List<String>>();





        //===================================================================================


        HashMap<String,List<String>> refInt = new HashMap<String, List<String>>();
        HashMap<String,String> URItoName = new HashMap<String, String>();
        HashMap<String, List<String>> TableColumn = new HashMap<String, List<String>>();
        HashMap<String, String> ColumnTypes = new HashMap<String, String>();

        ExtendedIterator<OntClass> iterator = model.listClasses();
        List<String> classes=new ArrayList<String>();
        HashMap<String,List<String>> ClassSuperclassUris=new HashMap<String,List<String>>();
//        HashMap<String,String> URItoName = new HashMap<String, String>();
//        HashMap<String,String> UrisToClassLabel=new HashMap<String,String>();
        Set<List<String>> AllProps=new HashSet<List<String>>();
        while (iterator.hasNext()){
            OntClass ontClass = (OntClass) iterator.next();
            String uri=ontClass.getURI();
            System.out.println("Class is : " + ontClass.getLabel("en"));
//            ontClass.getLocalName();
//            System.out.println("Label is :"+ontClass.getLabel("en"));
//            UrisToClassLabel.put(ontClass.toString(), ontClass.getLabel("en"));
            classes.add( ontClass.getLabel("en"));
//            if(ontClass.hasSubClass()){
//                System.out.println("SubClass is : " + ontClass.getSubClass());
//                OntClass subclass=ontClass.getSubClass();
////                System.out.println("Subclass Label is :"+ ontClass.getLabel("en"));
//            }
            if(ontClass.hasSubClass()){
//                System.out.println("SuperClassLabel is :"+ ontClass.getLabel("en"));
                ExtendedIterator<OntClass> iterator1 = ontClass.listSubClasses();
                while (iterator1.hasNext()){
                    OntClass c = iterator1.next();
                    System.out.println("SubClass is : " + c.getLabel("en"));
                    if(ClassSuperclassUris.containsKey(ontClass.getLabel("en"))){
                        List<String> temp=ClassSuperclassUris.get(ontClass.getLabel("en"));
                        temp.add(c.getLabel("en"));
                        ClassSuperclassUris.put(ontClass.getLabel("en"),temp);
                    }
                    else{
                        List<String> temp=new ArrayList<String>();
                        temp.add(c.getLabel("en"));
                        ClassSuperclassUris.put(ontClass.getLabel("en"),temp);
                    }
                }



            }


            //Trying to get properties
            System.out.println("Properties->");
            ExtendedIterator<OntProperty> iterprop = model.listAllOntProperties();

            while (iterprop.hasNext()){
//                System.out.println(iterprop.next());
                OntProperty prop=(OntProperty) iterprop.next();
                String domain="";
                domain=domain+prop.getDomain().getLabel("en");

                String range="";

                if(prop.getRange().isOntLanguageTerm()){
                    range=range+prop.getRange().getLocalName();
//                    System.out.println("Range of property "+prop.getLabel("en")+" is "+prop.getRange().getLocalName());
                }
                else {
                    range=range+prop.getRange().getLabel("en");
//                     System.out.println("Range of property "+prop.getLabel("en")+" is "+prop.getRange().getLabel("en"));
                }
//                System.out.println("Property is " + prop.getLabel("en") + " Domain is: "+ domain + " and Range is: "+range);
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
        System.out.println("Classes are: ");

//        System.out.println("SOP---------------------------------------------------");
//        StmtIterator stmtIterator = model.listStatements();
//        while(stmtIterator.hasNext()) {
//            Statement st = stmtIterator.next();
//            Property p = st.getPredicate();
//            RDFNode o = st.getObject();
//            System.out.println(p + ":" + o);
//        }
        //==========================================================================================================

        for(int i=0;i<classes.size();i++){
            System.out.println(classes.get(i));
        }
//        HashMap<String,List<String>>ClassSubClass=new HashMap<String, List<String>>();
//        Iterator itr = ClassSuperclassUris.entrySet().iterator();
//
//        while(itr.hasNext()){
//            Map.Entry ele = (Map.Entry)itr.next();
//            List<String>temp= (List<String>) ele.getValue();
//            List<String>ans=new ArrayList<String>();
//            for(int i=0;i<temp.size();i++){
//                String s=UrisToClassLabel.get(temp.get(i));
//                if(ClassSubClass.containsKey(s)){
//                    List<String>inside=ClassSubClass.get(s);
//                    inside.add((String) ele.getKey());
//                    ClassSubClass.put(s,inside);
//                }
//                else{
//                    List<String>inside=new ArrayList<String>();
//                    inside.add((String) ele.getKey());
//                    ClassSubClass.put(s,inside);
//                }
//            }
//
//        }
        System.out.println(ClassSuperclassUris.size());
        Iterator iter = ClassSuperclassUris.entrySet().iterator();
        for (Map.Entry ele : ClassSuperclassUris.entrySet()){
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
        Iterator i = AllProps.iterator();
        System.out.println("Properties are ");
        while (i.hasNext()) {
            List<String> next = (List<String>) i.next();
            System.out.println("Property is: " + model.getOntClass(next.get(1)).getLabel("en"));
            System.out.println("Property: "+next.get(0)+" Domain: "+next.get(1)+" Range: "+next.get(2));
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
//======================================================================================================
        //Individulas
//        System.out.println("Individuals");
//        Iterator indi=model.listIndividuals();
//        while(indi.hasNext()){
//            Individual indiv=(Individual) indi.next();
//            System.out.println(indiv.getLabel("en")+ indiv.getOntClass().getLabel("en"));
////            System.out.println(indiv.getLabel("en")+ indiv.get);
//        }



//        StmtIterator iterator = model.listStatements();
//        while (iterator.hasNext())
//        {
        iterator = model.listClasses();
        System.out.println("/**************************************************************/");
        while (iterator.hasNext()) {
            System.out.println("Inside");
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
        rdf_read rd=new rdf_read();
        while (it.hasNext()){
            Map.Entry<String, List<String>> next = it.next();
            System.out.print("Table is: "+next.getKey()+" Columns are: ");

            List<String> value = next.getValue();
            rd.CreateTable(next.getKey(),value,ColumnTypes);
            Iterator<String> iterator3 = value.iterator();
            while (iterator3.hasNext()){
                System.out.print(iterator3.next()+" ");
            }
            System.out.println();
        }
        System.out.println("/**************************************************************/");




        //        Doing this for making inferencing emgine==================================================================

//        StmtIterator inditer = model.listStatements();
//        while (inditer.hasNext()) {
//            System.out.println("*************************************");
//            Statement statement = inditer.nextStatement();
//            System.out.println(statement);
//            Resource subject = statement.getSubject();
//            Property predicate = statement.getPredicate();
//            RDFNode object = statement.getObject();
//            System.out.println("Subject is: " + URItoName.get(subject.getLocalName()));
//            System.out.println("Predicate is: " +URItoName.get(predicate.getLocalName()));
//            System.out.println("Object is: " +object.toString());
////            System.out.println(statement.asTriple());
//        }
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
        ExtendedIterator nm = model.listIndividuals();
        HashMap<String,String> NM=new HashMap<String, String>();
        HashMap<String,String> inverse=new HashMap<String, String>();
        HashMap<String,List<List<String>>> PredTo=new HashMap<String, List<List<String>>>();


        while(nm.hasNext()){
            Object temp=nm.next();
            Individual indi=(Individual) temp;
            System.out.println("nm :"+((Individual) temp).getLocalName());
            System.out.println(indi.getLabel("en"));
            NM.put(((Individual) temp).getLocalName(),indi.getLabel("en"));
        }
//        while (dp.hasNext()){
//            DatatypeProperty temp=(DatatypeProperty) dp.next();
//            System.out.println("Da");
//        }

        HashMap<String, List<String>> stp = new HashMap<String, List<String>>();
        HashMap<List<String>, List<String>> pto = new HashMap<List<String>, List<String>>();

        StmtIterator indi = model.listStatements();
        while (indi.hasNext()) {
            System.out.println("*************************************");
            String sub;
            String pred;
            String obj;
            Statement statement = indi.nextStatement();
            Resource subject = statement.getSubject();
            Property predicate = statement.getPredicate();
            RDFNode object = statement.getObject();
//            System.out.println(statement.getProperty(predicate).toString());
//            System.out.println("Subject is: " +URItoName.get(subject.getLocalName()));
//            System.out.println("Predicate is: " + URItoName.get(predicate.getLocalName()));
//            System.out.println("Subject is: " +subject.getLocalName());
//            System.out.println("Predicate is: " + predicate.getLocalName());
            if(URItoName.containsKey(subject.getLocalName())){
                System.out.println("Subject is:"+URItoName.get(subject.getLocalName()));
                sub=URItoName.get(subject.getLocalName());
            }
            else if(NM.containsKey(subject.getLocalName())){
                System.out.println("Subject is:" + NM.get(subject.getLocalName()));
                sub=NM.get(subject.getLocalName());
            }
            else{
//                Individual ind=(Individual) subject;
//                System.out.println();
                System.out.println("subject is in else:" + subject.getLocalName());
                sub= subject.getLocalName();
            }
            if(URItoName.containsKey(predicate.getLocalName())){
                System.out.println("Predicate is:"+URItoName.get(predicate.getLocalName()));
                pred=URItoName.get(predicate.getLocalName());
            }
            else if(NM.containsKey(predicate.getLocalName())){
                System.out.println("predicate is:" + NM.get(predicate.getLocalName()));
                pred=NM.get(predicate.getLocalName());
            }
            else{
                System.out.println("predicate is (in else):" + predicate.getLocalName());
                pred=predicate.getLocalName();
            }
            if(object.isLiteral())
            {
//                System.out.println("These values have to be added in the database");
                System.out.println("Object is: " + object.asLiteral().getString());
                obj=object.asLiteral().getString();
            }
            else if(object.isResource())
            {
//                StmtIterator iterator = subject.listProperties();
//                while (iterator.hasNext()){
//                    System.out.println("Properties for "+ subject.getLocalName()+ " is: "+ iterator.next().getSubject().getLocalName());
//                }
                String key=object.asResource().getLocalName();
                if(URItoName.containsKey(key)){
                    System.out.println("Object asResource is: " + URItoName.get(key));
                    obj=URItoName.get(key);}

                else{
                    System.out.println("Object asResource is:" + NM.get(key));
                    obj=NM.get(key);
                }
            }

            else {
                System.out.println("Object is: " + object.toString());
                obj=object.toString();
            }
            if (!stp.containsKey(sub)){
                stp.put(sub, new ArrayList<String>());
            }
            if(pred.equals("inverseOf")){
                inverse.put(sub,obj);
            }
//            if(pred.equals("label")){
//                System.out.println("Inside inverse");
//
//            }
            stp.get(sub).add(pred);
            List<String> list = new ArrayList<String>();
            list.add(sub);
            list.add(pred);
            if (!pto.containsKey(list)) {
                pto.put( list , new ArrayList<String>());
            }

            pto.get(list).add(obj);
            if(!PredTo.containsKey(pred)){
                List<List<String>>temp=new ArrayList<List<String>>();
                List<String>subs=new ArrayList<String>();
                subs.add(sub);
                List<String>objs=new ArrayList<String>();
                objs.add(obj);
                temp.add(subs);
                temp.add(objs);
                PredTo.put(pred,temp);
            }
            else{
                PredTo.get(pred).get(0).add(sub);
                PredTo.get(pred).get(1).add(obj);
            }

//======================================================================================================
        //Individulas
//        System.out.println("Individuals");
//        Iterator indi=model.listIndividuals();
//        while(indi.hasNext()){
//            Individual indiv=(Individual) indi.next();
//            System.out.println(indiv.getLabel("en")+ indiv.getOntClass());
//        }

        }


        //inverse Reasoning
        System.out.println("Inverse Reasoning");
        for (Map.Entry mapElement : inverse.entrySet()){
            String p=(String) mapElement.getKey();
            String k=(String) mapElement.getValue();
            System.out.println(p+"       "+k);
            List<String>subs=PredTo.get(k).get(0);
            List<String>objs=PredTo.get(k).get(1);
            for(int i1=0;i1<objs.size();i1++){
//                stp.put(item, p);
                stp.get(objs.get(i1)).add(p);
                List<String>trash=new ArrayList<String>();
                trash.add(objs.get(i1));
                trash.add(p);
                if(!pto.containsKey(trash))
                {
                        pto.put(trash,new ArrayList<String>());
                }
                pto.get(trash).add(subs.get(i1));
            }

        }



        int a=1;
        while(a==1) {
            System.out.println("select subject from following list:");
            for (Map.Entry mapElement : stp.entrySet()) {
                String key = (String) mapElement.getKey();
                System.out.println(key);

            }
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter your subject: ");
            String name_s = scan.nextLine();

            List<String> list = new ArrayList<String>();

            list = stp.get(name_s);
            System.out.println("select predicate from following list");
            System.out.println(list);
            System.out.print("Enter your predicate: ");
            String name_p = scan.nextLine();
            List<String> list_o = new ArrayList<String>();

            List<String> temp = new ArrayList<String>();
            temp.add(name_s);
            temp.add(name_p);
            list_o = pto.get(temp);

            System.out.println(list_o);

            System.out.println("FINAL RESULT\n\n");
            System.out.println("With subject '" + name_s + "' predicate '" + name_p + "' object is => " + list_o + "\n\n");
            System.out.println(name_s + " " + name_p + " " + list_o+"\n\n");

            System.out.println(" To Continue press 1 ");
            a= scan.nextInt();
        }
    }
}





