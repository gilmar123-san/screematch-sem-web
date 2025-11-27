package br.com.filmes.screenmatch.principal;

import br.com.filmes.screenmatch.model.DadosSerie;
import br.com.filmes.screenmatch.model.DadosTemporadas;
import br.com.filmes.screenmatch.model.exceptions.ServiceException;
import br.com.filmes.screenmatch.service.ConsumoAPI;
import br.com.filmes.screenmatch.service.ConvertDados;

import java.util.*;

public class Principal {

    private final String ENDERECO = "https://www.omdbapi.com/?t=";

    private static final String API_KEY = String.format("&apikey=%s", System.getenv("API_KEY_FILMES"));

    private ConvertDados conversor = new ConvertDados();

    private Scanner leitura = new Scanner(System.in);

    private ConsumoAPI consumo = new ConsumoAPI();

    private List<DadosSerie> listDadosSerie = new ArrayList<>();

    public void exibeMenu() {
        int opcao = -1;
        do {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            try {
                opcao = leitura.nextInt();
                leitura.nextLine();
                switch (opcao) {
                    case 1 -> buscarSerieWeb();
                    case 2 -> buscarEpisodioPorSerie();
                    case 3 -> imprimirSeriesBuscadas();
                    case 0 -> System.out.println("Saindo...");
                    default -> System.out.println("Opção inválida");
                }
            } catch (ServiceException e) {
                System.out.printf("Error: %s %n", e.getMessage());
            } catch (InputMismatchException e) {
                System.out.println("Opção inválida! Tente uma das opções possíveis!");
                System.out.println();
                opcao = -1;
                leitura.nextLine();
            }
        } while (opcao != 0);
        leitura.close();
    }

    private void imprimirSeriesBuscadas() {
        if (this.listDadosSerie.isEmpty()) {
            System.out.println("Ainda não existe nenhuma séria nessa lista!");
        }
        this.listDadosSerie.forEach(System.out::println);
    }

    private void buscarSerieWeb() throws ServiceException {
        DadosSerie dados = getDadosSerie();
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() throws ServiceException {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        if (json.isEmpty() || json.toLowerCase().contains("error")) {
            throw new ServiceException("Nenhuma série encontrada!");
        }

        DadosSerie dados = conversor.converterDados(json, DadosSerie.class);
        listDadosSerie.add(dados);
        return dados;
    }

    private void buscarEpisodioPorSerie() throws ServiceException {
        DadosSerie dadosSerie = getDadosSerie();
        List<DadosTemporadas> temporadas = new ArrayList<>();

        for (int i = 1; i <= dadosSerie.totalTemporadas(); i++) {
            var json = consumo.obterDados(ENDERECO + dadosSerie.titulo().replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporadas dadosTemporada = conversor.converterDados(json, DadosTemporadas.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);
    }
}
