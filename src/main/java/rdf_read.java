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
                    query += " PRIMARY KEY ";
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
    public void insertData(String query){
        Session session = SessionUtils.getSession();
        Transaction transaction = session.beginTransaction();
        Query query1 = session.createSQLQuery(query);
        query1.executeUpdate();
        transaction.commit();
    }

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
        List<String> objProperties = new ArrayList<String>();
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
            ColumnTypes.put(str + "_ID","VARCHAR(30)");
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
            objProperties.add(opname);
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
            URItoName.put(datatypeProperty.getLocalName(),datatypeProperty.getLabel("en"));
            if(datatypeProperty.getRange().getLabel("en")==null)
                range = datatypeProperty.getRange().getLocalName();
            else
                range = datatypeProperty.getRange().getLabel("en");
//            if(datatypeProperty.getDomain().getLabel("en")==null)
//                    domain = datatypeProperty.getDomain().getLocalName();
//                else
//                    domain = datatypeProperty.getDomain().getLabel("en");
//                System.out.println("Domain is: " + domain);
//                List<String> str = TableColumn.get(domain);
//                str.add(dpname);
//                TableColumn.put(domain,str);
            ExtendedIterator<? extends OntResource> extendedIterator1 = datatypeProperty.listDomain();
            while (extendedIterator1.hasNext()){
                OntResource next = extendedIterator1.next();
                if(next.getLabel("en")==null)
                    domain = next.getLocalName();
                else
                    domain = next.getLabel("en");
                System.out.println("Domain is: " + domain);
                List<String> str = TableColumn.get(domain);
                str.add(dpname);
                TableColumn.put(domain,str);
            }
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

        rdf_read rdf = new rdf_read();
        rdf.CreateTables(TableColumn,ColumnTypes,refInt);
        HashMap<String,HashMap<String,String>> dataItems = new HashMap<String,HashMap<String, String>>();
        HashMap<String,HashMap<String,List<String>>> objItems = new HashMap<String, HashMap<String, List<String>>>();
        HashMap<String,String> tableItems = new HashMap<String, String>();

        StmtIterator stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            Resource subject = statement.getSubject();
            Property predicate = statement.getPredicate();
            RDFNode object = statement.getObject();
            String sub,pred,obj;
            if(URItoName.containsKey(subject.getLocalName()) && URItoName.get(subject.getLocalName())!=null)
                sub = URItoName.get(subject.getLocalName());
            else
                sub = subject.getLocalName();
            if(URItoName.containsKey(predicate.getLocalName()) && URItoName.get(predicate.getLocalName())!=null)
                pred = URItoName.get(predicate.getLocalName());
            else
                pred = predicate.getLocalName();

            if(object.isLiteral())
            {
                obj = object.asLiteral().getString();
            }
            else if(object.isResource())
            {
                 if(URItoName.containsKey(object.asResource().getLocalName()) && URItoName.get(object.asResource().getLocalName())!=null)
                     obj = URItoName.get(object.asResource().getLocalName());
                 else
                     obj = object.asResource().getLocalName();
            }
            else
               obj = object.toString();
            if(!TableColumn.containsKey(sub) && !ColumnTypes.containsKey(sub) && !pred.equals("label")){
                if(pred.equals("type") && TableColumn.containsKey(obj))
                    tableItems.put(sub,obj);
                else
                {
                    if(objProperties.contains(pred)){
                        HashMap<String,List<String>> maps;
                        if(objItems.containsKey(pred))
                            maps = objItems.get(pred);
                        else
                            maps = new HashMap<String, List<String>>();
                        List<String> maps2;
                        if(maps.containsKey(sub))
                            maps2 = maps.get(sub);
                        else
                            maps2 = new ArrayList<String>();
                        maps2.add(obj);
                        maps.put(sub,maps2);
                        objItems.put(pred,maps);
                    }
                    else
                    {
                        HashMap<String,String> map;
                        if(dataItems.containsKey(sub))
                            map = dataItems.get(sub);
                        else
                            map = new HashMap<String, String>();
                        map.put(pred,obj);
                        dataItems.put(sub,map);
                    }

                }
            }
        }

        Iterator<Map.Entry<String, String>> iterator2 = tableItems.entrySet().iterator();
        while (iterator2.hasNext()){
            String query = "INSERT INTO ";
            Map.Entry<String, String> next = iterator2.next();
            query += next.getValue() +"(" + next.getValue() + "_ID,";
            String query2 = "('" + next.getKey() + "',";
            Iterator<Map.Entry<String, String>> iterator4 = dataItems.get(next.getKey()).entrySet().iterator();
            while (iterator4.hasNext()){
                Map.Entry<String, String> next1 = iterator4.next();
                query += next1.getKey() +",";
                if(ColumnTypes.get(next1.getKey()).equals("VARCHAR(30)"))
                    query2 += "'" + next1.getValue() + "' ,";
                else
                    query2 += next1.getValue() + ",";
            }
            query = query.substring(0,query.length() - 1);
            query += ") ";
            query2 = query2.substring(0,query2.length() - 1);
            query2 += ") ";
            query = query + " VALUES " +query2;
            System.out.println(query);
            rdf.insertData(query);
        }

        Iterator<Map.Entry<String, HashMap<String, List<String>>>> iterator4 = objItems.entrySet().iterator();
        while (iterator4.hasNext()){
            Map.Entry<String, HashMap<String, List<String>>> next = iterator4.next();
            Iterator<Map.Entry<String, List<String>>> iterator5 = next.getValue().entrySet().iterator();
            while (iterator5.hasNext()){
                Map.Entry<String, List<String>> next1 = iterator5.next();
                Iterator<String> iterator6 = next1.getValue().iterator();
                while (iterator6.hasNext()){
                    String query = "INSERT INTO " + next.getKey() +" VALUES ('"+ next1.getKey() +"', '"+iterator6.next()+"')";
                    System.out.println(query);
                    rdf.insertData(query);
                }
            }
        }
    }
}

