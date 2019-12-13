/* Copyright (c) 2018 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package norn;

import java.io.IOException;

/**
 * Start the Norn mailing list system console interface and web server.
 * <p>You are free to change this class.
 */
public class Main {
    
    /**
     * Start the norn system.
     * @param args unused
     * @throws IOException if the NornSystem has trouble reading input
     */
    public static void main(String[] args) throws IOException {
        NornSystem.run();
    }
}

