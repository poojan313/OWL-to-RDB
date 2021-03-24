import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.*;

/** Tutorial 1 creating a simple model
 */

public class rdf_access {
    // some definitions
    static String personURI    = "http://somewhere/JohnSmith";
    static String fullName     = "John Smith";
    static String firstName    = "John";
    static String lastName     = "Smith";
    public static void main (String args[]) {
        // create an empty model
        Model model = ModelFactory.createDefaultModel();

        // create the resource
        Resource johnSmith = model.createResource(personURI);

        // add the property
        johnSmith.addProperty(VCARD.FN, fullName);
        johnSmith.addProperty(VCARD.N,model.createResource().addProperty(VCARD.Given,firstName).addProperty(VCARD.Family,lastName));
        // list the statements in the Model
        StmtIterator iter = model.listStatements();

        // print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();  // get next statement
            Resource  subject   = stmt.getSubject();     // get the subject
            Property  predicate = stmt.getPredicate();   // get the predicate
            RDFNode   object    = stmt.getObject();      // get the object

            System.out.print("Subject : "+subject.toString());
            System.out.print(" Predicate :" + predicate.toString() + " ");
            if (object instanceof Resource) {
                System.out.print("Object :"+object.toString());
            } else {
                // object is a literal
                System.out.print(" Object :\"" + object.toString() + "\"");
            }

            System.out.println(" .");
        }
        System.out.println("Model.write : ");
        model.write(System.out);
        System.out.println("RDFDataMgr: ");
        RDFDataMgr.write(System.out, model, Lang.RDFXML);

    }
}
