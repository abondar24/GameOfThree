package org.abondar.experimental.gameofthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

import java.io.IOException;


public class Server {

    private static Logger logger = LoggerFactory.getLogger(Server.class);
    private static Integer MOD = 3;

    public static void startServer(Integer port) {
        port(port);
        get("/init_game", (req, res) -> "Ready");
        post("/make_move", (req, res) -> {

            Integer resNum = makeMove(req.body());

            return respToMove(resNum);
        });

    }


    public static Integer makeMove(String moveData) {
        ObjectMapper mapper = new ObjectMapper();
        Move m = new Move();
        try {
            m = mapper.readValue(moveData, Move.class);
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage());
        }

        return m.getResultingNumber() + m.getAddedNumber();
    }


    private static String respToMove(Integer resNum) {
        if (resNum == 1) {
            return "Game Over";
        } else if (resNum % MOD != 0) {
            return "Bad Number";
        } else {
            return "Accepted";
        }

    }

}
