/* Copyright (c) 2017-2018 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package norn.web;

import java.io.IOException;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * Logging filter that reports request URLs and response codes to the console.
 */
public class LogFilter extends Filter {
    
    @Override public String description() { return "Log requests"; }
    
    @Override public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        System.err.println(" -> " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
        chain.doFilter(exchange);
        System.err.println(" <- " + exchange.getResponseCode());
    }
}
