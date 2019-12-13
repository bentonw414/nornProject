package norn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a list expression with the setUnion operator.
 */
public class SetUnion implements ListExpression {
    // AF(elements) = a ListExpression that contains all of the email addresses 
    //      in the union of the addresses of each element in elements.
    // RI:
    //     elements is length >= 2
    // SRE:
    //     - elements is private and final and never modified in SetUnion class. 
    //       new List made from Constructor argument.
    //
    // Safety from rep exposure:
    //     All parts of our rep are immutable and threadsafe, and we know that any calls to our edits function must have a threadsafe map
    //     passed in, so we know that calling new threads with this is fine.
    //     All over variables are confined in their method call.

    private final List<ListExpression> elements;

    /**
     * Creates a list expression consisting of the elements unioned together.
     * @param elements the elements to union together.
     */
    public SetUnion(List<ListExpression> elements) {
        this.elements = new ArrayList<>(elements);
        checkRep();
    }
    
    /**
     * Fails an assertion if any part of our rep has been broken.
     */
    private void checkRep() {
        assert elements.size() >= 2;
    }
    
    @Override
    public ListExpression removeEdits(String outerDefinition, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        List<ListExpression> newList = new ArrayList<>();
        for (ListExpression e: elements) {
            newList.add(e.removeEdits(outerDefinition, definitions));
        }
        checkRep();
        return new SetUnion(newList);
    } 
    
    @Override
    public Set<EmailAddress> getMemberAddresses(Map<String, ListExpression> previousDefinitions) {
        // just need to return the union of the addresses on the left and the right
        final Set<EmailAddress> output = new HashSet<>();
        for (ListExpression e: elements) {
            output.addAll(e.getMemberAddresses(previousDefinitions));
        }
        checkRep();
        return output;
    }
    
    @Override
    public String toString(){
        checkRep();
        return "(" + elements.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public boolean equals(Object that){
        // Just needs to check that the right and left expressions are equal
        if (that instanceof SetUnion) {
            SetUnion other = (SetUnion) that;
            return this.elements.equals(other.elements);
        }
        checkRep();
        return false;
    }

    @Override
    public int hashCode() {
        checkRep();
        return elements.hashCode();
    }

    @Override
    public String htmlString() {
        checkRep();
        return "(" + elements.stream().map(e -> e.htmlString()).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public List<String> getDependentListNames(Set<String> outerNames, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        List<String> names = new ArrayList<>();
        for (ListExpression e: elements) {
            names.addAll(e.getDependentListNames(outerNames, definitions));
        }
        checkRep();
        return names; 
    }
    
    @Override
    public List<String> getReferencedLists(Map<String, ListExpression> definitions) {
        List<String> referenced = new ArrayList<>();
        elements.stream().map(e -> e.getReferencedLists(definitions))
            .forEach(l -> referenced.addAll(l));
        checkRep();
        return referenced;
    }

    @Override
    public List<String> getDefinedLists(Map<String, ListExpression> definitions) {
        List<String> defined = new ArrayList<>();
        elements.stream().map(e -> e.getDefinedLists(definitions))
            .forEach(l -> defined.addAll(l));
        checkRep();
        return defined;
    }
    
    @Override
    public boolean noForbiddenPipes(Map<String, ListExpression> definitions) {
        checkRep();
        return elements.stream()
                .map(e -> e.noForbiddenPipes(definitions))
                .reduce(true, Boolean::logicalAnd);
    }

}