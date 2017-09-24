package org.abondar.experimental.gameofthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

import java.io.IOException;


public class Server {

    private static Logger logger = LoggerFactory.getLogger(Server.class);
    private static Integer MOD = 3;

    private static Integer resNum;
    public static void startServer(Integer port) {
        port(port);
        get("/init_game", (req, res) -> ResponseUtil.READY);
        post("/make_move", (req, res) -> {

            Move m = getMove(req.body());
            resNum = m.getResultingNumber();
            System.out.printf("User has made a move with: %d \n" ,resNum);
            return respToMove(resNum);
        });


    }


    private static Move getMove(String moveData) {
        ObjectMapper mapper = new ObjectMapper();
        Move m = new Move();
        try {
            m = mapper.readValue(moveData, Move.class);
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage());
        }

        return m;
    }


    private static String respToMove(Integer resNum) {
        if (resNum == 1) {
            return ResponseUtil.GAME_OVER;
        } else if (resNum % MOD != 0 ) {

            return ResponseUtil.BAD_NUMBER;
        } else {
            return ResponseUtil.ACCEPTED;
        }

    }

}
