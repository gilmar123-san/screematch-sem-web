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

        List<DadosEpisodios> episodios = new ArrayList<>();

        episodios = temporadas
                .stream()
                .flatMap(t -> t.episodios().stream())
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodios::avaliacao).reversed())
                .collect(Collectors.toList());

//        episodios.stream()
//                .filter(e -> !e.avaliacao().equals("N/A"))
//                .peek(e -> System.out.printf("Primeiro filtro(N/A) %s%n", e))
//                .sorted(Comparator.comparing(DadosEpisodios::avaliacao).reversed())
//                .peek(e -> System.out.printf("Ordenação %s%n", e))
//                .limit(10)
//                .peek(e -> System.out.printf("Limit %s%n", e))
//                .map(e -> e.titulo().toUpperCase())
//                .peek(e -> System.out.printf("Map %s%n", e))
//                        .forEach(System.out::println);

        List<Episodio> eps = temporadas.stream()
                .flatMap(t -> t.episodios()
                        .stream()
                        .map(d -> new Episodio(t.numeroTemporada(), d)))
                .collect(Collectors.toList());

        eps.forEach(System.out::println);

        System.out.print("Informe o nome do episódio: ");
        var nomeTitulo = s.nextLine();

        var epFilter = eps.stream()
                .filter(e -> e.getTitulo().toLowerCase().contains(nomeTitulo.toLowerCase()))
                .findFirst();

        if(epFilter.isPresent()) {
            System.out.println("Ep encontrado!");
            System.out.println(epFilter.get());
        } else {
            System.out.println("Ep não encontrado!");
        }

        System.out.println("A partir de que ano você quer ver os episódios? ");
        var ano = s.nextInt();
        s.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);

        eps.stream()
                .filter(e -> Objects.nonNull(e) && e.getDataLancamento().isAfter(dataBusca))
                .forEach(System.out::println);

        Map<Integer, Double> avalicoesTemporada = eps.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println(avalicoesTemporada);

        DoubleSummaryStatistics est = eps.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                        .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.printf("Média: %.2f %n", est.getAverage());
        System.out.printf("Melhor Ep: %.2f %n", est.getMax());
        System.out.printf("Pior Ep: %.2f %n", est.getMin());

        s.close();
    }


}
