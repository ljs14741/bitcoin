package com.example.bitcoin.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonTransfer {

    public static Map<String, Object> getMapFromJSONObject(JSONObject obj) {
        if (ObjectUtils.isEmpty(obj)) {
            log.error("BAD REQUEST obj : {}", obj);
            throw new IllegalArgumentException(String.format("BAD REQUEST obj %s", obj));
        }

        try {
            /*
            unchecked or unsafe 에러로 인해 수정
            참고자료 : https://bjp5319.tistory.com/50
            */
            return new ObjectMapper().readValue(obj.toString(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> getListMapFromJsonArray(JSONArray jsonArray) throws JSONException {
        if (ObjectUtils.isEmpty(jsonArray)) {
            log.error("jsonArray is null.");
            throw new IllegalArgumentException("jsonArray is null");
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i=0; i<jsonArray.length(); i++) {
            Map<String, Object> map = getMapFromJSONObject((JSONObject)jsonArray.get(i));
            list.add(map);
        }
        return list;
    }

    public static <T> T getObjectFromJSONObject(JSONObject obj,TypeReference<T> type){
        if (ObjectUtils.isEmpty(obj)) {
            log.error("BAD REQUEST obj : {}", obj);
            throw new IllegalArgumentException(String.format("BAD REQUEST obj %s", obj));
        }

        try {
            // json -> object 시 없는 필드 무시
            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(obj.toString(), type);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> getListObjectFromJSONObject(JSONArray jsonArray, TypeReference<T> type) throws JSONException{
        if (ObjectUtils.isEmpty(jsonArray)) {
            log.error("jsonArray is null.");
            throw new IllegalArgumentException("jsonArray is null");
        }
        List<T> list = new ArrayList<>();
        log.info(list.getClass().getName());
        for (int i=0; i<jsonArray.length(); i++) {
            list.add(getObjectFromJSONObject((JSONObject)jsonArray.get(i),type));
        }
        return list;
    }
}
