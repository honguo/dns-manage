package com.yeepay.dnsmanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DnsManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(DnsManageApplication.class, args);
//        IdleConnectionMonitorThread thread = new IdleConnectionMonitorThread(HttpClientWithDNSCacheAutoClear.getConnectionManager());
//        thread.run();
    }

}
