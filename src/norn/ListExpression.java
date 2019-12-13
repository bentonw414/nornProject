package norn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.mit.eecs.parserlib.UnableToParseException;

/**
 * Interface representing a list expression entered into the norn mailing list system.
 */
public interface ListExpression {
    // Datatype Definition
    // ListExpression = Listname(String: listname)
    //                  + Pipe(ListExpression:left, ListExpression:right)
    //                  + SetUnion(ListExpression. . . elements)
    //                  + Sequence(ListExpression. . . elements)
    //                  + SetDifference(ListExpression:left, ListExpression:right)
    //                  + SetIntersection(ListExpression:left, ListExpression:right)
    //                  + ListDefinition(String: listname, ListExpression: value)
    //                  + EmptyExpression()
    
    /**
     * Parse a ListExpression.
     * @param input expression to parse, as defined in the handout
     * @return expression AST for the input
     * @throws InvalidExpressionException if the input is syntactically invalid
     */
    public static ListExpression parse(String input) throws InvalidExpressionException {
        try {
            return ExpressionParser.parse(input);
        } catch (UnableToParseException e) {
            throw new InvalidExpressionException("Error: expression is syntactically invalid");
        }
    }
    
    /**
     * Evaluates a ListExpression, returning ListEval object containing 
     * the set of email addresses this expression evaluates to and an HTML-formatted string
     * of visualization explaining how the set of email addresses was computed.
     * @param e the list expression to evaluate and visualize
     * @param previousDefinitions map containing any previously defined list names
     * @return a ListEval object containing the set of email addresses and visualization string.
     * @throws InvalidExpressionException if the expression given cannot be evaluated, 
     *  for instance because it has circular definitions, or the pipes cannot be evaluated in parallel
     */
    public static ListEval evalAndVisualize(ListExpression e, Map<String, ListExpression> previousDefinitions) throws InvalidExpressionException {
        // put the old definitions into the new definitions.
        Map<String, ListExpression> definitions = Collections.synchronizedMap(new HashMap<>(previousDefinitions));
        String noOuter = ""; // to start, there is no outer definition, so do empty string, which won't match any definition.
        ListExpression noEdits = e.removeEdits(noOuter, definitions);
        
        String breakdown = noEdits.htmlString();
        
        List<String> listNamesToDefine = noEdits.getDependentListNames(new HashSet<>(), definitions);
        
        if (!noEdits.noForbiddenPipes(definitions))
            throw new InvalidExpressionException("Expression contains pipes which may not be evaluated in parallel");
        
        // so that we don't define something twice if its referenced twice
        Set<String> usedSoFar = new HashSet<>();
        
        List<String> dependentHtmls = new ArrayList<>();
        for (String name: listNamesToDefine) {
            if (!usedSoFar.contains(name)) {
                String dependentHtml = name + ": ";
                if (definitions.containsKey(name)) {
                    ListExpression value = definitions.get(name);
                    dependentHtml += value.htmlString();
                } else {
                    // if its a listname without a definition, we get here
                    dependentHtml += new EmptyExpression().htmlString();
                }
               
                usedSoFar.add(name);
                dependentHtmls.add(dependentHtml);
            }
        }
        
        String dependentString = dependentHtmls.stream()
                .collect(Collectors.joining("<hr>"));
        String dependentLine = " <strong> Which is dependent on definitions: </strong>";
        String visualization = breakdown + "<hr> <p>" + dependentLine + "</p>" + dependentString;
        
        // now this is safe, because we've checked for circular definitions.
        Set<EmailAddress> addresses = noEdits.getMemberAddresses(definitions);
        ListEval output = new ListEval(addresses, visualization, definitions);
        return output;
    }
    
    
    /**
     * Returns whether the expression does not contain any forbidden pipes,
     * as defined in the project spec. 
     * Requires that definitions is a threadsafe data type.
     * @param definitions the set of definitions for this expression
     * @return whether the expression does not contain any forbidden pipes.
     */
    public boolean noForbiddenPipes(Map<String, ListExpression> definitions);
    
    /**
     * @return an html-formatted string representation of the object.
     */
    public String htmlString();
    
    /**
     * Returns a list of list names referenced in this ListExpression.
     * In order to count as referenced, the ListName has to contribute to the final set of email addresses
     * outputted by this expression.
     * List names are included in order of inclusion from left to right. If a list name is included more than once
     * in the expression, the list name is in the list for each time it's included.
     * @param definitions a map between list names and definitions for this expression.
     * @param outerNames a set of names of definitions that have already been referenced in the list expression
     * @return a list of list names referenced in this ListExpression. 
     * @throws InvalidExpressionException if for some reason we run into an invalid expression as we evaluation (a loop in definitions or invalid pipe)
     */
    public List<String> getDependentListNames(Set<String> outerNames, Map<String, ListExpression> definitions)
            throws InvalidExpressionException;
    
    /**
     * Returns a list of all listnames either referenced or defined somewhere in the expression.
     * @param definitions a map between list names and definitions for this expression. 
     * @return a list of all listnames either referenced or defined somewhere in the expression.
     */
    public List<String> getReferencedLists(Map<String, ListExpression> definitions);
    
    /**
     * Returns a list of all listnames defined somewhere in the expression.
     * @param definitions a map between list names and definitions for this expression. 
     * @return a list of all listnames defined somewhere in the expression.
     */
 
    public List<String> getDefinedLists(Map<String, ListExpression> definitions);
        
    /**
     * Returns a new ListExpression object where all edits have been taken out, or replaced as best they can
     * Populates the provided map with definitions found in the list expression, replacing definitions as we go.
     * @param outerDefinition the most recent definition.
     * @param definitions the definitions for all of the list expressions seen so far (which may not contains any edits)
     * @return a new listExpression where all of the edits have been removed.
     * @throws InvalidExpressionException if the expression contains a pipe whose left and right sides cannot be safely evaluated in parallel.
     */
    public ListExpression removeEdits(String outerDefinition, Map<String, ListExpression> definitions)
            throws InvalidExpressionException;
    
    /**
     * Returns a set of member addresses on this mailing list.
     * A member is an email address, not another mailing list, but sublist members are added recursively.
     * @param previousDefinitions the definitions that we want to use when evaluating listnames in the string
     * @return a Set of the member addresses.
     */
    public Set<EmailAddress> getMemberAddresses(Map<String, ListExpression> previousDefinitions);
    
    /**
     * @return a parsable representation of this ListExpression
     */
    @Override 
    public String toString();

    /**
     * @param that any object
     * @return true if and only if this and that are structurally-equal
     *         ListExpressions
     */
    @Override
    public boolean equals(Object that);
    
    /**
     * @return hash code value consistent with the equals() definition of structural
     *         equality
     */
    @Override
    public int hashCode();
}
