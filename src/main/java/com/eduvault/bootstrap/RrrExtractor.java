package com.eduvault.bootstrap;

import com.eduvault.pdf.PdfReaderUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RrrExtractor {
    public static String extractRRR(String pdfText) {
        Pattern pattern = Pattern.compile("\\b\\d{12,}\\b");
        Matcher matcher = pattern.matcher(pdfText);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static void main(String[] args) {
        String text = PdfReaderUtil.extractText("\"C:\\Users\\sammy\\OneDrive\\Downloads\\Okuboyejo Samuel Receipt.pdf\"");
        String rrr = extractRRR(text);
        System.out.println("RRR Found: " + rrr);
    }
}
