package norn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a new Pipe List expression (where the operator is the '|' symbol)
 */
public class Pipe implements ListExpression {
    // AF(left, right) = A list expression of the form left | right, where left and right are the list expressions to the
    //     left and the right of the pipe operator.
    // RI:
    //     true
    //
    // Thread Safety Argument:
    //     All parts of the rep (left and right) are threadsafe and immutable.
    //     For remove edits, we require that the client input a threadsafe map, so we don't need to worry about multiple threads for
    //     the definitions map as a parameter.
    //     Note that we spawn new threads in removeEdits, but this only happens when there is a synchronized map passed in (as we require in the spec).
    
    private final ListExpression left;
    private final ListExpression right;
    
    /**
     * Creates a new Pipe object instance
     * @param left the expression to the left side of the | operator
     * @param right the expression to the right side of the | operator
     */
    public Pipe(ListExpression left, ListExpression right) {
        this.left = left;
        this.right = right;
        checkRep();
    }
    
    /**
     * Fails an assertion if some part of our rep has been broken.
     */
    private void checkRep() {
        assert left != null;
        assert right != null;
    }
    
    @Override
    public ListExpression removeEdits(String outerDefinition, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        if (!noForbiddenPipes(definitions)) {
            throw new InvalidExpressionException("Expression contains pipes which may not be evaluated in parallel");
        }
        
        final ListExpression[] leftAndRight = new ListExpression[2];
        boolean[] wasException = new boolean[1];
        wasException[0] = false;
        
        Thread t1 = new Thread(() ->  {
            try {
                leftAndRight[0] = left.removeEdits(outerDefinition, definitions);
            } catch (InvalidExpressionException e) {
                wasException[0] = true;
            }
        });
        Thread t2 = new Thread(() ->  {
            try {
                leftAndRight[1] = right.removeEdits(outerDefinition, definitions);
            } catch (InvalidExpressionException e) {
                wasException[0] = true;
            }
        });
        t1.start();
        t2.start();
        
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new AssertionError("this should never happen");
        }
        if (wasException[0])
            throw new InvalidExpressionException("Expression contains pipes which may not be evaluated in parallel");
        
        checkRep();
        return new Pipe(leftAndRight[0], leftAndRight[1]);
        
    } 
    
    @Override
    public Set<EmailAddress> getMemberAddresses(Map<String, ListExpression> previousDefinitions) {
        checkRep();
        return new HashSet<>();
    }
    
    @Override
    public String toString() {
        checkRep();
        return "(" + left.toString() + "|" + right.toString() + ")";
    }
    
    @Override
    public boolean equals(Object that) {
        checkRep();
        if (that instanceof Pipe) {
            Pipe other = (Pipe) that;
            return this.left.equals(other.left) && this.right.equals(other.right);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        checkRep();
        return Objects.hash(left, right);
    }

    @Override
    public String htmlString() {
        checkRep();
        return "(" + left.htmlString() + " | " + right.htmlString() + ")";
    }

    @Override
    public List<String> getDependentListNames(Set<String> outerNames, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        checkRep();
        return new ArrayList<>(); 
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
        return defined;
    }

    @Override
    public boolean noForbiddenPipes(Map<String, ListExpression> definitions) {
        Set<String> leftReferenced = new HashSet<>(left.getReferencedLists(definitions));
        Set<String> rightReferenced = new HashSet<>(right.getReferencedLists(definitions));
        Set<String> leftDefined = new HashSet<>(left.getDefinedLists(definitions));
        Set<String> rightDefined = new HashSet<>(right.getDefinedLists(definitions));
        Set<String> leftOverlap = new HashSet<>(leftReferenced);
        leftOverlap.retainAll(rightDefined);
        Set<String> rightOverlap = new HashSet<>(rightReferenced);
        rightOverlap.retainAll(leftDefined);
        checkRep();
        return leftOverlap.isEmpty() 
                && rightOverlap.isEmpty()
                && left.noForbiddenPipes(definitions)
                && right.noForbiddenPipes(definitions);
    }
    
    

}
