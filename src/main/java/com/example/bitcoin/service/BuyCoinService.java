package com.example.bitcoin.service;

import com.example.bitcoin.common.RequestUpbitURL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
public class BuyCoinService {

    @Autowired
    RequestUpbitURL requestUpbitURL;

    public void buyCoin() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        requestUpbitURL.executeTrade("KRW-XRP", "bid","1", "5000", "price");
    }

    public void sellCoin() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        requestUpbitURL.executeTrade("KRW-XRP", "ask","1", "5000", "price");
    }
}
