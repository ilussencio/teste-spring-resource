package com.iftm.client.resources;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.services.ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientResourcesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService servico;

    /**
     * Caso de testes : Verificar se o endpoint get/clients/ retorna todos os clientes existentes
     * Arrange:
     * - camada service simulada com mockito
     * - base de dado : 3 clientes
     * new Client(7l, "Jose Saramago", "10239254871", 5000.0, Instant.parse("1996-12-23T07:00:00Z"), 0);
     * new Client(4l, "Carolina Maria de Jesus", "10419244771", 7500.0, Instant.parse("1996-12-23T07:00:00Z"), 0);
     * new Client(8l, "Toni Morrison", "10219344681", 10000.0, Instant.parse("1940-02-23T07:00:00Z"), 0);
     * - Uma PageRequest default
     */

    @DisplayName("Verificar se o endpoint get/clients/ retorna todos os clientes existentes")
    @Test
    public void testarEndPointRetornaTodosClientesExistentes() throws Exception {
        //necessário para o teste de unidade
        //confiurar mockBean Servico
        List<ClientDTO> listaClientesExistentes = new ArrayList<>();
        listaClientesExistentes.add(new ClientDTO(new Client(7l, "Jose Saramago", "10239254871", 5000.0, Instant.parse("1996-12-23T07:00:00Z"), 0)));
        listaClientesExistentes.add(new ClientDTO(new Client(4l, "Carolina Maria de Jesus", "10419244771", 7500.0, Instant.parse("1996-12-23T07:00:00Z"), 0)));
        listaClientesExistentes.add(new ClientDTO(new Client(8l, "Toni Morrison", "10219344681", 10000.0, Instant.parse("1940-02-23T07:00:00Z"), 0)));

        Page<ClientDTO> pagina = new PageImpl<>(listaClientesExistentes);
        Mockito.when(servico.findAllPaged(Mockito.any())).thenReturn(pagina);

        int qtdClientes = 3;

        ResultActions resultado = mockMvc.perform(get("/clients/").accept(APPLICATION_JSON));
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements").exists())
                .andExpect(jsonPath("$.numberOfElements").value(qtdClientes));
    }

}
