/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package net.clementlevallois.i18n.controller;

import java.io.File;
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
public class ControllerUmigonExplain {

    static String[] langsTargetDeepL = new String[]{"PT-BR", "PT-PT"};
    
    // list from https://cloud.google.com/translate/docs/languages?hl=fr
    static String[] langsTargetGoogle = new String[]{"AZ", "BE", "BN", "FR", "BS", "CA", "CO", "EO", "ET", "EU", "FI", "GL", "GU", "FY", "HA", "HE","HI", "HMN", "HAW", "HR", "HU", "HT", "ID", "IG", "IS", "KA", "SQ", "AM", "AR", "HY", "CEB", "GA", "JA", "JV", "KN", "KK", "KM", "RW", "KO", "KU", "KY", "LV", "LT", "LB", "MK", "MG", "MS", "ML", "MT", "MI", "MR", "MN", "MY", "NE", "NO", "NY", "OR", "PS", "FA", "PL", "PT", "PA", "RO", "RU", "SM", "GD", "SR", "ST", "SN", "SD", "SI", "SK", "SL", "SO", "SU", "SW", "SV", "TL", "TG", "TA", "TR", "TT", "TE", "TH", "TK", "UK", "UR", "UG", "UZ", "VI", "CY", "XH", "YI", "YO", "ZU", "LO", "ZH-TW", "BG", "CS", "DA", "DE", "EL", "ES", "IT", "NL", "ZH"};
    static String[] langsTarget;

    static String propertiesSourceLanguage = "text_en.properties";
    static String sourceLang = "EN";
    static String fullPathToPropertiesSourceLanguage;
    static String translationService = "DeepL"; // can be "Google" or "DeepL"
    static Properties propsParams;

    public static void main(String[] args) throws URISyntaxException, UnsupportedEncodingException, FileNotFoundException, IOException {
        propsParams = loadProperties("private/propsUmigonExplain.properties");
        String pathToI18Nproperties = propsParams.getProperty("path_to_i18n_properties");
        fullPathToPropertiesSourceLanguage = pathToI18Nproperties + propertiesSourceLanguage;

        Properties propsSource = loadProperties(fullPathToPropertiesSourceLanguage);

        if (translationService.equals("Google")) {
            langsTarget = langsTargetGoogle;
        } else {
            langsTarget = langsTargetDeepL;
        }

        Set<Object> keySetSource = propsSource.keySet();

        for (String langTarget : langsTarget) {

            String fileSuffix = langTarget.replace("-", "_");
            if (fileSuffix.contains("_")) {
                fileSuffix = fileSuffix.split("_")[0].toLowerCase() + "_" + fileSuffix.split("_")[1];
            } else {
                fileSuffix = fileSuffix.toLowerCase();
            }
            String pathTarget = pathToI18Nproperties + "text_" + fileSuffix + ".properties";
            File f = new File(pathTarget);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
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
                targetKeyValues = Translation_i18n_GoogleTranslate.runTranslation(maxRun, keySetSource, keySetTarget, propsSource, propsParams, sourceLang, langTarget, propsTarget);

            }
            if (translationService.equals("DeepL")) {
                targetKeyValues = Translation_i18n_DeepL.runTranslation(maxRun, keySetSource, keySetTarget, propsSource, propsParams, sourceLang, langTarget, propsTarget);
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
                Logger.getLogger(ControllerUmigonExplain.class.getName()).log(Level.SEVERE, null, ex);
            }

            // testing the print of one value in the translated target
            Properties loadProperties = loadProperties(pathTarget);
            Object oneValue = loadProperties.getProperty("decision.motive.WINNER_TAKES_ALL.NGRAM");
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
