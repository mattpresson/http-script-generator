package com.h3xstream.scriptgen.model;

import javax.xml.bind.DatatypeConverter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represent the model of an HTTP request.
 * The views (templates) will make reference to this POJO.
 * <p>
 * NOTE: This POJO permit abstraction from the initiator (Burp proxy or ZAP).
 */
public class HttpRequestInfo {

    private String method;
    private String url;
    private String hostname;
    private Map<String, String> parametersGet;
    private Map<String, String> parametersPost;
    private String postData;
    private Map<String, String> headers;
    private Map<String, String> cookies = new HashMap<String, String>();

    private AuthCredential basicAuth;
    //private AuthCredential digestAuth;

    public HttpRequestInfo(String method, String url, Map<String, String> parametersGet, Map<String, String> parametersPost, String postData, Map<String, String> headers) {
        this.method = method;
        this.url = url;
        try {
            this.hostname = new URL(this.url).getHost();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.parametersGet = parametersGet;
        this.parametersPost = parametersPost;
        this.postData = postData;
        this.headers = headers;

        extractHeaders();
        if(this.parametersGet != null && this.parametersGet.size() == 0) this.parametersGet = null;
        if(this.parametersPost != null && this.parametersPost.size() == 0) this.parametersPost = null;
        if(this.headers != null && this.headers.size() == 0) this.headers = null;
    }

    private void extractHeaders() {

        for(Iterator<Map.Entry<String, String>> it = this.headers.entrySet().iterator(); it.hasNext(); ) {

            Map.Entry<String, String> entry = it.next();

            //Host
            if(entry.getKey().toLowerCase().equals("host")) {
                it.remove();
            }

            //Content-Length
            if(entry.getKey().toLowerCase().equals("content-length")) { //Compute automatically by the HTTP clients
                it.remove();
            }

            //Cookies
            if(entry.getKey().toLowerCase().equals("cookie")) {
                String[] cookiesFound = entry.getValue().split(";");
                for(String cook : cookiesFound) {
                    String[] cookieParts = cook.split("=");
                    cookies.put(cookieParts[0].trim(),cookieParts[1].trim());
                }
                it.remove();
            }

            //Authentication
            if(entry.getKey().toLowerCase().equals("authorization")) {
                String[] valueParts = entry.getValue().split(" ",2);
                if(valueParts.length != 2) continue;

                //Basic authentication
                if(valueParts[0].toLowerCase().equals("basic")) {
                    try {
                        String creds = new String(DatatypeConverter.parseBase64Binary(valueParts[1]));
                        String[] credsParts = creds.split(":");
                        basicAuth = new AuthCredential(credsParts[0],credsParts[1]);
                        it.remove();
                    }
                    catch (IllegalArgumentException e){
                        continue; //Header will stay unchanged
                    }
                }
            }
        }

        if(cookies.size() == 0) cookies = null;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getHostname() {
        return hostname;
    }

    public Map<String, String> getParametersGet() {
        return parametersGet;
    }

    public Map<String, String> getParametersPost() {
        return parametersPost;
    }

    public String getPostData() {
        return postData;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public AuthCredential getBasicAuth() {
        return basicAuth;
    }
}
