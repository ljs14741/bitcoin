package com.example.bitcoin.common;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RequestUpbitURL {

    public String request(String url){

        String data = "";
        try{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("accept", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            data = response.body().string();

        }catch(Exception e){
//            log.info(e.getMessage());
        }

        return data;

    }
}
