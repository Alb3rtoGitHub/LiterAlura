package com.aluracursos.literalura.service;

public interface IConvertData {
    <T> T getData(String json, Class<T> classInfo);
}
