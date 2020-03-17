package com.yeepay.dnsmanage;

import com.alibaba.dcm.DnsCache;
import com.alibaba.dcm.DnsCacheManipulator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class Demo {

    public static void main(String args[]){
        // 设置DNS，随便设置个IP
        DnsCacheManipulator.setDnsCache("www.baidu.com", "192.168.1.1");

        HttpPost post = new HttpPost("http://www.baidu.com");
        HttpClient client = HttpClients.createDefault();
        HttpClientContext httpContext = HttpClientContext.create();
        try {
            //访问成功，返回200
            HttpResponse httpResponse = client.execute(post, httpContext);
            System.out.println(httpResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        DnsCache dnsCache = DnsCacheManipulator.getWholeDnsCache();
        System.out.println("1:"+dnsCache);

        // 设置DNS，随便设置个IP
        DnsCacheManipulator.setDnsCache("www.baidu.com", "192.168.1.1");

        dnsCache = DnsCacheManipulator.getWholeDnsCache();
        System.out.println("2:"+dnsCache);

        try {
            //重新执行一次，访问不通
            HttpResponse httpResponse = client.execute(post, httpContext);
            System.out.println(httpResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        dnsCache = DnsCacheManipulator.getWholeDnsCache();
        System.out.println("3:"+dnsCache);

        //删除DNS缓存
        DnsCacheManipulator.removeDnsCache("www.baidu.com");

        //查看DNS缓存为空
        dnsCache = DnsCacheManipulator.getWholeDnsCache();
        System.out.println("4:"+dnsCache);

        try {
            //重新执行一次，成功返回200
            HttpResponse httpResponse = client.execute(post, httpContext);
            System.out.println(httpResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        dnsCache = DnsCacheManipulator.getWholeDnsCache();
        System.out.println("5:"+dnsCache);




    }
}
