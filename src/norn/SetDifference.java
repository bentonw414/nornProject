package norn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a ListExpression with the set difference operator "!".
 */
public class SetDifference implements ListExpression {
    // AF(left, right) = A set difference operation with all the email addresses in left.getMemberAddresses,
    //     that are not also in right.getMemberAddresses
    // RI:
    //     true
    // SRE:
    //     all parts of the rep are private, final, and immutable.
    //
    // Safety from rep exposure:
    //     All parts of our rep are immutable and threadsafe, and we know that any calls to our edits function must have a threadsafe map
    //     passed in, so we know that calling new threads with this is fine.
    //     All over variables are confined in their method call.
    
    
    private final ListExpression left;
    private final ListExpression right;
    
    /**
     * Creates a new SetDifference instance.
     * @param left the expression to the left of the "!" operator
     * @param right the expression to the right of the "!" operator
     */
    public SetDifference(ListExpression left, ListExpression right) {
        this.left = left;
        this.right = right;
        checkRep();
    }
    
    /**
     * Fails an assertion if any part of our rep has been broken.
     */
    private void checkRep() {
        assert left != null;
        assert right != null;
    }
    
    @Override
    public ListExpression removeEdits(String outerDefinition, Map<String, ListExpression> definitions) 
            throws InvalidExpressionException {
        ListExpression newLeft = left.removeEdits(outerDefinition, definitions);
        ListExpression newRight = right.removeEdits(outerDefinition, definitions);
        checkRep();
        return new SetDifference(newLeft, newRight);
    } 
    
    @Override
    public Set<EmailAddress> getMemberAddresses(Map<String, ListExpression> previousDefinitions) {
        final Set<EmailAddress> output = new HashSet<>();
        output.addAll(left.getMemberAddresses(previousDefinitions));
        output.removeAll(right.getMemberAddresses(previousDefinitions));
        checkRep();
        return output;
    }
    
    @Override
    public String toString() {
        checkRep();
        return "(" + left.toString() + "!" + right.toString() + ")";
    }
    
    @Override
    public boolean equals(Object that) {
        // Just needs to check that the right and left expressions are equal
        checkRep();
        if (that instanceof SetDifference) {
            SetDifference other = (SetDifference) that;
            if (this.left.equals(other.left) && this.right.equals(other.right)){
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        checkRep();
        return Objects.hash(left.hashCode(), right.hashCode());
    }

    @Override
    public String htmlString() {
        checkRep();
        return "(" + left.htmlString() + " &minus; " + right.htmlString() + ")";
    }

    @Override
    public List<String> getDependentListNames(Set<String> outerNames, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        List<String> names = left.getDependentListNames(outerNames, definitions);
        names.addAll(right.getDependentListNames(outerNames, definitions));
        checkRep();
        return names; 
    }

    @Override
    public List<String> getReferencedLists(Map<String, ListExpression> definitions) {
        List<String> referenced = new ArrayList<>();
        referenced.addAll(left.getReferencedLists(definitions));
        referenced.addAll(right.getReferencedLists(definitions));
        checkRep();
        return referenced;
    }

    @Override
    public List<String> getDefinedLists(Map<String, ListExpression> definitions) {
        List<String> defined = new ArrayList<>();
        defined.addAll(left.getDefinedLists(definitions));
        defined.addAll(right.getDefinedLists(definitions));
        checkRep();
        return defined;
    }

    @Override
    public boolean noForbiddenPipes(Map<String, ListExpression> definitions) {
        checkRep();
        return left.noForbiddenPipes(definitions) 
                && right.noForbiddenPipes(definitions);
    }

}
