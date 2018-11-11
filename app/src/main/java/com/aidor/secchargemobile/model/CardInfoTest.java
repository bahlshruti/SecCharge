package com.aidor.secchargemobile.model;

import java.util.List;

/**
 * Created by Nikhil on 02/03/2017.
 * Model for mapping response JSON data from web service call to this class.
 */

public class CardInfoTest {

    List<CardInfo> cardInfos;

    public List<CardInfo> getCardInfos() {
        return cardInfos;
    }

    public void setCardInfos(List<CardInfo> cardInfos) {
        this.cardInfos = cardInfos;
    }
}
