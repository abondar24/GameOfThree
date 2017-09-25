package org.abondar.experimental.gameofthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

import java.io.IOException;
import java.util.concurrent.*;


public class Server {

    private Logger logger = LoggerFactory.getLogger(Server.class);


    private ExecutorService executorService = Executors.newCachedThreadPool();

    private Integer resNum;

    private Client client;

    public Server(Client client) {
        ThreadLocal tl = new ThreadLocal();
        this.client = client;

        tl.set(client);
    }

    public void startServer(Integer port) {
        port(port);
        get("/init_game", (req, res) -> ResponseUtil.READY);
        post("/make_move", (req, res) -> {

            Move m = getMove(req.body());
            resNum = m.getResultingNumber();


            System.out.printf("(Server) User has made a move with %d and got %d\n", m.getAddedNumber(), resNum);


            Future<Void> future = executorService.submit(() -> {
                client.makeClientMove(resNum);
                return null;

            });

            return respToMove(resNum);

        });

        get("/shutdown", (req, res) -> {
            System.out.println("Shutting");
            stop();
            executorService.shutdownNow();
            // System.exit(0);
            return "";
        });


    }


    public Move getMove(String moveData) {
        ObjectMapper mapper = new ObjectMapper();
        Move m = new Move();
        try {
            m = mapper.readValue(moveData, Move.class);
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage());
        }

        return m;
    }


    public String respToMove(Integer resNum) {
        if (resNum == 1) {
            client.setGameOver(true);
            return ResponseUtil.GAME_OVER;
        } else {
            return ResponseUtil.ACCEPTED;
        }

    }

}
