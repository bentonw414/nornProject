package norn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * List class represents and empty ListExpression
 */
public class EmptyExpression implements ListExpression {
    // AF() = an empty listExpression
    // RI: true, as there is no rep
    // SRE: there is no rep, therefore there is no rep to be exposed
    // ThreadSafetyArgument:
    //     there is no rep, so all variables are confined to the functions they are called in (as no new threads are spawned)
        
    /**
     * Creates a new instance of an EmptyExpression
     */
    public EmptyExpression() {
        checkRep();
    }
    
    /**
     * Fails an assertion if some part of our rep invariant has been broken
     */
    private void checkRep() {
        assert true;
    }
    
    @Override
    public ListExpression removeEdits(String outerDefinition, Map<String, ListExpression> definitions) {
        checkRep();
        return this;
    } 
    
    @Override
    public Set<EmailAddress> getMemberAddresses(Map<String, ListExpression> previousDefinitions) {
        checkRep();
        return new HashSet<>();
    }
    
    @Override
    public String toString() {
        checkRep();
        return "";
    }
    
    @Override
    public boolean equals(Object that) {
        // any two empty expressions are equal
        if (that instanceof EmptyExpression) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        checkRep();
        return 0;
    }

    @Override
    public String htmlString() {
        checkRep();
        return " &empty; ";
    }
    
    @Override
    public List<String> getDependentListNames(Set<String> outerNames, Map<String, ListExpression> definitions)
            throws InvalidExpressionException {
        checkRep();
        return new ArrayList<>();
    }
    
    @Override
    public List<String> getReferencedLists(Map<String, ListExpression> definitions) {
        checkRep();
        return new ArrayList<>();
    }
    
    @Override
    public List<String> getDefinedLists(Map<String, ListExpression> definitions) {
        checkRep();
        return new ArrayList<>();
    }

    @Override
    public boolean noForbiddenPipes(Map<String, ListExpression> definitions) {
        checkRep();
        return true;
    }
}
