package org.fisco.bcos;

import org.fisco.bcos.asset.contract.Asset;

import java.math.BigInteger;

import org.fisco.bcos.asset.client.AssetClient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) throws Exception{
        System.out.println("<---------------- client ---------------->");
        AssetClient client = new AssetClient();
        client.initialize();
        client.deployAssetAndRecordAddr();

        System.out.println("<---------------- client end ---------------->");
        SpringApplication.run(Application.class, args);
    }
}
