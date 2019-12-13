package norn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a list expression with the setIntersection operator.
 */
public class SetIntersection implements ListExpression {
    // AF(left, right) = a ListExpression that contains all of the email addresses in both left.getMemberAddresses()
    //     and right.getMemeberAddresses
    // RI:
    //     true
    // SRE:
    //     - all fields are private, final, and immutable
    // 
    // Safety from rep exposure:
    //     All parts of our rep are immutable and threadsafe, and we know that any calls to our edits function must have a threadsafe map
    //     passed in, so we know that calling new threads with this is fine.
    //     All over variables are confined in their method call.

    private final ListExpression left;
    private final ListExpression right;
    
    /**
     * Creates a new SetUnion instance with the given values
     * @param left the expression on the left of the SetUnion instance
     * @param right the expression on the right of the SetUnion instance
     */
    public SetIntersection(ListExpression left, ListExpression right) {
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
        return new SetIntersection(newLeft, newRight);
    } 
    
    @Override
    public Set<EmailAddress> getMemberAddresses(Map<String, ListExpression> previousDefinitions) {
        // just need to return the intersection of the addresses on the left and the right
        final Set<EmailAddress> output = new HashSet<>();   
        output.addAll(left.getMemberAddresses(previousDefinitions));
        output.retainAll(right.getMemberAddresses(previousDefinitions));
        checkRep();
        return output;
    }
    
    @Override
    public String toString(){
        checkRep();
        return "(" + left.toString() + "*" + right.toString() + ")";
    }

    @Override
    public boolean equals(Object that){
        // Just needs to check that the right and left expressions are equal
        if (that instanceof SetIntersection) {
            SetIntersection other = (SetIntersection) that;
            if (this.left.equals(other.left) && this.right.equals(other.right)){
                return true;
            } 
        }
        return false;
    }

    @Override
    public int hashCode(){
        checkRep();
        return Objects.hash(left.hashCode(), right.hashCode());
    }

    @Override
    public String htmlString() {
        checkRep();
        return "(" + left.htmlString() + " &cap; " + right.htmlString() + ")";
    }

    @Override
    public List<String> getDependentListNames(Set<String> outerNames, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        List<String> names = left.getDependentListNames(outerNames, definitions);
        names.addAll(right.getDependentListNames(outerNames, definitions));
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
