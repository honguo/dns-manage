package com.yeepay.dnsmanage;

import com.alibaba.dcm.DnsCache;
import com.alibaba.dcm.DnsCacheManipulator;
import org.apache.http.HttpResponse;

import java.io.IOException;

public class ClientPoolDemo {

    public static void main(String[] args){
        DnsCacheManipulator.removeDnsCache("www.baidu.com");

        // 设置DNS，随便设置个IP
        DnsCacheManipulator.setDnsCache("www.baidu.com", "192.168.1.1");

        HttpClientWithDNSCacheAutoClear client = new HttpClientWithDNSCacheAutoClear();
        int code = client.doGet("http://www.baidu.com",null,null);
        System.out.println(code);

        DnsCache dnsCache = DnsCacheManipulator.getWholeDnsCache();
        System.out.println("1:"+dnsCache);

        //重新执行一次
        code = client.doGet("http://www.baidu.com",null,null);
        System.out.println(code);

        dnsCache = DnsCacheManipulator.getWholeDnsCache();
        System.out.println("3:"+dnsCache);


    }
}
