package org.abondar.experimental.gameofthree.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.abondar.experimental.gameofthree.Client;
import org.abondar.experimental.gameofthree.Move;
import org.abondar.experimental.gameofthree.ResponseUtil;
import org.abondar.experimental.gameofthree.Server;
import org.junit.Assert;
import  org.junit.Test;

import java.io.IOException;

public class TestMove {

    @Test
    public void testMakeMove(){
        Move expected = new Move(-1,6);

        Client client = new Client("addr");
        Move actual = client.enterAddNumber(19,1000);


        Assert.assertEquals(expected.getAddedNumber(),actual.getAddedNumber());
        Assert.assertEquals(expected.getResultingNumber(),actual.getResultingNumber());
    }

    @Test
    public void testGetMove() throws IOException{
        Client client = new Client("addr");
        Server server = new Server(client);

        Move expected = new Move(-1,6);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(expected);

        Move actual = server.getMove(json);

        Assert.assertEquals(expected.getAddedNumber(),actual.getAddedNumber());
        Assert.assertEquals(expected.getResultingNumber(),actual.getResultingNumber());
    }

    @Test
    public void testGetResponse() throws IOException{
        Client client = new Client("addr");
        Server server = new Server(client);

        String resp1 = server.respToMove(34);
        String resp2 = server.respToMove(1);


        Assert.assertEquals(resp1, ResponseUtil.ACCEPTED);
        Assert.assertEquals(resp2,ResponseUtil.GAME_OVER);
    }

}
