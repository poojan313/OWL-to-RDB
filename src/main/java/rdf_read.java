import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import utils.SessionUtils;

import java.io.InputStream;
import java.util.*;

public class rdf_read {
    public void CreateTables(HashMap<String,List<String>> tables,HashMap<String,String> types,HashMap<String,List<String>> references)
    {
        List<String> all_q = new ArrayList<String>();
        Iterator<Map.Entry<String, List<String>>> iterator = tables.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<String, List<String>> next = iterator.next();
            String name = next.getKey();
            String query = "CREATE TABLE IF NOT EXISTS " + name +"(";
            Iterator<String> iterator1 = next.getValue().iterator();
            while (iterator1.hasNext()){
                String col = iterator1.next();
                query += col + " " + types.get(col);
                if (col.equals(name+"_ID"))
                {
                    query += " PRIMARY KEY AUTO INCREMENT";
                }
                query += ",";
            }
            query = query.substring(0,query.length() - 1);
            query += ")";
            all_q.add(query);
        }

        Iterator<String> iterator1 = all_q.iterator();
        while (iterator1.hasNext())
        {
            Session session = SessionUtils.getSession();
            Transaction transaction =session.beginTransaction();
            String next = iterator1.next();
            Query query = session.createSQLQuery(next);
            query.executeUpdate();
            transaction.commit();
        }
        List<String> updateQueries = new ArrayList<String>();
        Iterator<Map.Entry<String, List<String>>> iterator2 = references.entrySet().iterator();
        while (iterator2.hasNext())
        {
            Map.Entry<String, List<String>> next = iterator2.next();
            String name = next.getKey();
            String query = "ALTER TABLE "+ name ;
            Iterator<String> iterator3 = next.getValue().iterator();
            while (iterator3.hasNext()){
                String next1 = iterator3.next();
                query += " ADD FOREIGN KEY (" + next1 +"_ID) REFERENCES " + next1+"("+next1+"_ID),";
            }
            query = query.substring(0,query.length() - 1);
            updateQueries.add(query);
        }

        Iterator<String> iterator3 = updateQueries.iterator();
        while (iterator3.hasNext()){
            Session session = SessionUtils.getSession();
            Transaction transaction =session.beginTransaction();
            String next = iterator3.next();
            Query query = session.createSQLQuery(next);
            query.executeUpdate();
            transaction.commit();
        }
    }


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
        ////////////////////////////////////////
        // Parsing owl file for classes
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
                List<String> superClass = new ArrayList<String>();
                ExtendedIterator<OntClass> classes = ontClass.listSuperClasses();
                while (classes.hasNext()){
                    OntClass next = classes.next();
                    String sup;
                    if(next.getLabel("en")!=null){
                        sup = next.getLabel("en");
                    }
                    else{
                        sup = next.getLocalName();
                    }
                    superClass.add(sup);
                    cols.add(sup+"_ID");
                }
                refInt.put(str,superClass);
            }
            TableColumn.put(str,cols);
        }

        /////////////////////////////////////////////
        // Parsing owl file for object properties


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
                if(refInt.containsKey(opname))
                    references = refInt.get(opname);
                else
                    references = new ArrayList<String>();
                references.add(iDomain);
                references.add(iRange);
                refInt.put(opname,references);
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
                if(refInt.containsKey(opname)){
                    str1 = refInt.get(opname);
                }
                else
                    str1 = new ArrayList<String>();
                str1.add(domain);
                str1.add(range);
                refInt.put(opname,str1);
            }

        }

        ////////////////////////////////////////////////////
        // Parsing owl file for Datatype Properties


        System.out.println("/**************************************************************/");
        ExtendedIterator<DatatypeProperty> extendedIterator = model.listDatatypeProperties();
        while (extendedIterator.hasNext()) {
            DatatypeProperty datatypeProperty = (DatatypeProperty) extendedIterator.next();
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
            String type;
            if (range.equals("string"))
                type = "VARCHAR(30)";
            else
                type = range;
            ColumnTypes.put(dpname,type);
            System.out.println("Range is: " + range);
        }


        ///////////////////////////////////////////
        // Printing all Data that has been parsed


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
        Iterator<Map.Entry<String, List<String>>> iterator1 = refInt.entrySet().iterator();
        while (iterator1.hasNext()){
            Map.Entry<String, List<String>> next = iterator1.next();
            System.out.print("PK Table is: "+next.getKey()+" FK Tables are: ");
            List<String> value = next.getValue();
            Iterator<String> it1 = value.iterator();
            while (it1.hasNext()){
                System.out.print(it1.next()+" ");
            }
            System.out.println();
        }

        System.out.println("/**************************************************************/");

        Iterator<Map.Entry<String, String>> iterator3 = ColumnTypes.entrySet().iterator();
        while (iterator3.hasNext()){
            Map.Entry<String, String> next = iterator3.next();
            System.out.println("Column is: " + next.getKey()+ " with Type: "+next.getValue());
        }
//        rdf_read rdf = new rdf_read();
//        rdf.CreateTables(TableColumn,ColumnTypes,refInt);
        StmtIterator stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()) {
            System.out.println("*************************************");
            Statement statement = stmtIterator.nextStatement();
            Resource subject = statement.getSubject();
            Property predicate = statement.getPredicate();
            RDFNode object = statement.getObject();
            System.out.println("Subject is: " + subject.getLocalName());
            System.out.println("Predicate is: " + predicate.getLocalName());
            if(object.isLiteral())
            {
                System.out.println("Object is: " + object.asLiteral().getString());
            }

            else if(object.isResource())
            {
                System.out.println("Object asResource is: " + object.asResource().getLocalName());
            }

            else
                System.out.println("Object is: " + object.toString());

        }

    }
}

