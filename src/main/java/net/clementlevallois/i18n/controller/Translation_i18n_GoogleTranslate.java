/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package net.clementlevallois.i18n.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.*;
import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author LEVALLOIS
 */
public class Translation_i18n_GoogleTranslate {

    public static Map<String, String> runTranslation(int maxRun, Set<Object> keySetSource, Set<String> keySetTarget, Properties propsSource, Properties propsParams, String sourceLang, String langTarget, Properties propsTarget) throws FileNotFoundException, IOException {

        String jsonPath = propsParams.getProperty("path_to_Google_Secret");

        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

        Translate translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();

        int counter = 0;
        Map<String, String> targetKeyValues = new HashMap();

        for (Object keyObject : keySetSource) {
            counter++;
            if (counter > maxRun) {
                break;
            }

            String key = (String) keyObject;
            Object targetValue = propsTarget.get(keyObject);
            String targetValueString = "";
            if (targetValue != null) {
                targetValueString = (String) targetValue;
            }

            // if the key already exists in the target language, don't handle it.
            if (keySetTarget.contains(key) & targetValue != null && !targetValueString.isBlank()) {
                continue;
            }

            String valueSource = propsSource.getProperty(key);

            Translation translationToolByGoogle = translate.translate(
                    valueSource,
                    Translate.TranslateOption.sourceLanguage(sourceLang),
                    Translate.TranslateOption.targetLanguage(langTarget),
                    // Use "base" for standard edition, "nmt" for the premium model.
                    Translate.TranslateOption.model("nmt"));
            String translation = translationToolByGoogle.getTranslatedText();
            targetKeyValues.put(key, translation);

        }

        return targetKeyValues;

    }

}
