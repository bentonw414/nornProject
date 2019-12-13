/* Copyright (c) 2018 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package norn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class EvalTest {
    // Evaluation tests
    // Number of subexpressions is
    // 0, 1, > 1
    //
    // Contains nested List Definitions, doesn't contain nested List Definitions
    // 
    // Contains/Doesn't contain each of the operators
    // 
    // Contains 1 type of operator, >1 type of operator
    //
    // Expressions are given in sequence, expressions are all in one sequence of expressions (using ";" operators)
    //
    // 
    // Circularly linked definitions with two different list names  (or not)
    // Editing a predefined list by referencing itself (or not)
    // Pipe side contains list names appearing in other side (or not)
    // Infinite recursion gives an error without crashing (or not)
    // Pipe updates addresses to be used later (or not)
    // Test editing list (in its own name) (like a = a,benton@mit.edu) and not
    // 
    
    public static final EmailAddress Y = new EmailAddress("yilinn@mit.edu");
    public static final EmailAddress B = new EmailAddress("benton@mit.edu");
    public static final EmailAddress S = new EmailAddress("simon@mit.edu");
    public static final EmailAddress L = new EmailAddress("lucy@mit.edu");
    public static final EmailAddress A = new EmailAddress("andrew@mit.edu");
    
    //Test listname with one subexpression 
    @Test
    public void testOneListname() throws InvalidExpressionException {
        ListExpression e1 = ListExpression.parse("benton");
        assertEquals(Set.of(), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
    }
    
    //Test union with one subexpression
    @Test
    public void testOneUnion() throws InvalidExpressionException {
        ListExpression e1 = ListExpression.parse("benton@mit.edu, yilinn@mit.edu");
        assertEquals(Set.of(Y, B), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        
        ListExpression e2 = ListExpression.parse("benton@mit.edu, benton@mit.edu");
        assertEquals(Set.of(B), ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
    }
    
    //Test intersection with one subexpression
    @Test
    public void testOneIntersection() throws InvalidExpressionException {
        ListExpression e1 = ListExpression.parse("benton@mit.edu *   yilinn@mit.edu");
        assertEquals(Set.of(), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        
        ListExpression e2 = ListExpression.parse("benton@mit.edu*benton@mit.edu");
        assertEquals(Set.of(B), ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
    }
    
    //Test definition with one subexpression
    @Test
    public void testOneDefinition() throws InvalidExpressionException {
        ListExpression e1 = ListExpression.parse("s=simon@mit.edu");
        assertEquals(Set.of(S), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
    }
    
    //Test pipe with one subexpression
    @Test
    public void testOnePipe() throws InvalidExpressionException {
        ListExpression e1 = ListExpression.parse("benton@mit.edu |   yilinn@mit.edu");
        assertEquals(Set.of(), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
    }
    
    //Test sequence with one subexpression
    @Test
    public void testOneSequence() throws InvalidExpressionException {
        ListExpression e1 = ListExpression.parse("benton@mit.edu;yilinn@mit.edu");
        assertEquals(Set.of(Y), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        
        ListExpression e2 = ListExpression.parse("benton@mit.edu;");
        assertEquals(Set.of(), ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
    }
    
    // Test editing list
    @Test
    public void testEditingList() throws InvalidExpressionException {
        ListExpression e1 = ListExpression.parse("b=benton@mit.edu");
        e1 = add(e1, ListExpression.parse("b=b,yilinn@mit.edu;b"));
        assertEquals(Set.of(B,Y), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        
        ListExpression e2 = ListExpression.parse("b=benton@mit.edu; b=yilinn@mit.edu,b;b");
        assertEquals(Set.of(B,Y), ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        
        ListExpression e3 = ListExpression.parse("b=yilinn@mit.edu,b");
        assertEquals(Set.of(Y), ListExpression.evalAndVisualize(e3, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        e3 = add(e3, ListExpression.parse("b=benton@mit.edu;b"));
        assertEquals(Set.of(B), ListExpression.evalAndVisualize(e3, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
    }
    
    // Tests case we were having difficulty with for parsing
    @Test
    public void testWeirdCase() throws InvalidExpressionException {
        ListExpression e1 = ListExpression.parse("a,b=yilinn@mit.edu");
        assertEquals(Set.of(Y), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        e1 = add(e1, ListExpression.parse("a"));
        assertEquals(Set.of(), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of addresses");
        e1 = add(e1, ListExpression.parse("b"));
        assertEquals(Set.of(Y), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of addresses");
    }
    
    // Tests operator precedence with the operators for union, intersection and difference
    @Test
    public void testBasicOperatorsPrecedence() throws InvalidExpressionException {
        ListExpression e1 = ListExpression.parse("benton@mit.edu * yilinn@mit.edu , simon@mit.edu ! lucy@mit.edu, andrew@mit.edu");
        assertEquals(Set.of(A,S), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        
        ListExpression e2 = ListExpression.parse("benton@mit.edu * yilinn@mit.edu ! simon@mit.edu , lucy@mit.edu, andrew@mit.edu");
        assertEquals(Set.of(L,A), ListExpression.evalAndVisualize(e2, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        
        ListExpression e3 = ListExpression.parse("benton@mit.edu ! yilinn@mit.edu * benton@mit.edu , lucy@mit.edu, andrew@mit.edu");
        assertEquals(Set.of(B, L ,A), ListExpression.evalAndVisualize(e3, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        
        ListExpression e4 = ListExpression.parse("benton@mit.edu ! yilinn@mit.edu , benton@mit.edu * lucy@mit.edu, andrew@mit.edu");
        assertEquals(Set.of(B,A), ListExpression.evalAndVisualize(e4, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        
        ListExpression e5 = ListExpression.parse("benton@mit.edu , yilinn@mit.edu * yilinn@mit.edu ! lucy@mit.edu, andrew@mit.edu");
        assertEquals(Set.of(Y, B, A), ListExpression.evalAndVisualize(e5, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        
        ListExpression e6 = ListExpression.parse("benton@mit.edu , simon@mit.edu ! simon@mit.edu * lucy@mit.edu, andrew@mit.edu");
        assertEquals(Set.of(S, B, A), ListExpression.evalAndVisualize(e6, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
    }
    
    // Tests nested list definitions with definitions coming in different orders.
    // Also makes sure to test using both adding new expressions one at a time or all together using the sequence operator
    @Test
    public void testNestedListDefinitions() throws InvalidExpressionException {
        ListExpression e1a = ListExpression.parse("b = benton@mit.edu; a = b, yilinn@mit.edu, simon@mit.edu; a");
        assertEquals(Set.of(S,Y,B), ListExpression.evalAndVisualize(e1a, new HashMap<>()).getEmailAddresses());
        
        ListExpression e1b = ListExpression.parse("b = benton@mit.edu");
        e1b = add(e1b, ListExpression.parse("a = b, yilinn@mit.edu, simon@mit.edu"));
        e1b = add(e1b, ListExpression.parse("a"));
        assertEquals(Set.of(S,Y,B), ListExpression.evalAndVisualize(e1b, new HashMap<>()).getEmailAddresses());
        
        ListExpression e2a = ListExpression.parse("a = b, yilinn@mit.edu, simon@mit.edu; b = benton@mit.edu; a");
        assertEquals(Set.of(S,Y,B), ListExpression.evalAndVisualize(e2a, new HashMap<>()).getEmailAddresses());
        e2a = add(e2a, ListExpression.parse("b"));
        assertEquals(Set.of(B), ListExpression.evalAndVisualize(e2a, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
        
        ListExpression e2b = ListExpression.parse("a = b, yilinn@mit.edu, simon@mit.edu");
        e2b = add(e2b, ListExpression.parse("b = benton@mit.edu;"));
        e2b = add(e2b, ListExpression.parse("a"));
        assertEquals(Set.of(S,Y,B), ListExpression.evalAndVisualize(e2b, new HashMap<>()).getEmailAddresses());
    }
    
    // Tests a larger version of union and intersection (covers case with >1 in the overlap as well as outside of the overlap)
    @Test
    public void testBigUnionAndIntersection() throws InvalidExpressionException {
        ListExpression e1 = ListExpression.parse("(benton@mit.edu, simon@mit.edu, andrew@mit.edu, albert@mit.edu, taylor@mit.edu, derek@mit.edu) *   (simon@mit.edu, yilinn@mit.edu, benton@mit.edu, lucy@mit.edu)");
        assertEquals(Set.of(B,S), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses());
    }
    
    // This tests for parallel definitions to make sure that they actually effect the list definitions (makes sure that they are doing something)
    @Test
    public void testParallelDefinitionsUsedLater() throws InvalidExpressionException {
        ListExpression e1 = ListExpression.parse("(thing = benton@mit.edu, yilinn@mit.edu | other = benton@mit.edu, simon@mit.edu; track = andrew@mit.edu | x = this@wow.com); thing, other, track");
        assertEquals(Set.of(Y, A, S, B), ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses(), "got wrong set of email addresses");
    } 
    
    // This test invalid parallel expressions when the references that are invalid are both directly and indirectly referenced
    @Test
    public void testParallelError() throws InvalidExpressionException {
        // first test a direct reference
        ListExpression e1 = ListExpression.parse("(a = benton@mit.edu | a = a, yilinn@mit.edu); a");
        assertThrows(InvalidExpressionException.class, () -> ListExpression.evalAndVisualize(e1, new HashMap<>()));
        
        // second test an indirect reference
        ListExpression e2 = ListExpression.parse("c = a, b@mit.edu; (a = benton@mit.edu | c = c, simon@mit.edu); a");
        assertThrows(InvalidExpressionException.class, () -> ListExpression.evalAndVisualize(e2, new HashMap<>()));
    }
    
    @Test
    public void testLoops() throws InvalidExpressionException {
        boolean failed = false;
        try {
            ListExpression e1 = ListExpression.parse("a = b; b = a");
            ListExpression.evalAndVisualize(e1, new HashMap<>()).getEmailAddresses();
        } catch (InvalidExpressionException e) {
            // supposed to happen
            failed = true;
        }
        assertTrue(failed, "should have failed");
    }
    
    /**
     * Used for testing. Creates a sequence out of two listExpressions
     * @param e1 the first expression
     * @param e2 the second expression
     * @return a sequence out of the two expressions.
     */
    private static ListExpression add(ListExpression e1, ListExpression e2) {
        return new Sequence(List.of(e1, e2));
    }
    
}