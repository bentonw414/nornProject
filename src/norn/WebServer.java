/* Copyright (c) 2018 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package norn;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import norn.web.ExceptionsFilter;
import norn.web.LogFilter;

/**
 * Class connecting web requests to our provided ListExpression machinery 
 */
public class WebServer {
    // AF(server) = the Web server with HttpServer server
    // RI:
    //     true
    // SRE:
    //     all instance variables private and final and not given by any instance method
    // Thread safety:
    //      only handling done is for evaluating an expression. 
    //      Multiple threads using the web server can parse independently because their requests are independent.
    //      When the result is added to NornSystem via parseAndStore, it is safe because NornSystem.java is threadsafe. 
    
    private static final int SUCCESS_CODE = 200;
    
    private final HttpServer server;
    
    /**
     * Make a new web server that listens for connections on port.
     * @param port server port number
     * @throws IOException if an error occurs starting the server
     */
    public WebServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // handle concurrent requests with multiple threads
        server.setExecutor(Executors.newCachedThreadPool());
        
        List<Filter> logging = List.of(new ExceptionsFilter(), new LogFilter());
        
        HttpContext eval = server.createContext("/eval/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                handleEval(exchange);
            }
        });
        eval.getFilters().addAll(logging);
    }
    
    /**
     * @return the port on which this server is listening for connections
     */
    public int port() {
        return server.getAddress().getPort();
    }
    
    /**
     * Start this server in a new background thread.
     */
    public void start() {
        System.err.println("Server will listen on " + server.getAddress());
        server.start();
    }
    
    /**
     * Stop this server. Once stopped, this server cannot be restarted.
     */
    public void stop() {
        System.err.println("Server will stop");
        server.stop(0);
    }
    
    private void handleEval(HttpExchange exchange) throws IOException {
        // page response is HTML text in UTF-8
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        
        final String path = exchange.getRequestURI().getPath();
        
        exchange.sendResponseHeaders(SUCCESS_CODE, 0);
        
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        
        
        String input = path.substring(base.length());
        try {
            ListEval eval = NornSystem.parseEvalAndStore(input);
            String emailString = "";
            Set<EmailAddress> emails = eval.getEmailAddresses();
            for (EmailAddress email: emails) {
                if (emailString.length() != 0) {
                    emailString += ", ";
                }
                emailString = emailString + email.getAddress(); 
            }
            
            String visualization = eval.getVisualization();
            
            OutputStream body = exchange.getResponseBody();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
            out.println("<p><strong>" + "Addresses in List: " + " </strong></p>");
            out.println("<p> {" + emailString + " } </p> <hr>");
            out.println("<p><strong>" + "Visualization: " + "</strong></p>");
            out.println("<p>" + visualization + "</p>");
        } catch (InvalidExpressionException e) {
            OutputStream body = exchange.getResponseBody();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
            out.println("Error: Invalid expression: " + e.getMessage());
        }
        
        exchange.close();
    }
    
}
