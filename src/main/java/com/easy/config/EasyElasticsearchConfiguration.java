package com.easy.config;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@ConditionalOnProperty(
        name = {"easy.elasticsearch.cluster.enabled"},
        havingValue = "true"
)
public class EasyElasticsearchConfiguration {

    private Client client;

    public Client transportClient(ConfigProperties configProperties){
        TransportClient transportClient = null;
        try {
            if (!StringUtils.isEmpty(configProperties.getUsername()) && !StringUtils.isEmpty(configProperties.getPassword())){
                StringBuffer xpack = new StringBuffer(configProperties.getUsername());
                xpack.append(":").append(configProperties.getPassword());
                transportClient = new PreBuiltTransportClient(Settings.builder()
                        .put("cluster.name",configProperties.getName())
                        .put("xpack.security,user",xpack.toString())
                        .build());
            }else{
                transportClient = new PreBuiltTransportClient(Settings.builder()
                        .put("cluster.name",configProperties.getName())
                        .build());
            }
            String[] nodes = configProperties.getNodes().split(",");
            for (String node : nodes){
                TransportAddress transportAddress = new TransportAddress(InetAddress.getByName(node),configProperties.getPort());
                transportClient.addTransportAddress(transportAddress);
            }
        }catch (UnknownHostException e){
            log.error("elasticsearch client error : ",e.getMessage());
        }
        return transportClient;
    }

    @Primary
    @Bean("easyElasticsearch")
    public Client easyElasticsearch(ConfigProperties configProperties){
        if (StringUtils.isEmpty(client) || StringUtils.isEmpty(client.admin())){
            client = transportClient(configProperties);
            return client;
        }
        return client;
    }
}
