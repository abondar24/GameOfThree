package org.abondar.experimental.gameofthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client implements FaultListener {
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    private String rivalAddr;

    private List<Integer> addRange;

    private Integer MOD = 3;

    private Integer WAIT_INPUT = 15000;

    private Boolean gameOver = false;

    private WebClient client;

    public Client(String rivalAddr) {

        this.rivalAddr = rivalAddr;
        addRange = Arrays.asList(-1, 0, 1);
         client = WebClient.create("http://" + rivalAddr);
        WebClient.getConfig(client).getBus().setProperty("org.apache.cxf.logging.FaultListener", this);
    }

    private String initGame() {

        try {

            Response r = client.reset().path("/init_game").get();
            return r.readEntity(String.class);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return ResponseUtil.NOT_CONNECTED;
        }


    }

    @Override
    public boolean faultOccurred(Exception e, String s, Message message) {
        logger.error(e.getMessage());
        if (e instanceof org.apache.cxf.interceptor.Fault) {
            Throwable cause = e.getCause();
            if (cause != null) {
                if (cause instanceof java.net.ConnectException) {
                    logger.error(ResponseUtil.NOT_CONNECTED);
                    return false;
                }
            }
        }
        return true;

    }


    private String makeMove(Move m) {

        String body = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            body = mapper.writeValueAsString(m);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }

        Response resp = client.reset().path("/make_move").post(body);
        return resp.readEntity(String.class);

    }



    private void shutdown() {
       client.reset().path("/shutdown").get();
       client.close();
    }

    private Move enterAddNumber(Integer number, long waitInput) {

        Scanner scanner = new Scanner(System.in);
        Integer addNumber = 0;
        System.out.println(ResponseUtil.ENTER_FROM_RANGE);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {

            while (true) {

                long startTime = System.currentTimeMillis();
                while ((System.currentTimeMillis() - startTime) < waitInput && !br.ready()) {
                }

                if (br.ready()) {
                    addNumber = Integer.valueOf(scanner.next());
                    if (!addRange.contains(addNumber)) {
                        System.out.println("Wrong add number. " + ResponseUtil.ENTER_FROM_RANGE);
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

                return new Move(addNumber, number);
            }

        } catch (IOException ex) {
            logger.error(ex.getMessage());
            System.out.println("Input error");
        }

        return new Move(0, 0);
    }


    public synchronized void makeClientMove(Integer number) {

        if (number == 1) {
            System.out.println(ResponseUtil.GAME_OVER);
            shutdown();
            System.exit(0);
        }
        Move m = enterAddNumber(number, WAIT_INPUT);
        String res = makeMove(m);
        System.out.println(res);


    }


    public void run() {

        Integer number;
        if (initGame().equals(ResponseUtil.READY)) {

            Scanner scanner = new Scanner(System.in);
            System.out.println("Start game.Enter initial number");
            number = Integer.valueOf(scanner.next());
            String resp = makeMove(new Move(0, number));
            System.out.println(resp);

        } else {
            System.out.println("Waiting for rival");
            while (!gameOver) {
                try {
                    Thread.sleep(15000l);
                } catch (InterruptedException ex) {
                    logger.error(ex.getMessage());
                    System.out.println(ResponseUtil.NOT_CONNECTED);
                }

            }


        }


    }

    public void setGameOver(Boolean gameOver) {
        this.gameOver = gameOver;
    }
}
