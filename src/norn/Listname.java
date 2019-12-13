package norn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a list expression that is just a listname
 */
public class Listname implements ListExpression {
    // AF(listname) = a ListExpression which is a listname with name equal to <listname>
    // RI:
    //     listname is lowercase
    // SRE:
    //     listname is private, final, and immutable
    //
    // Thread Safety Argument;
    //     Our rep is private and final
    //     All other variables are confined to the thread that called the method (except potentially parameters, which means the client 
    //     will have to use a threadsafe datatype as input.
    
    private final String listname;
    
    /**
     * Creates a new Listname instance.
     * @param listname the name of the listname
     */
    public Listname(String listname) {
        this.listname = listname.toLowerCase();
        checkRep();
    }
    
    private void checkRep() {
        assert listname != null;
        assert listname.toLowerCase().equals(listname);
    }

    @Override
    public ListExpression removeEdits(String outerDefinition, Map<String, ListExpression> definitions) {
        checkRep();
        if (listname.equals(outerDefinition)) { // if we have an edit
            return definitions.getOrDefault(outerDefinition, new EmptyExpression());
        }
        return this;
    } 
    
    @Override
    public Set<EmailAddress> getMemberAddresses(Map<String, ListExpression> previousDefinitions) {
        checkRep();
        if (previousDefinitions.containsKey(listname)) {
            return previousDefinitions.get(listname).getMemberAddresses(previousDefinitions);
        }
        return Set.of(); // if listname has not been defined, return empty set.
    }
    
    
    @Override
    public String toString() {
        checkRep();
        return listname;
    }
    
    @Override
    public boolean equals(Object that){
        // Just needs to check that the right and left expressions are equal
        checkRep();
        if (that instanceof Listname) {
            Listname other = (Listname) that;
            if (this.listname.equals(other.listname)){
                return true;
            } 
        }
        return false;
    }
    
    @Override
    public int hashCode(){
        checkRep(); 
        return listname.hashCode();
    }

    @Override
    public String htmlString() {
        checkRep();
        return listname;
    }
    
    @Override
    public List<String> getDependentListNames(Set<String> outerNames, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        if (outerNames.contains(listname)) {
            throw new InvalidExpressionException("Circular definition found involving list name " + listname);
        }
        
        Set<String> newOuterNames = new HashSet<>(outerNames);
        newOuterNames.add(listname);
        List<String> names = new ArrayList<>();
        names.add(listname);
        if (definitions.containsKey(listname)) {
            names.addAll(definitions.get(listname).getDependentListNames(newOuterNames, definitions));
        }
        checkRep();
        return names; 
    }
    
    @Override
    public List<String> getReferencedLists(Map<String, ListExpression> definitions) {
        List<String> referenced = new ArrayList<>();
        referenced.add(listname);
        if (definitions.containsKey(listname)) {
            referenced.addAll(definitions.get(listname).getReferencedLists(definitions));
        }
        checkRep();
        return referenced;
    }

    @Override
    public List<String> getDefinedLists(Map<String, ListExpression> definitions) {
        List<String> defined = new ArrayList<>();
        if (definitions.containsKey(listname)) {
            defined.addAll(definitions.get(listname).getReferencedLists(definitions));
        }
        checkRep();
        return defined;
    }

    @Override
    public boolean noForbiddenPipes(Map<String, ListExpression> definitions) {
        checkRep();
        if (definitions.containsKey(listname)) {
            return definitions.get(listname).noForbiddenPipes(definitions);
        }
        return true;
    }
}
