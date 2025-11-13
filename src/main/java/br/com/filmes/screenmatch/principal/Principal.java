package br.com.filmes.screenmatch.principal;

import br.com.filmes.screenmatch.model.DadosEpisodios;
import br.com.filmes.screenmatch.model.DadosSerie;
import br.com.filmes.screenmatch.model.DadosTemporadas;
import br.com.filmes.screenmatch.model.Episodio;
import br.com.filmes.screenmatch.service.ConsumoAPI;
import br.com.filmes.screenmatch.service.ConvertDados;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

        var temporadas = new ArrayList<DadosTemporadas>();

        for (int i = 1; i < dados.totalTemporadas(); i++) {
            var url = String.format(URL_PADRAO_SEASON, nomeSerieParam, i, API_KEY);
            json = ConsumoAPI.obterDados(url);
            temporadas.add(conversor.converterDados(json, DadosTemporadas.class));
        }

//        List<DadosEpisodios> episodios = new ArrayList<>();
//
//        episodios = temporadas
//                .stream()
//                .flatMap(t -> t.episodios().stream())
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .sorted(Comparator.comparing(DadosEpisodios::avaliacao).reversed())
//                .limit(5)
//                .collect(Collectors.toList());

        List<Episodio> eps = temporadas
                .stream()
                .flatMap(t -> t.episodios()
                        .stream()
                        .map(d -> new Episodio(t.numeroTemporada(), d))
                )
                .sorted(Comparator.comparing(Episodio::getAvaliacao).reversed())
                .limit(5)
                .collect(Collectors.toList());
        eps.forEach(System.out::println);

        s.close();
    }


}
