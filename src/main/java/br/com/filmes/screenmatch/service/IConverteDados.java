package br.com.filmes.screenmatch.service;

public interface IConverteDados {

    <T> T converterDados(String json, Class<T> classe);

}
