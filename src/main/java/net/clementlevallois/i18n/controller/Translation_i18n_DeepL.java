/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.clementlevallois.i18n.controller;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import static net.clementlevallois.i18n.controller.Controller.propsParams;

/**
 *
 * @author LEVALLOIS
 */
public class Translation_i18n_DeepL {

    public static Map<String, String> runTranslation(Integer maxRun, Set<Object> keySetSource, Set<String> keySetTarget, Properties propsSource, String langTarget, Properties propsTarget) {
        HttpRequest request;
        HttpClient client = HttpClient.newHttpClient();
        int counter = 0;
        Set<CompletableFuture> futures = new HashSet();
            Map<String, String> targetKeyValues = new HashMap();

        try {

            for (Object keyObject : keySetSource) {
                counter++;
                if (counter > maxRun) {
                    break;
                }

                String key = (String) keyObject;
                Object targetValue = propsTarget.get(keyObject);
                String targetValueString = "";
                if (targetValue!=null){
                    targetValueString = (String) targetValue;
                }

                // if the key already exists in the target language, don't handle it.
                if (keySetTarget.contains(key) & targetValue!=null && !targetValueString.isBlank()) {
                    continue;
                }
                String valueSource = propsSource.getProperty(key);
                String auth_key = "auth_key" + "=" + propsParams.getProperty("deepl_api_key")+ ":fx";
                String text = "text" + "=" + URLEncoder.encode(valueSource, StandardCharsets.UTF_8.toString());
                String target_lang = "target_lang" + "=" + langTarget;
                String source_lang = "source_lang" + "=" + "EN";
                String tag_handling = "tag_handling" + "=" + "html";

                URI uri = new URI("https://api-free.deepl.com/v2/translate?" + tag_handling + "&" + source_lang + "&" + auth_key + "&" + text + "&" + target_lang);

                request = HttpRequest.newBuilder()
                        .uri(uri)
                        .build();

                CompletableFuture<Void> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
                    String body = resp.body();
//                    System.out.println("response: " + body);
                    JsonReader jr = Json.createReader(new StringReader(body));
                    JsonObject read = jr.readObject();
                    for (String keySeries : read.keySet()) {
                        JsonArray value = read.getJsonArray(keySeries);
                        if (value != null && !value.isEmpty()) {
                            for (int i = 0; i < value.size(); i++) {
                                JsonObject values = value.getJsonObject(i);
                                String translation = values.getString("text");
                                targetKeyValues.put(key, translation);
                            }
                        }
                    }
                });
                futures.add(future);
                
                // this is because we need to slow down a bit the requests to DeepL - sending too many thros a
                // java.util.concurrent.CompletionException: java.io.IOException: too many concurrent streams
                Thread.sleep(20);
                
            }

        } catch (UnsupportedEncodingException | URISyntaxException | InterruptedException ex) {
            
            Logger.getLogger(Translation_i18n_DeepL.class.getName()).log(Level.SEVERE, null, ex);
        }
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures.toArray((new CompletableFuture[0])));
        combinedFuture.join();

        return targetKeyValues;
        
    }
}
