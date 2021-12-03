package br.com.mgs.filaIntegracao;

import br.com.sankhya.bh.filaIntegracao.LoginMsca;
import br.com.sankhya.bh.utils.ErroUtils;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConexaoMsca {
    private Gson g = new Gson();
    private int status;
    private LoginMsca login;

    public ConexaoMsca() {
        this.login = LoginMsca.INSTANCE;
    }

    public void loginMGS() throws Exception {
        this.login.login();
    }

    public String sendRequestJson(String url, String request) throws Exception {
        return this.sendRequest(url, request, "application/json");
    }

    public br.com.sankhya.bh.filaIntegracao.ConexaoMsca.Result sendGetRequest(String url) throws Exception {
        String result = this.sendGetRequest(url, "");
        br.com.sankhya.bh.filaIntegracao.ConexaoMsca.Result r = null;

        try {
            r = (br.com.sankhya.bh.filaIntegracao.ConexaoMsca.Result)this.g.fromJson(result, br.com.sankhya.bh.filaIntegracao.ConexaoMsca.Result.class);
        } catch (Exception var5) {
        }

        return r;
    }

    public String sendRequest(String url, String request, String contentType) throws Exception {
        url = this.login.getUrl() + url;
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        HttpPost post = new HttpPost(url);
        post.addHeader("Content-type", contentType);
        if (this.login.getToken() != null) {
            post.addHeader("Authorization", "bearer " + this.login.getToken());
        }

        if (request == null) {
            request = "";
        }

        HttpEntity entity = new ByteArrayEntity(request.getBytes("UTF-8"));
        post.setEntity(entity);
        HttpResponse response = httpClient.execute(post, httpContext);
        this.status = response.getStatusLine().getStatusCode();
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();

        String line;
        while((line = rd.readLine()) != null) {
            result.append(new String(line.getBytes()));
        }

        if (this.status != 200) {
            try {
                Result r = (Result)this.g.fromJson(result.toString(), Result.class);
                ErroUtils.disparaErro("Erro na requisição! Status = " + this.status + "\n Resposta: " + r.Message);
            } catch (Exception var13) {
                ErroUtils.disparaErro("Erro na requisição! Status = " + this.status + "\n Resposta: " + result.toString());
            }
        }

        return result.toString();
    }

    public String sendGetRequest(String url, String contentType) throws Exception {
        url = this.login.getUrl() + url;
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        HttpGet post = new HttpGet(url);
        post.addHeader("Content-type", contentType);
        if (this.login.getToken() != null) {
            post.addHeader("Authorization", "bearer " + this.login.getToken());
        }

        HttpResponse response = httpClient.execute(post, httpContext);
        this.status = response.getStatusLine().getStatusCode();
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();

        String line;
        while((line = rd.readLine()) != null) {
            result.append(new String(line.getBytes()));
        }

        if (this.status != 200) {
            try {
                Result r = (Result)this.g.fromJson(result.toString(), Result.class);
                ErroUtils.disparaErro(r.Message);
            } catch (Exception var11) {
                ErroUtils.disparaErro(result.toString());
            }
        }

        return result.toString();
    }

    public static class AcessToken {
        public String access_token;
        public String token_type;
        public long expires_in;

        public AcessToken() {
        }
    }

    public class Result {
        boolean Success;
        String Message;

        public Result() {
        }
    }
}
