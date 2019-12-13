package norn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an Email address expression.
 */
public class EmailAddress implements ListExpression {

    private final String address;
    
    /*
     * AF(address) = the email address with the given (case-insensitive) address <address>.
     * RI(address):
     *      true
     * SRE:
     * - only field is private final, and immutable. 
     * 
     * Thread safety argument:
     *     String is final and immutable, and all methods are self contained in terms of what they do, so other variables are confined.
     *     Note that if a user wants to open multiple threads that modify definitions, then they must deal with using a synchronized map to pass in
     */
    
    /**
     * Create an EmailAddress with the given address.
     * @param address the email address.
     */
    public EmailAddress(String address) {
        this.address = address.toLowerCase();
        checkRep();
    }
    
    /**
     * Fails an assertion if some part of our rep invariant has been broken
     */
    private void checkRep() {
        assert this.address != null;
    }
    
    @Override
    public ListExpression removeEdits(String outerDefinition, Map<String, ListExpression> definitions) {
        checkRep();
        return this;
    } 
    
    @Override
    public Set<EmailAddress> getMemberAddresses(Map<String, ListExpression> previousDefinitions) {
        checkRep();
        return Set.of(this);
    }
    
    /**
     * @return the email address string.
     */
    public String getAddress() {
        checkRep();
        return address;
    }
        
    @Override
    public String toString() {
        checkRep();
        return getAddress();
    }
    
    @Override
    public String htmlString() {
        checkRep();
        return getAddress();
    }

    @Override 
    public int hashCode() {
        checkRep();
        return address.hashCode();
    }

    @Override 
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EmailAddress other = (EmailAddress) obj;
        return other.address.equals(this.address);
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
