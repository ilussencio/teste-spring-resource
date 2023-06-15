package com.iftm.client.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iftm.client.dto.ClientDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientResourcesTestIntegracao {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * Caso de testes : Verificar se o endpoint get/clients/ retorna todos os clientes existentes
     * - Uma PageRequest default
     */

    @DisplayName("Verificar se o endpoint get/clients/ retorna todos os clientes existentes")
    @Test
    public void testarEndPointRetornaTodosClientesExistentes() throws Exception {

        int qtdClientes = 12;

        ResultActions resultado = mockMvc.perform(get("/clients/").accept(APPLICATION_JSON));
        resultado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.*", isA(ArrayList.class)))
                .andExpect(jsonPath("$.content.*", hasSize(qtdClientes)))
                .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].id").value(4L))
                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 7L).exists())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 3L).exists())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 5L).exists())
                .andExpect(jsonPath("$.pageable.pageSize").exists())
                .andExpect(jsonPath("$.pageable.pageSize").value(12))
                .andExpect(jsonPath("$.numberOfElements").exists())
                .andExpect(jsonPath("$.numberOfElements").value(qtdClientes))
                .andExpect(jsonPath("$.content[0].id", is(4)))
        ;
    }

    /**
     * Caso de teste: verificar se o endPoint clients/id retorna o cliente correto quando o id existe
     * Arrange:     *
     * - Uma PageRequest default
     * - idExistente : 4L
     * {
     * "id": 4,
     * "name": "Carolina Maria de Jesus",
     * "cpf": "10419244771",
     * "income": 7500.0,
     * "birthDate": "1996-12-23T07:00:00Z",
     * "children": 0
     * }
     */
    @Test
    @DisplayName("verificar se o endPoint clients/id retorna o cliente correto quando o id existe")
    public void testarEndPointBuscaPorIDRetornaClienteIdExistente() throws Exception {
        long idExistente = 4L;
        mockMvc.perform(get("/clients/{id}", idExistente).accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").value(idExistente))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.name").value("Carolina Maria de Jesus"))
                .andExpect(jsonPath("$.cpf").exists())
                .andExpect(jsonPath("$.cpf").value("10419244771"))
        ;
    }

    /**
     * Caso de teste: verificar se o endPoint clients/id retorna
     * o erro Resource not found quando o id não existe
     * Arrange:
     * - idExistente : 40L
     */
    public void testarBuscaPorIdNaoExistenteRetornaErro() throws Exception {
        long idNaoExistente = 40L;
        ResultActions resultado = mockMvc.perform(get("/clients/id/{id}", idNaoExistente)
                .accept(MediaType.APPLICATION_JSON));
        resultado.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource not found"));
    }

    /**
     * Caso de teste: insert deveria retornar “created” (código 201), bem como o produto criado, verifique no
     * mínimo dois atributos. Lembrando que esse método retorna um Json contendo o registro
     * criado. Para implementar esse teste é necessário definir um objeto a ser inserido, esse objeto
     * é um clientDTO que você irá criar no teste.
     */
    @Test
    public void testInsertShouldReturnCreatedStatusAndClientDTO() throws Exception {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("Lucas Ilussencio");
        clientDTO.setChildren(0);
        clientDTO.setCpf("10419244771");
        clientDTO.setIncome(7500.0);
        String json = objectMapper.writeValueAsString(clientDTO);
        ResultActions result = mockMvc.perform(post("/clients")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.name").value(clientDTO.getName()))
                .andExpect(jsonPath("$.cpf").exists())
                .andExpect(jsonPath("$.cpf").value(clientDTO.getCpf()));
    }

    /**
     * Caso de teste: delete deveria
     * ◦ retornar “no content” (código 204) quando o id existir
     * ◦ retornar “not found” (código 404) quando o id não existir
     */
    @Test
    public void testDeleteShouldReturnNoContentWhenIdExists() throws Exception {
        long existingId = 1L;

        ResultActions result = mockMvc.perform(delete("/clients/{id}", existingId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        long nonExistingId = 100L;

        ResultActions result = mockMvc.perform(delete("/clients/{id}", nonExistingId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    /**
     * Caso de teste: retornar OK (código 200), bem como os clientes que tenham o Income informado.
     * Verificar se o Json Paginado tem a quantidade de clientes correta e se os clientes
     * retornados são aqueles esperados. (similar ao exemplo feito em sala de aula).
     * ◦ Cuidado com os valores para teste, pois o delete apagou algum registro:
     */
    @Test
    public void testFindByIncomeShouldReturnClientsWithIncome() throws Exception {
        double income = 7000;
        ResultActions result = mockMvc.perform(get("/clients/incomeGreaterThan/?income={income}", income)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.numberOfElements").value(2))
                .andExpect(jsonPath("$.content[*].name").value(containsInAnyOrder("Carolina Maria de Jesus", "Toni Morrison")));
    }

    /**
     * Caso de teste: update deveria
     * ◦ retornar “ok” (código 200), bem como o json do produto atualizado para um id existente,
     * verifique no mínimo dois atributos. (similar ao insert, precisa passar o json modificado).
     * ◦ retornar “not found” (código 204) quando o id não existir. Fazer uma assertion para
     * verificar no json de retorno se o campo “error” contém a string “Resource not found”.
     */
    @Test
    public void testUpdateShouldReturnOkAndUpdatedProductForExistingId() throws Exception {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(3L);
        clientDTO.setName("Lucas Ilussencio");
        clientDTO.setChildren(0);
        clientDTO.setCpf("10419244771");
        clientDTO.setIncome(7500.0);
        String json = objectMapper.writeValueAsString(clientDTO);

        ResultActions result = mockMvc.perform(put("/clients/{id}", clientDTO.getId())
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").value(clientDTO.getId()))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.name").value(clientDTO.getName()))
                .andExpect(jsonPath("$.cpf").exists())
                .andExpect(jsonPath("$.cpf").value(clientDTO.getCpf()));
    }

    @Test
    public void testUpdateShouldReturnNotFoundAndErrorMessageWhenIdDoesNotExist() throws Exception {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(100L);
        clientDTO.setName("Lucas Ilussencio");
        clientDTO.setChildren(0);
        clientDTO.setCpf("10419244771");
        clientDTO.setIncome(7500.0);
        String json = objectMapper.writeValueAsString(clientDTO);

        ResultActions result = mockMvc.perform(put("/clients/{id}", clientDTO.getId())
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource not found"));
    }
}