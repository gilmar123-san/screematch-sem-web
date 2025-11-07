package br.com.filmes.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DadosTemporadas(@JsonAlias("Season") String numeroTemporada,
                              @JsonAlias("Episodes") List<DadosEpisodios> episodios) {
}
