package norn;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import edu.mit.eecs.parserlib.ParseTree;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;

/**
 * Static class for parsing ListExpressions.
 */
public class ExpressionParser {
    
    // the nonterminals of the grammar
    private static enum ListExpressionGrammar {
        EXPRESSION, SEQUENCE, SETUNION, SETDIFFERENCE, SETINTERSECTION, PRIMITIVE,
        EMAILADDRESS, EMPTYEXPRESSION, USERNAME, DOMAIN, WHITESPACE, LISTNAME, LISTDEFINITION,
        PIPE
    }

    private static Parser<ListExpressionGrammar> parser = makeParser();
    
    /**
     * Compile the grammar into a parser.
     * 
     * @return parser for the grammar
     * @throws RuntimeException if grammar file can't be read or has syntax errors
     */
    private static Parser<ListExpressionGrammar> makeParser() {
        try {
            // read the grammar as a file, relative to the project root.
            final File grammarFile = new File("src/norn/ListExpressionGrammar.g");
            return Parser.compile(grammarFile, ListExpressionGrammar.EXPRESSION);
            
        // Parser.compile() throws two checked exceptions.
        // Translate these checked exceptions into unchecked RuntimeExceptions,
        // because these failures indicate internal bugs rather than client errors
        } catch (IOException e) {
            throw new RuntimeException("can't read the grammar file", e);
        } catch (UnableToParseException e) {
            throw new RuntimeException("the grammar has a syntax error", e);
        }
    }
    
    /**
     * Parses an expression for email addresses.  Supports the "," , "!", and "*" operators.
     * Email addresses are defined as a username followed by a domain name, with an "@" symbol between them.
     * 
     * Parsing is done as specified in the project specifications.
     * Behavior is undetermined in the case of nested definitions of the same variable, such as a=(a=c).   
     * 
     * Usernames are defined as nonempty case-insensitive strings of letters, digits, underscores, dashes, periods, and plus signs (e.g. bitdiddle+nospam).
     * Domain name are defined as nonempty case-insensitive strings of letters, digits, underscores, dashes, and periods.
     * 
     * This also modifies previousDefinitions to include all new definitions in the string to parse.
     * 
     * @param string the string we want to parse into a ListExpression
     * @return ListExpression parsed from the string.
     * @throws UnableToParseException if the string does not match the ListExpressionGrammar
     */
    public static ListExpression parse(final String string) throws UnableToParseException {
        // parse the example into a parse tree
        final ParseTree<ListExpressionGrammar> parseTree = parser.parse(string);
        // display the parse tree in various ways, for debugging only
        // System.out.println("parse tree " + parseTree);
        // Visualizer.showInBrowser(parseTree);

        // make an AST from the parse tree
        final ListExpression expression = makeAbstractSyntaxTree(parseTree);
        // System.out.println("AST " + expression);
        
        return expression;
    }
    
    /**
     * Convert a parse tree into an abstract syntax tree.
     * 
     * This also modifies previousDefinitions to include all new definitions in the string to parse.
     * 
     * @param parseTree constructed according to the grammar in ListExressionGrammar.g
     * @param previousDefinitions the definitions that we want to use when parsing the input string.
     * @return abstract syntax tree corresponding to parseTree
     */
    private static ListExpression makeAbstractSyntaxTree(final ParseTree<ListExpressionGrammar> parseTree) {
        switch(parseTree.name()) {
        case EXPRESSION: // expression ::= sequence;
            {
                final ParseTree<ListExpressionGrammar> child = parseTree.children().get(0);
                return makeAbstractSyntaxTree(child);
            }
        case SEQUENCE:
            {
                final List<ParseTree<ListExpressionGrammar>> children = parseTree.children();
                ListExpression expression;
                if (children.size() == 1) {
                    expression = makeAbstractSyntaxTree(children.get(0));
                } else {
                    expression = new Sequence(children.stream()
                            .map(c -> makeAbstractSyntaxTree(c))
                            .collect(Collectors.toList()));
                }
                return expression;
            }
        case SETUNION:
            {
                final List<ParseTree<ListExpressionGrammar>> children = parseTree.children();
                ListExpression expression;
                if (children.size() == 1) {
                    expression = makeAbstractSyntaxTree(children.get(0));
                } else {
                    expression = new SetUnion(children.stream()
                            .map(c -> makeAbstractSyntaxTree(c))
                            .collect(Collectors.toList()));
                }
                return expression;
            }
        case SETDIFFERENCE:
            {
                final List<ParseTree<ListExpressionGrammar>> children = parseTree.children();
                ListExpression expression = makeAbstractSyntaxTree(children.get(0));
                for (int i = 1; i < children.size(); i++) {
                    expression = new SetDifference(expression, makeAbstractSyntaxTree(children.get(i)));
                }
                return expression;
            }
        case SETINTERSECTION:
            {
                final List<ParseTree<ListExpressionGrammar>> children = parseTree.children();
                ListExpression expression = makeAbstractSyntaxTree(children.get(0));
                for (int i = 1; i < children.size(); i++) {
                    expression = new SetIntersection(expression, makeAbstractSyntaxTree(children.get(i)));
                }
                return expression;
            }
        case PRIMITIVE:
            {
                final ParseTree<ListExpressionGrammar> child = parseTree.children().get(0);
                switch(child.name()) {
                case EMAILADDRESS:
                    {
                        return new EmailAddress(child.text());
                    }
                case EMPTYEXPRESSION:
                    {
                        return new EmptyExpression();
                    }
                case EXPRESSION:
                    {
                        return makeAbstractSyntaxTree(child);
                    }
                case LISTNAME:
                    {
                        if (parseTree.children().size() == 1) {
                            return makeAbstractSyntaxTree(child);
                        } else { // case where we have listname '=' listDefinition
                            String listname = child.text().toLowerCase();
                            ListExpression value = makeAbstractSyntaxTree(parseTree.children().get(1));
                            return new ListDefinition(listname, value);
                        }
                    }
                default:
                    throw new RuntimeException("should never get here");
                }
            }
        case LISTDEFINITION:
            {
                final ParseTree<ListExpressionGrammar> child = parseTree.children().get(0);
                if (parseTree.children().size() == 1) {
                    return makeAbstractSyntaxTree(child);
                } else if (parseTree.children().size() == 2) {
                    String listname = child.text().toLowerCase();
                    ListExpression value = makeAbstractSyntaxTree(parseTree.children().get(1));
                    return new ListDefinition(listname, value);
                } else {
                    throw new RuntimeException("should never have more than one child here");
                }
            }
        case LISTNAME:
            {
                return new Listname(parseTree.text());
            }
        case PIPE:
            {
                final List<ParseTree<ListExpressionGrammar>> children = parseTree.children();
                ListExpression expression = makeAbstractSyntaxTree(children.get(0));
                for (int i = 1; i < children.size(); i++) {
                    expression = new Pipe(expression, makeAbstractSyntaxTree(children.get(i)));
                }
                return expression;
            }
        default:
            throw new RuntimeException("should never get here");
        }
    }
    
}
