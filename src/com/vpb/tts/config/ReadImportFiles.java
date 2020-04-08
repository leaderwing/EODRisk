/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vpb.tts.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author quynx
 */
public class ReadImportFiles {
    public List<String> readListFileNames(String columnMappingFile) throws IOException {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = ReadImportFiles.class.getClassLoader().getResourceAsStream(columnMappingFile);
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br  = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
                result.add(line);
            }
        }
        return result;
    }
    public static String getFileType(String fileName) {
        if(StringUtils.contains(fileName, "SpotQuotes")) {
            return "SpotQuotes";
        }else if (StringUtils.contains(fileName, "BondPrice")) {
            return "BondsQuotes";
        } else if (StringUtils.contains(fileName, "ForwardPoints")) {
            return "FxSwapRevalPoints";
        }else if (StringUtils.contains(fileName, "FloatingRates")) {
            return "FloatingRatesValues";
        }else if (StringUtils.contains(fileName, "Curves")) {
            return "CurvesRates";
        }
        return null;
        }
}
