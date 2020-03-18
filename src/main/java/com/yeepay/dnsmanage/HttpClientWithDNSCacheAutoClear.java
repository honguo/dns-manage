package com.yeepay.dnsmanage;

import com.alibaba.dcm.DnsCache;
import com.alibaba.dcm.DnsCacheManipulator;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpClientWithDNSCacheAutoClear {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientWithDNSCacheAutoClear.class);
    private static String charset = "UTF-8";

    private volatile static CloseableHttpClient http_client = null;
    private volatile static RequestConfig request_config = null;


    /**
     * 从连接池获取连接对象超时时间
     */
    private static int connectionRequestTimeout;
    /**
     * 连接超时时间
     */
    private static int connectTimeout;
    /**
     * 读超时时间
     */
    private static int socketTimeout;


    private static HttpClientConnectionManager connMgr = getConnectionManager();

    /**
     * 请求响应结果
     */
    private static String respMessage;
    private static String identification;

    private static volatile boolean shutdown;


    public HttpClientWithDNSCacheAutoClear() {
        connectionRequestTimeout = 5000;
        connectTimeout = 15000;
        socketTimeout = 30000;
        identification = "";
    }

    public HttpClientWithDNSCacheAutoClear(String identification) {
        connectionRequestTimeout = 5000;
        connectTimeout = 15000;
        socketTimeout = 30000;
        identification = identification;
    }

    public HttpClientWithDNSCacheAutoClear(int connectionRequestTimeout, int connectTimeout, int socketTimeout, String identification) {
        connectionRequestTimeout = connectionRequestTimeout;
        connectTimeout = connectTimeout;
        socketTimeout = socketTimeout;
        identification = identification;
    }

    private static void init() {
        if (http_client == null) {
            synchronized (HttpClientWithDNSCacheAutoClear.class) {
                if (http_client == null) {
                    logger.info("init_xhttpclient");
                    http_client = HttpClients
                            .custom()
                            .setConnectionManager(connMgr)
                            .build();
                }
            }
        }
        if (request_config == null) {
            synchronized (HttpClientWithDNSCacheAutoClear.class) {
                if (request_config == null) {
                    logger.info("init_xrequestconfig");
                    request_config = RequestConfig
                            .custom()
                            .setConnectionRequestTimeout(connectionRequestTimeout)
                            .setConnectTimeout(connectTimeout)
                            .setSocketTimeout(socketTimeout).build();
                }
            }
        }

    }


    /**
     * @param url
     * @param headers
     * @param params
     * @return
     */
    public static int doPost(String url, Map<String, String> headers, Map<String, String> params, String identification) {
        HttpPost httpPost = null;
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        try {
            if (params != null) {
                Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> entry = it.next();
                    BasicNameValuePair nameValue = new BasicNameValuePair(entry.getKey(), entry.getValue());
                    formParams.add(nameValue);
                }
            }
        } catch (Exception e) {
            logger.error("HttpClientWithDNSCacheAutoClearPOST Params Err:" + identification + " " + (params != null ? JSON.toJSONString(params) : "null."), e);
            throw new RuntimeException("HttpClientWithDNSCacheAutoClearPOST Params Err:" + identification, e);
        }
        try {
            init();
            httpPost = createHttpPost(url, headers);
            if (httpPost == null) {
                logger.error("CreateHttpPostError：" + identification + " " + (formParams != null ? JSON.toJSONString(formParams) : "null."));
                throw new RuntimeException("CreateHttpPostError：" + identification);
            }
            UrlEncodedFormEntity ueEntity = new UrlEncodedFormEntity(formParams, charset);
            httpPost.setEntity(ueEntity);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            HttpResponse response = http_client.execute(httpPost);
            logger.info("HttpClientPost_ExecTime:" + stopWatch.toString() + " ms " + identification + " " + (formParams != null ? JSON.toJSONString(formParams) : "null."));
            stopWatch.stop();
            HttpEntity entity = response.getEntity();
            StatusLine statusLine = response.getStatusLine();
            if (entity != null) {
                String str = EntityUtils.toString(entity, charset);
                respMessage = str;
            }
            return statusLine.getStatusCode();
        } catch (ClientProtocolException e) {
            logger.error("XHttp_ClientProtocolException " + identification + " " + (formParams != null ? JSON.toJSONString(formParams) : "null."), e);
            throw new RuntimeException("XHttp_ClientProtocolException " + identification + " " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            logger.error("XHttp_UnsupportedEncodingException " + identification + " " + (formParams != null ? JSON.toJSONString(formParams) : "null."), e);
            throw new RuntimeException("XHttp_UnsupportedEncodingException " + identification + " " + e.getMessage());
        } catch (IOException e) {
            logger.error("XHttp_IOException " + identification + " " + (formParams != null ? JSON.toJSONString(formParams) : "null."), e);
            throw new RuntimeException("XHttp_IOException " + identification + " " + e.getMessage());
        } catch (Exception e) {
            logger.error("XHttp_Exception " + identification + " " + (formParams != null ? JSON.toJSONString(formParams) : "null."), e);
            throw new RuntimeException("XHttp_Exception " + identification + " " + e.getMessage());
        } finally {
            if (httpPost != null) {
                try {
                    httpPost.releaseConnection();
                } catch (Exception e) {
                    logger.error("HttpClientWithDNSCacheAutoClearReleaseConnectionError " + identification, e);
                }
            }
        }
    }

    /**
     * @param url
     * @param headers
     * @return
     */
    public static int doGet(String url, Map<String, String> headers, String identification) {
        HttpGet httpGet = null;
        try {
            init();
            httpGet = createHttpGet(url, headers);
            if (httpGet == null) {
                logger.error("CreateHttpGetError：" + identification);
                throw new RuntimeException("CreateHttpGetError：" + identification);
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            HttpResponse response = http_client.execute(httpGet);
            logger.info("HttpClientGet_ExecTime:" + stopWatch.toString() + " ms " + identification);
            stopWatch.stop();
            HttpEntity entity = response.getEntity();
            StatusLine statusLine = response.getStatusLine();
            if (entity != null) {
                String str = EntityUtils.toString(entity, charset);
                respMessage = str;
            }
            return statusLine.getStatusCode();
        } catch (ConnectTimeoutException e){
            DnsCache dnsCache = DnsCacheManipulator.getWholeDnsCache();
            System.out.println("11:"+dnsCache);
            //删除失效DNS缓存
            DnsCacheManipulator.removeDnsCache(url);
            dnsCache = DnsCacheManipulator.getWholeDnsCache();
            System.out.println("12:"+dnsCache);
            //清理失效连接
            connMgr.closeExpiredConnections();
            return 501;
        }
        catch (Exception e) {
            logger.error("XHttp_Exception " + identification, e);
            return 500;
//            throw new RuntimeException("XHttp_Exception " + identification + " " + e.getMessage());
        } finally {
            if (httpGet != null) {
                try {
                    httpGet.releaseConnection();
                } catch (Exception e) {
                    logger.error("HttpClientWithDNSCacheAutoClearReleaseConnectionError " + identification, e);
                }
            }
        }
    }


    /**
     * @param url
     * @param headers
     * @return
     */
    private static HttpPost createHttpPost(String url, Map<String, String> headers) {
        if (url == null || url.length() == 0) return null;
        HttpPost httpPost = new HttpPost();
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }
        httpPost.setURI(URI.create(url));
        httpPost.setConfig(request_config);
        return httpPost;
    }

    /**
     * @param url
     * @param headers
     * @return
     */
    private static HttpGet createHttpGet(String url, Map<String, String> headers) {
        if (url == null || url.length() == 0) return null;
        HttpGet httpGet = new HttpGet();
        httpGet.setURI(URI.create(url));
        httpGet.setConfig(request_config);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return httpGet;
    }

    /**
     * @return
     */
    public static HttpClientConnectionManager getConnectionManager() {
        try {
            System.out.println("getCon init");
            ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", plainsf)
                    .build();
            PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
            manager.setMaxTotal(5);
            manager.setDefaultMaxPerRoute(5);
            manager.setDefaultMaxPerRoute(manager.getMaxTotal());
            return manager;
        } catch (Exception e) {
            logger.error("getConnectionManager Exception", e);
            throw new RuntimeException("getConnectionManager Exception " + e.getMessage());
        }
    }


    public String getRespMessage() {
        return respMessage;
    }

}
