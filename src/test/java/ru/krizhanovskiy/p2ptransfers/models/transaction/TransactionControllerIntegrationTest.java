/*
package ru.krizhanovskiy.p2ptransfers.models.transaction;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void createTransaction_shouldReturnCreated() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                "12345678901234567890", "09876543210987654321", 5000L
        );

        Transaction tx = Transaction.builder()
                .id(1L)
                .sourceAccountId(1L)
                .recipientAccountId(2L)
                .amount(5000L)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .build();

        when(transactionService.createTransaction(any())).thenReturn(tx);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "sourceAccountNumber": "12345678901234567890",
                              "recipientAccountNumber": "09876543210987654321",
                              "amount": 5000
                            }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(5000));
    }
}
*/
