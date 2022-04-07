/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package net.clementlevallois.i18n.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LEVALLOIS
 */
public class Controller {

    static String[] langsTarget = new String[]{"LO", "FR", "IT", "ES", "PT-BR", "ZH", "DE"};

    static String propertiesSourceLanguage = "text_en.properties";
    static String fullPathToPropertiesSourceLanguage;
    static String translationService = "Google";
    static Properties propsParams;

    public static void main(String[] args) throws URISyntaxException, UnsupportedEncodingException, FileNotFoundException, IOException {
        propsParams = loadProperties("/private/props.properties");
        String pathToI18Nproperties = propsParams.getProperty("path to i18n properties");
        fullPathToPropertiesSourceLanguage = pathToI18Nproperties + propertiesSourceLanguage;

        Properties propsSource = loadProperties(fullPathToPropertiesSourceLanguage);

        Set<Object> keySetSource = propsSource.keySet();

        for (String langTarget : langsTarget) {

            String fileSuffix = langTarget.replace("-", "_");
            if (fileSuffix.contains("_")) {
                fileSuffix = fileSuffix.split("_")[0].toLowerCase() + "_" + fileSuffix.split("_")[1];
            } else {
                fileSuffix = fileSuffix.toLowerCase();
            }
            String pathTarget = pathToI18Nproperties + "text_" + fileSuffix + ".properties";
            Properties propsTarget = loadProperties(pathTarget);
            Set<Object> keySetObjectTarget = propsTarget.keySet();
            Set<String> keySetTarget = new HashSet();
            for (Object key : keySetObjectTarget) {
                keySetTarget.add((String) key);
            }

            Map<String, String> targetKeyValues = new HashMap();

            // max run for tests
            int maxRun = Integer.MAX_VALUE;

            if (translationService.equals("Google")) {
                targetKeyValues = Translation_i18n_GoogleTranslate.runTranslation(maxRun, keySetSource, keySetTarget, propsSource, langTarget, propsTarget);

            }
            if (translationService.equals("DeepL")) {
                targetKeyValues = Translation_i18n_DeepL.runTranslation(maxRun, keySetSource, keySetTarget, propsSource, langTarget, propsTarget);
            }

            for (Map.Entry<String, String> entry : targetKeyValues.entrySet()) {
//                System.out.println("key: " + entry.getKey());
//                System.out.println("value: " + entry.getValue());
//                System.out.println("-----------------");
                propsTarget.put(entry.getKey(), entry.getValue());
            }

            try ( OutputStream output = new FileOutputStream(pathTarget)) {
                propsTarget.store(output, null);
            } catch (IOException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }

            // testing the print of one value in the translated target
            Properties loadProperties = loadProperties(pathTarget);
            Object oneValue = loadProperties.getProperty("labelling.item_eval.categorization.annotate_injunction");
            System.out.println("one value: " + oneValue);
        }
    }

    private static Properties loadProperties(String path) {
        Properties prop = new Properties();
        try ( InputStream input = new FileInputStream(path)) {

            // load a properties file
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;

    }
}
