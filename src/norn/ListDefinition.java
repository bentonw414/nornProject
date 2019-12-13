package norn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a list expression where we define a listname
 */
public class ListDefinition implements ListExpression {
    // AF (listname, list) = A list definition where the list with name <listname> is defined to have value
    //     corresponding to <value>. It evaluates to value.getMemberAddresses()
    // RI
    //    listname is lowercase
    // SRE
    //    all reps are private, final, and immutable.
    //
    // Thread Safety Argument:
    //    All elements of the rep are final and immutable.
    //    since the other variants of ListExpression are also threadsafe, we know that the <value> part of the rep is also a threadsafe data type.
    //    since our rep invariant is true, we also know that we cannot break a relation between the parts of the rep with multiple threads running. 
    
    private final ListExpression value;
    private final String listname;

    /**
     * Creates a new ListDefinition instance
     * @param listname the listname that has been defined.
     * @param value the ListExpression that corresponds to that listname's value.
     */
    public ListDefinition(String listname, ListExpression value) {
        this.listname = listname.toLowerCase();
        this.value = value;
        checkRep();
    }
    
    private void checkRep() {
        assert listname != null;
        assert listname.toLowerCase().equals(listname);
        assert value != null;
        assert listname.length() > 0;
    }
    
    @Override
    public ListExpression removeEdits(String outerDefinition, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        ListExpression newValue = value.removeEdits(listname, definitions);
        definitions.put(listname, newValue);
        checkRep();
        return new ListDefinition(listname, newValue);
    } 
    
    @Override
    public Set<EmailAddress> getMemberAddresses(Map<String, ListExpression> previousDefinitions) {
        checkRep();
        return value.getMemberAddresses(previousDefinitions);
    }
    
    @Override
    public String toString() {
        checkRep();
        return "(" + listname + "=" + value.toString() + ")";
    }
    
    @Override
    public boolean equals(Object that) {
        // Just needs to check that the right and left expressions are equal
        if (that instanceof ListDefinition) {
            ListDefinition other = (ListDefinition) that;
            if (this.value.equals(other.value) && this.listname.equals(other.listname)){
                return true;
            } 
        }
        return false;
    }
    
    @Override
    public int hashCode(){
        checkRep();
        return Objects.hash(listname.hashCode(), value.hashCode());
    }

    @Override
    public String htmlString() {
        checkRep();
        return "(" + listname + "=" + value.htmlString() + ")";
    }
    
    @Override
    public List<String> getDependentListNames(Set<String> outerNames, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        checkRep();
        return value.getDependentListNames(outerNames, definitions);
    }
    
    @Override
    public List<String> getReferencedLists(Map<String, ListExpression> definitions) {
        List<String> referenced = new ArrayList<>(value.getReferencedLists(definitions));
        referenced.add(listname);
        checkRep();
        return referenced;
    }

    @Override
    public List<String> getDefinedLists(Map<String, ListExpression> definitions) {
        List<String> defined = new ArrayList<>(value.getDefinedLists(definitions));
        defined.add(listname);
        checkRep();
        return defined;
    }
    
    @Override
    public boolean noForbiddenPipes(Map<String, ListExpression> definitions) {
        checkRep();
        return value.noForbiddenPipes(definitions);
    }

}
