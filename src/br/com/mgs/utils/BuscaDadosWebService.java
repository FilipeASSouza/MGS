package br.com.mgs.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BuscaDadosWebService {

    private int status;

    public BuscaDadosWebService() {
    }

    public String buscar(String url, String request) throws IOException {
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
        BasicHttpContext basicHttpContext = new BasicHttpContext();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-type", "application/json");
        if (request == null) {
            request = "";
        }

        HttpEntity entity = new ByteArrayEntity(request.getBytes("UTF-8"));
        httpPost.setEntity(entity);
        HttpResponse response = defaultHttpClient.execute(httpPost, basicHttpContext);
        this.status = response.getStatusLine().getStatusCode();
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();

        String line;
        while((line = rd.readLine()) != null) {
            result.append(new String(line.getBytes()));
        }

        return result.toString();
    }
}
