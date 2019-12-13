package norn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class VisualizationTest {
 // Visualization tests
    // Number of subexpressions is
    // 0, 1, > 1
    //
    // Contains nested List Definitions, doesn't contain nested List Definitions
    // 
    // Contains/Doesn't contain each of the operators
    // 
    // Contains 1 type of operator, >1 type of operator
    // 
    // All partitions were also tested directly on web server to check that formatting the html works properly as well.
    
    // Covers where there is no subexpression and we have one type of operator.
    @Test
    public void testNoSubexpressions() throws InvalidExpressionException {
        ListExpression noSubExpressions = ListExpression.parse("benton@mit.edu, simon@mit.edu");
        String visualString = noSubExpressions.htmlString();
        String expectedVisualString = "(benton@mit.edu, simon@mit.edu)";
        assertEquals(expectedVisualString, visualString);
    }
    
    // Covers where there is one subexpression and we have one type of operator.
    @Test
    public void testOneSubexpressions() throws InvalidExpressionException {
        ListExpression oneSubExpressions = ListExpression.parse("b = benton@mit.edu, c; c = john@mit.edu; b");
        String visualString = oneSubExpressions.htmlString();
        String expectedVisualString = "((b=(benton@mit.edu, c)); (c=john@mit.edu); b)";
        assertEquals(expectedVisualString, visualString);
    }

    // Covers where there are multiple operators and sub expressions
    @Test
    public void testMultipleOperators() throws InvalidExpressionException {
        ListExpression multipleOperatos = ListExpression.parse("benton@mit.edu * yilinn@mit.edu , simon@mit.edu ! lucy@mit.edu, andrew@mit.edu");
        String visualString = multipleOperatos.htmlString();
        String expectedVisualString = "((benton@mit.edu &cap; yilinn@mit.edu), (simon@mit.edu &minus; lucy@mit.edu), andrew@mit.edu)";
        assertEquals(expectedVisualString, visualString);
    }
}
