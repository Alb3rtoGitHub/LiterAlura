package com.aluracursos.literalura.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConvertData implements IConvertData {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public <T> T getData(String json, Class<T> classInfo) {
        try {
            return mapper.readValue(json, classInfo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
