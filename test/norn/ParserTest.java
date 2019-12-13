package norn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Set;

import org.junit.jupiter.api.Test;

import edu.mit.eecs.parserlib.UnableToParseException;

public class ParserTest {
    /*
     * Things to test:
     * 
     * union ,
     * difference !
     * intersection *
     * parentheses ()
     * 
     * precedence: ()  *  ,  !. =  ;  |
     * 
     * listname = e
     * returns the expression
     * x=a,b gives x=(a,b)
     * 
     * sequence ;
     * parallel |
     * 
     * a=b=x should give a = (b=x)
     * a,b=x
     * 
     * Semicolons have lower precedence than all the operators above.
     * Pipes have the lowest precedence of all the operators.
     * 
     * Lists that depend on each other can be created in any order
     * 
     * Undefined lists are empty
     * 
     * A list can be edited
     * 
     * Dependent lists should see the edits
     * 
     * Errors are well-defined
     * 
     * Subexpressions e and f are forbidden to define any list names that also appear in the other subexpression:
     * if they do, your system should produce an error with an informative error message
     * 
     * Username: nonempty case-insensitive strings of letters, digits, underscores, dashes, periods, and plus signs
     * Domain name: nonempty case-insensitive strings of letters, digits, underscores, dashes, and periods
     * List names are nonempty case-insensitive strings of letters, digits, underscores, dashes, and periods (e.g. course.6).
     * 
     * Partitions:
     * - no nested operations, some nested operations
     * - partitions where operators do and don't take precedence for ()  *  ,  !  =  ;  |
     * - using or not using ()  *  ,  !  =  ;  |
     * - exists undefined list or not
     * - edited list or not
     * - error or not
     * - username valid or not
     * - domain name valid or not
     * - listname valid or not
     * - create lists in any order or not
     * - a=b=x should give a = (b=x) (or not)
     * - a,b=x likewise (or not)
     * - multiple in a row for sequence, parallel (or not)
     * - whitespace, no whitespace
     * - valid, invalid pipe subexpressions
     * 
     * Because the method names correspond pretty exactly to what's being tested, I won't label each with a comment.
     */
    
    public static final String A = "a@mit.edu";
    public static final String B = "b@mit.edu";
    public static final String C = "c@mit.edu";
    public static final EmailAddress A_ADDRESS = new EmailAddress(A);
    public static final EmailAddress B_ADDRESS = new EmailAddress(B);
    public static final EmailAddress C_ADDRESS = new EmailAddress(C);
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    @Test
    public void usernamePattern() throws UnableToParseException, InvalidExpressionException {
        // nonempty
        assertBadUsername("");
        // case-insensitive
        assertEquals(ExpressionParser.parse("a@mit.edu"), ExpressionParser.parse("A@mit.edu"), "addresses need to be case insensitive");
        // letters, digits, underscores, dashes, periods, plus signs
        assertGoodUsername("a");
        assertGoodUsername("1");
        assertGoodUsername("_");
        assertGoodUsername(".");
        assertGoodUsername("+");
        assertGoodUsername("a1_.+");
        assertBadUsername("&");
    }
    
    
    private void assertBadUsername(String username) {
        assertThrows(UnableToParseException.class, () -> ExpressionParser.parse(username + "@mit.edu"));
    }
    
    private void assertGoodUsername(String username) {
        try {
            ExpressionParser.parse(username + "@mit.edu");
        } catch (UnableToParseException e) {
            fail("shouldn't have gotten error");
        }
    }
    
    @Test
    public void domainNamePattern() throws UnableToParseException, InvalidExpressionException {
        // letters, digits, underscores, dashes, and periods
        // nonempty
        assertBadDomainName("");
        // case-insensitive
        assertEquals(ExpressionParser.parse("a@mit.edu"), ExpressionParser.parse("a@MIT.edu"), "addresses need to be case insensitive");
        // letters, digits, underscores, dashes, periods
        assertGoodDomainName("s");
        assertGoodDomainName("12");
        assertGoodDomainName("_");
        assertGoodDomainName("--");
        assertGoodDomainName(".");
        assertGoodDomainName("s2.-_H");
        assertBadDomainName("+");
        assertBadDomainName("..dsfsdfsdf+Df");
    }
    
    private void assertBadDomainName(String domainName) {
        assertThrows(UnableToParseException.class, () -> ExpressionParser.parse("a@" + domainName));
    }
    
    private void assertGoodDomainName(String domainName) {
        try {
            ExpressionParser.parse("a@" + domainName);
        } catch (UnableToParseException e) {
            fail("shouldn't have gotten error");
        }
    }
    
    @Test
    public void listnamePattern() throws UnableToParseException, InvalidExpressionException {
        // nonempty
        assertBadListname("");
        // case-insensitive

        ListExpression e1 = ExpressionParser.parse("a@mit.edu");
        ListExpression e2 = ExpressionParser.parse("l=b@mit.edu; L=a@mit.edu; l");
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses(), "got different addresses");
        
        // letters, digits, underscores, dashes, and periods (e.g. course.6).
        assertGoodListname("hello");
        assertGoodListname("hell0000");
        assertGoodListname("----");
        assertGoodListname("....");
        assertGoodListname("whatareyou....0000---");
        assertBadListname("not&");
    }
    
    private void assertBadListname(String listname) {
        assertThrows(UnableToParseException.class, () -> ExpressionParser.parse(listname + "= a@mit.edu; " + listname));
    }
    
    private void assertGoodListname(String listname) {
        try {
            ExpressionParser.parse(listname + "= a@mit.edu; " + listname);
        } catch (UnableToParseException e) {
            fail("shouldn't have gotten error");
        }
    }
    
    @Test
    public void emptyList() throws UnableToParseException, InvalidExpressionException {

        ListExpression e1 = ExpressionParser.parse("undefinedlist");
        ListExpression e2 = ExpressionParser.parse("undefined2");
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses(), "should both be empty sets");
        assertEquals(Set.of(), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses());
    }
    
    @Test
    public void singleRecipient() throws UnableToParseException, InvalidExpressionException {
        ListExpression e1 = ExpressionParser.parse("a@mit.edu");
        assertEquals(e1, new EmailAddress("a@mit.edu")); 
        assertEquals(Set.of(new EmailAddress("a@mit.edu")), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses());
    }
    
    @Test
    public void union() throws UnableToParseException, InvalidExpressionException {
        ListExpression e1 = ExpressionParser.parse(A + ", " + B + ", " + C);
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), Set.of(A_ADDRESS, B_ADDRESS, C_ADDRESS));
    }
    
    @Test
    public void difference() throws UnableToParseException, InvalidExpressionException {
        ListExpression e1 = ExpressionParser.parse("( a@mit.edu, b@mit.edu, c@mit.edu) ! (a@mit.edu, c@mit.edu)");
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), Set.of(B_ADDRESS));
    }
    
    @Test
    public void intersection() throws UnableToParseException, InvalidExpressionException {
        ListExpression e1 = ExpressionParser.parse("( a@mit.edu, b@mit.edu, c@mit.edu) * (a@mit.edu, c@mit.edu)");
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), Set.of(A_ADDRESS, C_ADDRESS));
    }
    
    @Test
    public void equals() throws UnableToParseException, InvalidExpressionException {
        ListExpression e1 = ExpressionParser.parse("list=a@mit.edu");
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), Set.of(A_ADDRESS));
        ListExpression e2 = ExpressionParser.parse("list=a@mit.edu; list2=list");
        assertEquals(ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses(), Set.of(A_ADDRESS));
    }
    
    @Test
    public void sequence() throws UnableToParseException, InvalidExpressionException {
        ListExpression e1 = ExpressionParser.parse("list=a@mit.edu; list2=b@mit.edu");
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), Set.of(B_ADDRESS));
    }
    
    @Test
    public void parallel() throws UnableToParseException, InvalidExpressionException {
        ListExpression e1 = ExpressionParser.parse("list=a@mit.edu | list2=b@mit.edu");
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), Set.of());
        // With new spec, parser does not through exception for invalid parallels; eval should though
//        assertThrows(UnableToParseException.class, () -> ExpressionParser.parse("x = a@mit.edu | y = x,b@mit.edu"));
    }
    
    @Test
    public void unionPrecedence() throws UnableToParseException, InvalidExpressionException {
        // order: ()  *  ,  ! = ;  |
        
        // * takes precedence
        ListExpression e1 = ExpressionParser.parse("a@mit.edu, c@mit.edu * b@mit.edu, c@mit.edu");
        ListExpression e2 = ExpressionParser.parse("a@mit.edu, (c@mit.edu * b@mit.edu), c@mit.edu");
        assertEquals(e1, e2);
        
        // everything else takes lower precedence 
        // = 
        e1 = ExpressionParser.parse("l=a@mit.edu, c@mit.edu; l");
        e2 = ExpressionParser.parse("a@mit.edu, c@mit.edu");
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses());
        
        // !
        e1 = ExpressionParser.parse("a@mit.edu, c@mit.edu ! a@mit.edu");
        e2 = ExpressionParser.parse("a@mit.edu, c@mit.edu");
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses());
       
        // ;
        e1 = ExpressionParser.parse("a@mit.edu, c@mit.edu ; a@mit.edu");
        e2 = ExpressionParser.parse("(a@mit.edu, c@mit.edu) ; a@mit.edu");
        assertEquals(e1, e2);
        
        // |
        e1 = ExpressionParser.parse("a@mit.edu, c@mit.edu | a@mit.edu");
        e2 = ExpressionParser.parse("(a@mit.edu, c@mit.edu) | a@mit.edu");
        assertEquals(e1, e2);
    }
    
    @Test
    public void differencePrecedence() throws UnableToParseException, InvalidExpressionException {
        // order: ()  *  ,  ! = ;  |
        
        // * takes precedence
        ListExpression e1 = ExpressionParser.parse("a@mit.edu ! a@mit.edu * b@mit.edu");
        ListExpression e2 = ExpressionParser.parse("a@mit.edu !(a@mit.edu * b@mit.edu)");
        assertEquals(e1, e2);
        
        // = takes precedence
        e1 = ExpressionParser.parse("l=a@mit.edu ! a@mit.edu; l");
        e2 = ExpressionParser.parse("(l=a@mit.edu ! a@mit.edu); l");
        assertEquals(e1, e2);

        
        // everything else lower precedence
        // ;
        e1 = ExpressionParser.parse("a@mit.edu ! c@mit.edu ; a@mit.edu");
        e2 = ExpressionParser.parse("(a@mit.edu ! c@mit.edu) ; a@mit.edu");
        assertEquals(e1, e2);
        
        // |
        e1 = ExpressionParser.parse("a@mit.edu ! c@mit.edu | a@mit.edu");
        e2 = ExpressionParser.parse("(a@mit.edu ! c@mit.edu) | a@mit.edu");
        assertEquals(e1, e2);
    }
    
    @Test
    public void intersectionPrecedence() throws UnableToParseException, InvalidExpressionException {
        // order: ()  *  ,  ! = ;  |
        
        // everything else lower precedence
        
        // =
        ListExpression e1 = ExpressionParser.parse("list = a@mit.edu * c@mit.edu; list");
        ListExpression e2 = ExpressionParser.parse("list = (a@mit.edu * c@mit.edu); list");
        assertEquals(e1, e2);
        
        // ;
        e1 = ExpressionParser.parse("a@mit.edu * c@mit.edu ; a@mit.edu");
        e2 = ExpressionParser.parse("(a@mit.edu * c@mit.edu) ; a@mit.edu");
        assertEquals(e1, e2);
        
        // |
        e1 = ExpressionParser.parse("a@mit.edu * c@mit.edu | a@mit.edu");
        e2 = ExpressionParser.parse("(a@mit.edu * c@mit.edu) | a@mit.edu");
        assertEquals(e1, e2);
    }
    
    @Test
    public void equalsPrecedence() throws UnableToParseException, InvalidExpressionException {
        // order: ()  *  ,  ! = ;  |
        
        // everything else lower precedence
        // ;
        ListExpression e1 = ExpressionParser.parse("list = a@mit.edu; b@mit.edu");
        ListExpression e2 = ExpressionParser.parse("(list = a@mit.edu) ; b@mit.edu");
        assertEquals(e1, e2);
        
        // ;
        e1 = ExpressionParser.parse("list = a@mit.edu | b@mit.edu");
        e2 = ExpressionParser.parse("(list = a@mit.edu) | b@mit.edu");
        assertEquals(e1, e2);
    }
    
    @Test
    public void sequencePrecedence() throws UnableToParseException, InvalidExpressionException {
        
        // pipe has lower precedence
        ListExpression e1 = ExpressionParser.parse("a@mit.edu; b@mit.edu | c@mit.edu");
        ListExpression e2 = ExpressionParser.parse("(a@mit.edu; b@mit.edu) | c@mit.edu");
        assertEquals(e1, e2);
    }
    
    @Test
    public void equalsNonprecedence() throws UnableToParseException, InvalidExpressionException {
        
        ListExpression e1 = ExpressionParser.parse("a=b=x@mit.edu; a");
        ListExpression e2 = ExpressionParser.parse("a=(b=x@mit.edu); a");
        assertEquals(e1, e2);

        e1 = ExpressionParser.parse("a=s@mit.edu;a,b=x@mit.edu");
        e2 = ExpressionParser.parse("a=s@mit.edu;a,(b=x@mit.edu)");
        assertEquals(e1, e2);
        
        e1 = ExpressionParser.parse("a=s@mit.edu,x@mit.edu;a!b=x@mit.edu");
        e2 = ExpressionParser.parse("a=s@mit.edu,x@mit.edu;a!(b=x@mit.edu)");
        assertEquals(e1, e2);
        
        e1 = ExpressionParser.parse("a=s@mit.edu,x@mit.edu;a*b=x@mit.edu");
        e2 = ExpressionParser.parse("a=s@mit.edu,x@mit.edu;a*(b=x@mit.edu)");
        assertEquals(e1, e2);
    }
    
    @Test
    public void editedLists() throws UnableToParseException, InvalidExpressionException {
        ListExpression e1 = ExpressionParser.parse("x=a@mit.edu; x=b@mit.edu");
        ListExpression e2 = ExpressionParser.parse("x=b@mit.edu");
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses());
        assertEquals(Set.of(new EmailAddress("B@mit.edu")), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "should be correct set of email addresses");
    }
    
    @Test
    public void dependentLists() throws UnableToParseException, InvalidExpressionException {
        
        ListExpression e1 = ExpressionParser.parse("x=a@mit.edu; y=x; x=b@mit.edu; y");
        ListExpression e2 = ExpressionParser.parse("x=b@mit.edu; y=x");
        assertEquals(ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses());
        assertEquals(Set.of(new EmailAddress("b@mit.edu")) ,ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of addresses");
    }
    
    @Test
    public void whitespace() throws UnableToParseException, InvalidExpressionException {    
        ListExpression e1 = ExpressionParser.parse("x = a@mit.edu,      b@mit.edu");
        ListExpression e2 = ExpressionParser.parse("x=a@mit.edu,b@mit.edu");
        assertEquals(e1, e2);
    }
}
