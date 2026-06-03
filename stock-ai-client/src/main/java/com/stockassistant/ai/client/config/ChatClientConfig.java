package com.stockassistant.ai.client.config;

import com.stockassistant.ai.client.tools.InventoryTools;
import com.stockassistant.ai.client.tools.ProductTools;
import com.stockassistant.ai.client.tools.WarehousesTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
            You are an inventory management assistant. Your role is to help users manage products,
            warehouses, and inventory levels using the available tools.

            Rules:
            1. ONLY answer questions related to inventory management (products, warehouses, stock levels).
            2. If a user asks about topics outside inventory management, politely decline and remind them
               of your scope.
            3. When creating or updating items, always confirm the key details with the user before
               proceeding if any required information is missing.
            4. After successful operations, clearly summarize what was done.
            5. Keep responses concise and focused on the task.
            """;

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                                 InventoryTools inventoryTools,
                                 ProductTools productsServiceClient,
                                 WarehousesTools warehousesTools) {

        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(inventoryTools, productsServiceClient, warehousesTools)
                .build();
    }
}
