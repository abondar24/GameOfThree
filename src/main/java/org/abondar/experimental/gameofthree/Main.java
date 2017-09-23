package org.abondar.experimental.gameofthree;

import org.apache.log4j.BasicConfigurator;

import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Scanner in = new Scanner(System.in);
        System.out.println("Enter port number");
        Integer port = Integer.valueOf(in.next());
        Server.startServer(port);
    }
}
