package org.abondar.experimental.gameofthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client implements FaultListener, Runnable {
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    private String rivalAddr;

    private Integer intialNum = 0;

    private List<Integer> addRange;

    private Integer MOD = 3;

    public Client(String rivalAddr) {
        this.rivalAddr = rivalAddr;
        addRange = Arrays.asList(-1, 0, 1);
    }

    private String initGame() {
        WebClient client = WebClient.create("http://" + rivalAddr + "/init_game");
        WebClient.getConfig(client).getBus().setProperty("org.apache.cxf.logging.FaultListener", this);
        try {
            Response r = client.get();
            return r.readEntity(String.class);
        } catch (Exception ex){
            logger.error(ex.getMessage());
            return "Rival not connected";
        }



    }

    @Override
    public boolean faultOccurred(Exception e, String s, Message message) {
        logger.error(e.getMessage());
        if (e instanceof org.apache.cxf.interceptor.Fault) {
            Throwable cause = e.getCause();
            if (cause != null) {
                if (cause instanceof java.net.ConnectException) {
                    logger.error("Rival not available");
                    return false;
                }
            }
        }
        return true;

    }


    private String makeMove(Integer resNumber, Integer addedNumber) {
        WebClient client = WebClient.create("http://" + rivalAddr + "/make_move");
        WebClient.getConfig(client).getBus().setProperty("org.apache.cxf.logging.FaultListener", this);

        Move m = new Move(addedNumber, resNumber);
        String body = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            body = mapper.writeValueAsString(m);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }

        Response resp = client.post(body);
        return resp.readEntity(String.class);

    }

    private String initNumber(Integer initialNum) {
        WebClient client = WebClient.create("http://" + rivalAddr + "/init_num/" + initialNum);
        WebClient.getConfig(client).getBus().setProperty("org.apache.cxf.logging.FaultListener", this);

        Response resp = client.get();
        return resp.readEntity(String.class);
    }

    @Override
    public void run() {
        while (true) {
                if (initGame().equals(ResponseUtil.READY)) {
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Start game");
                    System.out.println("Enter number and -1,0,+1(If it's a first move add 0) ");
                    Integer number = Integer.valueOf(scanner.next());
                    Integer addNumber = Integer.valueOf(scanner.next());
                    if (addRange.contains(addNumber)) {
                        String res = "";
                        if (intialNum == 0) {
                            intialNum = number;
                            res = makeMove(number, addNumber);
                        } else {
                            res = makeMove(number, addNumber);
                        }
                        switch (res) {
                            case ResponseUtil.ACCEPTED:
                                System.out.println("Move accepted");
                                break;
                            case ResponseUtil.BAD_NUMBER:
                                System.out.println("Your result is not divisible by 3. Please enter again");
                                number = Integer.valueOf(scanner.next());
                                addNumber = Integer.valueOf(scanner.next());
                                makeMove(number, addNumber);
                                break;
                            case ResponseUtil.GAME_OVER:
                                System.out.println("Game over.");
                                break;
                        }
                    } else {
                        System.out.println("Wrong add number. Enter -1 0 or 1");
                    }
                } else {
                    System.out.println("Waiting for rival");
                    try {
                        Thread.sleep(15000l);
                    } catch (InterruptedException ex){
                        logger.error(ex.getMessage());
                        System.out.println("Player not connected");
                    }

                }


        }

    }
}
