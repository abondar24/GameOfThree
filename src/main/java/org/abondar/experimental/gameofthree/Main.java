package org.abondar.experimental.gameofthree;

import org.apache.log4j.BasicConfigurator;

import java.util.Scanner;


public class Main {


    public static void main(String[] args) {
        //  BasicConfigurator.configure();
        Scanner in = new Scanner(System.in);
        System.out.println("Enter port number");
        Integer port = Integer.valueOf(in.next());

        System.out.println("Enter rival's address");
        String client2Addr = in.next();

        Client client = new Client(client2Addr);
        Server server = new Server(client);

        (new Thread(client)).start();
        server.startServer(port);

    }
}
