package norn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
/**
 * Class of static methods combining the console and Webserver output for the norn system.
 */
public class NornSystem {
    // Note: AF, RI, SRE, Thread safety all for the class and its methods. 
    // Since we only every make one instance, we consider the class to be "one instance" for the purposes of AF, RI, etc. 
    // AF(NornSystem) = the norn system with WebServer WEB
    // RI:
    //     true
    // SRE:
    //     all instance variables private and final and not given by any instance method
    // Thread safety:
    //      run() will only be called once, by Main.java. So there's only one chain of sequential console input to consider.
    //      However, the WebServer may give concurrent parseEvalAndStore requests.
    //      parseEvalAndStore is synchronized, so parsing, evaluating, and storing a list expression is an atomic action.
    
    private static final String SAVE_COMMAND = "/save";
    private static final String LOAD_COMMAND = "/load";
    
    private static final WebServer WEB;
    
    static {
        try {
            final int port = 8080;
            WEB = new WebServer(port);
        } catch (IOException e) {
            System.out.println("Exception while making server" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    private static Map<String, ListExpression> definitions = new HashMap<>();
    
    /**
     * Read expression and command inputs from the console and output results,
     * and start a web server to handle requests from remote clients.
     * An empty console input terminates the program.
     * @throws IOException if there is an error reading the input
     */
    public static void run() throws IOException {
        WEB.start();
        
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        
        while (true) {
            System.out.print("> ");
            final String input = in.readLine();
            
            if (input.isEmpty()) {
                WEB.stop();
                return; // exits the program
            }
            
            try {
                if (input.split(" ")[0].equals(SAVE_COMMAND)) {
                    String filepath = input.split(" ")[1];
                    try {
                        save(filepath);
                    } catch (IOException e) {
                        System.out.println("Problem saving file: " + e.getMessage());
                    }
                } else if (input.split(" ")[0].equals(LOAD_COMMAND)){
                    String filepath = input.split(" ")[1];
                    try {
                        load(filepath);
                    } catch (IOException e) {
                        System.out.println("Problem loading file: " + e.getMessage());
                    }
                } else {
                    String output;
                    try {
                        ListEval eval = parseEvalAndStore(input);
                        output = "";
                        Set<EmailAddress> emails = eval.getEmailAddresses();
                        
                        for (EmailAddress email: emails) {
                            if (output.length() != 0) {
                                output += ", ";
                            }
                            output = output + email.getAddress(); 
                        }
                    } catch (InvalidExpressionException e) {
                        output = "Error: Invalid expression: " + e.getMessage();
                    }
                    System.out.println(output);
                }
            } catch (NoSuchElementException nse) {
                // currentExpression was empty
                System.out.println("must enter an expression before using this command");
            } catch (RuntimeException re) {
                System.out.println(re.getClass().getName() + ": " + re.getMessage());
            }
        }
    }
    
    private static void load(String filename) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
        String line = br.readLine();
        String savedString = "";
        
        while (line != null) { // must use null since the reading library does this
            savedString += line;
            line = br.readLine();
        }
        
        try {
            parseEvalAndStore(savedString); // we can throw away the output since we are just loading
            br.close();
            System.out.println("Successfully loaded the ListExpression from the supplied file.");
        } catch (InvalidExpressionException e) {
            br.close();
            throw new IOException("file was not a valid expression");
        }
        br.close();
    }
    
    private static void save(String filename) throws IOException{
        String outputString = "";
        for (String listname: definitions.keySet()) {
            if (outputString.length() != 0) {
                outputString += ";";
            }
            outputString += "(" + listname + "=" + definitions.get(listname) + ")";
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)));
        bw.write(outputString);
        bw.close();
        System.out.println("Successfully saved all ListExpression definitions at the supplied location.");
    }
    
    /**
     * Parses the string input into a ListExpression, evaluates into a set of email addresses and a visualization string, 
     * and stores it into the norn system's current expression if it was a valid expression.
     * 
     * This method is synchronized, so it is safe to be called by multiple threads. 
     * Parsing, evaluating, and storing is thus an atomic operation,
     * preventing concurrency issues with adding list expressions.
     * 
     * @param input the string to parse
     * @return a ListEval object containing the set of email addresses and visualization string
     *  of the parsed string combined sequentially with the norn system's previous input.
     * @throws InvalidExpressionException if the expression from the parsed input is not valid.
     */
    public static synchronized ListEval parseEvalAndStore(String input) throws InvalidExpressionException {
        final ListExpression newExpression = ListExpression.parse(input);
        ListEval eval = ListExpression.evalAndVisualize(newExpression, definitions);
        // if above line gives InvalidExpressionException, rest won't happen
        definitions = eval.getDefinitions();
        return eval;
    }
}
