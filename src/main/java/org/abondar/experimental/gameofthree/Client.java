package org.abondar.experimental.gameofthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client implements FaultListener, Runnable {
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    private String rivalAddr;

    private List<Integer> addRange;

    private Integer MOD = 3;

    private Integer WAIT_INPUT = 15000;

    private Boolean gameOver = false;

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
        } catch (Exception ex) {
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


    private String makeMove(Move m) {
        WebClient client = WebClient.create("http://" + rivalAddr + "/make_move");
        WebClient.getConfig(client).getBus().setProperty("org.apache.cxf.logging.FaultListener", this);

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

    private Move enterAddNumber(Integer number, long waitInput) {

        Scanner scanner = new Scanner(System.in);
        Integer addNumber = 0;
        System.out.println("Enter number and -1,0,+1");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {

            while (true) {

                long startTime = System.currentTimeMillis();
                while ((System.currentTimeMillis() - startTime) < waitInput && !br.ready()) {
                }

                if (br.ready()) {
                    addNumber = Integer.valueOf(scanner.next());
                    if (!addRange.contains(addNumber)) {
                        System.out.println("Wrong add number. Enter -1 0 or 1");
                        addNumber = Integer.valueOf(scanner.next());
                    }

                    if ((number + addNumber) % MOD != 0) {
                        System.out.println(ResponseUtil.BAD_NUMBER);
                        addNumber = Integer.valueOf(scanner.next());
                    }

                } else {
                    for (Integer ar : addRange) {
                        if ((number + ar) % MOD == 0) {
                            addNumber = ar;
                        }
                    }
                    System.out.println("No input.Automatically added " + addNumber);
                }
                number = (number + addNumber) / 3;

                return new Move(addNumber,number);
            }

        } catch (IOException ex) {
            logger.error(ex.getMessage());
            System.out.println("Input error");
        }

        return new Move(0,0);
    }


    public void makeClientMove(Integer number){
        while (true) {

                Move m = enterAddNumber(number,WAIT_INPUT);
                String res = makeMove(m);
                System.out.println(res);
                if (res.equals(ResponseUtil.GAME_OVER)){
                    break;
                }
            }
    }


    @Override
    public void run() {

        Integer number;
            if (initGame().equals(ResponseUtil.READY)) {

                Scanner scanner = new Scanner(System.in);
                System.out.println("Start game.Enter initial number");
                number = Integer.valueOf(scanner.next());
                String resp = makeMove(new Move(0,number));
                System.out.println(resp);

            } else {
                System.out.println("Waiting for rival");
                while (!gameOver){
                    try {
                        Thread.sleep(15000l);
                    } catch (InterruptedException ex) {
                        logger.error(ex.getMessage());
                        System.out.println("Player not connected");
                    }

                }

            }


    }

    public void setGameOver(Boolean gameOver) {
        this.gameOver = gameOver;
    }
}
