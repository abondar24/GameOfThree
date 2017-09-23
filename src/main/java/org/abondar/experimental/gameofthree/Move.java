package org.abondar.experimental.gameofthree;

public class Move {

    private Integer addedNumber;
    private Integer resultingNumber;

    public Move(){}

    public Move( Integer addedNumber, Integer resultingNumber) {
        this.addedNumber = addedNumber;
        this.resultingNumber = resultingNumber;
    }


    public Integer getAddedNumber() {
        return addedNumber;
    }

    public void setAddedNumber(Integer addedNumber) {
        this.addedNumber = addedNumber;
    }

    public Integer getResultingNumber() {
        return resultingNumber;
    }

    public void setResultingNumber(Integer resultingNumber) {
        this.resultingNumber = resultingNumber;
    }

    @Override
    public String toString() {
        return "Move{" +
                "addedNumber=" + addedNumber +
                ", resultingNumber=" + resultingNumber +
                '}';
    }
}
