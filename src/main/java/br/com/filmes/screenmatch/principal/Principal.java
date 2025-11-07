package br.com.filmes.screenmatch.principal;

import br.com.filmes.screenmatch.model.DadosSerie;
import br.com.filmes.screenmatch.model.DadosTemporadas;
import br.com.filmes.screenmatch.service.ConsumoAPI;
import br.com.filmes.screenmatch.service.ConvertDados;

import java.util.ArrayList;
import java.util.Scanner;

public class Principal {

    private static final String URL_PADRAO = "https://www.omdbapi.com/?t=%s&apikey=%s";

    private static final String URL_PADRAO_SEASON = "https://www.omdbapi.com/?t=%s&season=%d&apikey=%s";

    private static final String API_KEY = System.getenv("API_KEY_FILMES");

    private ConvertDados conversor = new ConvertDados();

    private Scanner s = new Scanner(System.in);

    public void exibeMenu() {

        System.out.println("Digite o nome da série para consulta:");
        var nomeSerieParam = s.nextLine().replace(" ", "+");

        var json = ConsumoAPI.obterDados(String.format(URL_PADRAO, nomeSerieParam, API_KEY));
        var dados = conversor.converterDados(json, DadosSerie.class);
        var episodios = new ArrayList<DadosTemporadas>();
        for (int i = 1; i < dados.totalTemporadas(); i++) {
            var url = String.format(URL_PADRAO_SEASON, nomeSerieParam, i, API_KEY);
            json = ConsumoAPI.obterDados(url);
            episodios.add(conversor.converterDados(json, DadosTemporadas.class));
        }

        episodios.forEach(System.out::println);

        s.close();
    }


}
