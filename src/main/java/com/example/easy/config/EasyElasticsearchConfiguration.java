package com.example.easy.config;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;


@Slf4j
@Configuration
@ComponentScan(basePackages = {"com.example.easy.**"})
public class EasyElasticsearchConfiguration {

    private Client client;

    @Autowired
    private Environment environment;

    private static String CLUSTER_NAME;

    private static String CLUSTER_NODES;

    private static int CLUSTER_PORT;

    public void init(){
        CLUSTER_NAME = environment.getProperty("easy.elasticsearch.cluster.name");
        CLUSTER_NODES = environment.getProperty("easy.elasticsearch.cluster.nodes");
        CLUSTER_PORT = Integer.parseInt(environment.getProperty("easy.elasticsearch.cluster.port"));
    }

    @Bean
    public Client transportClient(){
        TransportClient transportClient = null;
        try {
            transportClient = new PreBuiltTransportClient(Settings.builder()
                    .put("cluster.name",CLUSTER_NAME)
                    .build())
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(CLUSTER_NODES),CLUSTER_PORT));
        }catch (UnknownHostException e){
            log.error("elasticsearch client error : ",e.getMessage());
        }
        return transportClient;
    }


    public Client transportClient(String clusterName , String nodes ,int port ,String userName ,String password ){
        TransportClient transportClient = null;
        try {
            StringBuffer xpack = new StringBuffer(userName);
            xpack.append(":").append(password);
            transportClient = new PreBuiltTransportClient(Settings.builder()
                    .put("cluster.name",clusterName)
                    .put("xpack.security,user",xpack.toString())
                    .build())
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(nodes),port));
        }catch (UnknownHostException e){
            log.error("elasticsearch client error : ",e.getMessage());
        }
        return transportClient;
    }

    @Bean
    public Client easyElasticsearch(){
        if (StringUtils.isEmpty(client) || StringUtils.isEmpty(client.admin())){
            client = transportClient();
            return client;
        }
        return client;
    }
}
