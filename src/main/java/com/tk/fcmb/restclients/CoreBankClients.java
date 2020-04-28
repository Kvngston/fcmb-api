package com.tk.fcmb.restclients;

import com.google.gson.JsonObject;
import com.tk.fcmb.Repositories.SmsLogRepository;
import com.tk.fcmb.handler.LogSMS;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class CoreBankClients {

    private static final int TIMEOUT = 15;

//    @Autowired
//    RestTemplate restTemplate;

    @Value("${fmcb.sms.gateway.url}")
    private String smsGatewayUrl;

    @Value("${fmcb.sms.gateway.proxy}")
    private String smsGatewayProxy;



    @Autowired
    private SmsLogRepository smsLogRepository;

//    public String httpPostClients(String url, String token, JsonObject request) {
//        long start = System.currentTimeMillis();
//        log.info("REQUEST " + request);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(token);
//        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);
//        String responseBody = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
//        log.info("RESPONSE " + responseBody);
//        log.info("TIME TAKEN " + (System.currentTimeMillis() - start) + "ms");
//        return responseBody;
//    }

    public String httpPostClients(String url, String apiKey, JsonObject ob) {
        long start = System.currentTimeMillis();
        String result;
        try {
            HttpClient client = HttpClientBuilder.create().build();

            HttpPost request = new HttpPost(url);
            request.setConfig(requestConfigWithTimeout(TIMEOUT));
            // add request header
            request.addHeader("Authorization", "Bearer " + apiKey);
            request.addHeader("Content-Type", "application/json");

            request.setEntity(new StringEntity(ob.toString()));
            HttpResponse serviceResponse = client.execute(request);

            log.info("Response Code : " + serviceResponse.getStatusLine().getStatusCode());
            log.info("URL: " + url);
            log.info("REQUEST: " + ob);

            result = EntityUtils.toString(serviceResponse.getEntity());


        } catch (IOException ex) {
            JsonObject json = new JsonObject();
            json.addProperty("error", "-1");
            return json.toString();
        }
//        if (url.contains("GetStatement") || url.contains("GetCustomerAccountQuery")) {
//            log.info("logging disabled for " + url);
//        } else {
//            log.info("RESPONSE " + result);
//        }
        log.info("RESPONSE " + result);
        log.info("TIME TAKEN " + (System.currentTimeMillis() - start));
        return result;
    }

//    public String refreshCoreBankingAPI(String url, JsonObject request) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);
//        String token = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
//        log.info("token " + token);
//        return token;
//    }

    public String refreshCoreBankingAPI(String url, JsonObject ob) {
        long start = System.currentTimeMillis();
        String result;
        try {

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(url);

            // add request header
            request.addHeader("Content-Type", "application/json");
            request.setConfig(requestConfigWithTimeout(TIMEOUT));
            request.setEntity(new StringEntity(ob.toString()));
            HttpResponse serviceResponse = client.execute(request);

            log.info("Response Code : " + serviceResponse.getStatusLine().getStatusCode());
            log.info("URL: " + url);
            log.info("REQUEST: " + ob);

            result = EntityUtils.toString(serviceResponse.getEntity());

        } catch (IOException ex) {
            log.error("error ", ex);
            JsonObject json = new JsonObject();
            json.addProperty("error", "-1");
            return json.toString();
        }

        log.info("RESPONSE " + result);
        log.info("TIME TAKEN " + (System.currentTimeMillis() - start));
        return result;
    }

    public void sendSMS(String destinationMobile, String message, String mobileNumber) {
        long start = System.currentTimeMillis();
        String userId = "93158098";
        String apiKey = "c53dYmXp7a8E75z2";
        String sender = "FCMB Beta";

        String result = "";
        try {
            message = URLEncoder.encode(message, "UTF-8");
            sender = URLEncoder.encode(sender, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
            message = "";
            sender = "";
        }
        //http://developers.cloudsms.com.ng/api.php?userid=XXXX&password=YYYYY&type=Y&destination=QQQQQQQQQ&sender=RRRR&message=SSSSSSS
        String queryUrl = smsGatewayUrl + "/api1.php?userid=" + userId + "&password=" + apiKey + "&type=5&destination=" + destinationMobile + "&sender=" + sender + "&message=" + message + "";
        try {

            //if (proxyServer.isPresent()) {
            String[] p = smsGatewayProxy == null ? "127.0.0.1:80".split("\\:") : smsGatewayProxy.split("\\:");


            HttpHost proxy = new HttpHost(p[0], Integer.parseInt(p[1]));
            RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
            //}

            log.info("REQUEST: " + queryUrl);

            HttpClient client = getCloseableHttpClient();

            HttpGet request = new HttpGet(queryUrl);

            request.setConfig(config);
            HttpResponse serviceResponse = client.execute(request);

            log.info("RESPONSE CODE : " + serviceResponse.getStatusLine().getStatusCode());

            log.info("Proxy Server, " + p[0] + " : " + p[1]);

            result = EntityUtils.toString(serviceResponse.getEntity());

        } catch (IOException ex) {
            log.error("Error sending SMS ", ex);
        }
        log.info("Logging SMS information on the Database.");

        if(message.contains("OTP")){
            message = "";
        }

        try {
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.execute(new LogSMS(smsLogRepository, mobileNumber, destinationMobile, message, result));
            executorService.shutdown();
        } catch (Exception e) {
            log.error("Executor Service Error ", e);
        }

        log.info("RESPONSE " + result);

        log.info("TIME TAKEN " + (System.currentTimeMillis() - start));
    }

    private RequestConfig requestConfigWithTimeout(int timeoutInMilliseconds) {
        return RequestConfig.copy(RequestConfig.DEFAULT)
                .setSocketTimeout(timeoutInMilliseconds * 1000)
                .setConnectTimeout(timeoutInMilliseconds * 1000)
                .setConnectionRequestTimeout(timeoutInMilliseconds * 1000)
                .build();
    }

    private static CloseableHttpClient getCloseableHttpClient() {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.custom().
                    setSSLHostnameVerifier(new NoopHostnameVerifier()).
                    setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, (X509Certificate[] arg0, String arg1) -> true).build()).build();
        } catch (KeyManagementException e) {
            log.error("KeyManagementException in creating http client instance", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException in creating http client instance", e);
        } catch (KeyStoreException e) {
            log.error("KeyStoreException in creating http client instance", e);
        }
        return httpClient;
    }


//    public void sendSMS_(String senderMobile, String message, String mobileNumber) throws Exception {
//
//        long start = System.currentTimeMillis();
//        String userId = "93158098";
//        String apiKey = "c53dYmXp7a8E75z2";
//        String sender = "FCMB Beta";
//
//        mobileNumber = !mobileNumber.startsWith("234") ? "234" + mobileNumber.substring(1) : mobileNumber;
//
////        try {
////            message = URLEncoder.encode(message, "UTF-8");
////            sender = URLEncoder.encode(sender, "UTF-8");
////        } catch (UnsupportedEncodingException e) {
////            //e.printStackTrace();
////            message = "";
////            sender = "";
////        }
//        String[] p = smsGatewayProxy == null ? "127.0.0.1:80".split("\\:") : smsGatewayProxy.split("\\:");
//        SimpleClientHttpRequestFactory clientHttpReq = new SimpleClientHttpRequestFactory();
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(p[0], Integer.parseInt(p[1])));
//        clientHttpReq.setProxy(proxy);
//        restTemplate = new RestTemplate();
//        restTemplate.setRequestFactory(clientHttpReq);
//        String queryUrl = smsGatewayUrl + "/api1.php?userid=" + userId + "&password=" + apiKey + "&type=5&destination=" + mobileNumber + "&sender=" + sender + "&message=" + message;
//
//        log.info(queryUrl);
//
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        String responseCode = restTemplate.exchange(queryUrl, HttpMethod.GET, entity, String.class).getBody();
//
//
//        log.info("Logging SMS information on the Database.");
//
//        try {
//            ExecutorService executorService = Executors.newCachedThreadPool();
//            executorService.execute(new LogSMS(smsLogRepository, senderMobile, mobileNumber, message, responseCode));
//            executorService.shutdown();
//        } catch (Exception e) {
//            log.error("Executor Service Error ", e);
//        }
//        log.info("TIME TAKEN " + (System.currentTimeMillis() - start));
//    }
}
