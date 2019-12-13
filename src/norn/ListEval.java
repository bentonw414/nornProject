package norn;

import java.util.Map;
import java.util.Set;

/**
 * Data wrapper class containing output of visualization and evaluation of a ListExpression.
 * Since eval and visualize overlap in their computation, both are done in a single method of 
 * ListExpression and the method returns a ListEval.
 * 
 * For all purposes this can be thought of as a typed Tuple output.
 */
public class ListEval {
    // AF (emailAddresses, visualizationString, definitions) = the output of ListExpression.eval() with
    //      set of email addresses emailAddresses and visualization string visualizationString
    //      and definitions definitions.
    // RI
    //    true
    // SRE
    //    class is only used as a wrapper for returning method calls by ListExpression, so does not provide
    //      any guarantees of immutability or exposure prevention.
    
    private final Set<EmailAddress> emailAddresses;
    private final String visualizationString;
    private final Map<String, ListExpression> definitions;
    
    /**
     * Create the ListEval with the given set of email addresses and visualization string and definitions.
     * @param emailAddresses the email addresses
     * @param visualizationString the visualization string
     * @param definitions the definitions
     */
    public ListEval(Set<EmailAddress> emailAddresses, String visualizationString, Map<String, ListExpression> definitions) {
        this.emailAddresses = emailAddresses;
        this.visualizationString = visualizationString;
        this.definitions = definitions;
        checkRep();
    }
    
    /**
     * Fails an assertion if some part of our rep invariant has been broken
     */
    private void checkRep() {
        assert this.emailAddresses != null;
        assert this.visualizationString != null;
        assert this.definitions != null;
    }
    
    /**
     * Returns the set of email addresses associated with this instance.
     * @return the set of email addresses
     */
    public Set<EmailAddress> getEmailAddresses() {
        checkRep();
        return emailAddresses;
    } 
    
    /**
     * Returns the visualization string associated with this instance.
     * @return the visualization string
     */
    public String getVisualization() {
        checkRep();
        return visualizationString;
    }
    
    /**
     * This gets all the definitions associated with the ListEval object.
     * All the definitions includes the most recent list definition from a list object (where there are no edits in the mapping)
     * @return the definitions associated with the ListEval object
     */
    public Map<String, ListExpression> getDefinitions() {
        checkRep();
        return definitions;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + definitions.hashCode();
        result = prime * result + emailAddresses.hashCode();
        result = prime * result + visualizationString.hashCode();
        checkRep();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof ListEval))
            return false;
        ListEval other = (ListEval) obj;
        return this.emailAddresses.equals(other.emailAddresses)
                && this.visualizationString.equals(other.visualizationString)
                && this.definitions.equals(other.definitions);
    }
    
    
}
