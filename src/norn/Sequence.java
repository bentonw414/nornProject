package norn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a sequence of n ListExpressions.
 */
public class Sequence implements ListExpression {
    // AF(elements) = A sequence of list expression consisting of these elements, ordered in that order.
    // RI:
    //     elements is of size >= 2
    // SRE:
    //     - elements is private and final, never returned and never modified in SetUnion class. 
    //       new List made from Constructor argument.
    //
    // Thread Safety Argument;
    //     Elements is a synchronized list, and it is never modified (so we don't need to worry about any race conditions with reading and writing).

    private final List<ListExpression> elements;

    /**
     * Creates a new instance of Sequence
     * @param elements the ListExpressions inside the Sequence
     */
    public Sequence(List<ListExpression> elements) {
        this.elements = new ArrayList<>(elements);
        checkRep();
    }

    /**
     * Fails an assertion if some part of our rep invariant has been broken.
     */
    private void checkRep() {
        assert this.elements.size() >= 2;
    }
    
    @Override
    public ListExpression removeEdits(String outerDefinition, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        List<ListExpression> newList = Collections.synchronizedList(new ArrayList<>());
        for (ListExpression e: elements) {
            newList.add(e.removeEdits(outerDefinition, definitions));
        }
        checkRep();
        return new Sequence(newList);
    } 
    
    @Override
    public Set<EmailAddress> getMemberAddresses(Map<String, ListExpression> previousDefinitions) {
        checkRep();
        return elements.get(elements.size() - 1).getMemberAddresses(previousDefinitions);
    }
    
    @Override
    public String toString() {
        checkRep();
        return "(" + elements.stream().map(e -> e.toString()).collect(Collectors.joining("; ")) + ")";
    }
    
    @Override
    public boolean equals(Object that) {
        if (that instanceof Sequence) {
            Sequence other = (Sequence) that;
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
        return "(" + elements.stream().map(e -> e.htmlString()).collect(Collectors.joining("; ")) + ")";
    }
    
    @Override
    public List<String> getDependentListNames(Set<String> outerNames, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        checkRep();
        return elements.get(elements.size() - 1).getDependentListNames(outerNames, definitions);
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