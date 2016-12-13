package com.mrkid.ecommerce.itjuzi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xudong
 * Date: 7/24/16
 * Time: 2:19 PM
 */
public class CSVExtractor {

    final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }


    private List<String> header() {
        List<String> l = new ArrayList<>();

        l.add("com_id");
        l.add("com_name");
        l.add("com_registered_name");
        l.add("com_des");
        l.add("com_url");
        l.add("com_born_year");
        l.add("com_born_month");
        l.add("com_prov");
        l.add("com_city");

        l.add("company_scope");
        l.add("company_sub_scope");


        l.add("company_status");
        l.add("company_stage");
        l.add("company_fund_status");
        l.add("company_fund_needs_status");


        l.add("company_mile_stones");
        l.add("all_company_mile_stones");

        l.add("company_invest_events");
        l.add("all_company_invest_events");


        return l;

    }

    private List<String> toRecord(Company company) {
        List<String> l = new ArrayList<>();

        l.add(company.com_id);
        l.add(company.com_name);
        l.add(company.com_registered_name);
        l.add(company.com_des);
        l.add(company.com_url);
        l.add(company.com_born_year);
        l.add(company.com_born_month);
        l.add(company.com_prov);
        l.add(company.com_city);

        l.add(company.company_scope.isEmpty() ? "" : company.company_scope.get(0).cat_name);
        l.add(company.company_sub_scope.isEmpty() ? "" : company.company_sub_scope.get(0).cat_name);


        l.add(company.company_status == null ? "" : company.company_status.com_status_name);
        l.add(company.company_stage == null ? "" : company.company_stage.com_stage_name);
        l.add(company.company_fund_status == null ? "" : company.company_fund_status.com_fund_status_name);
        l.add(company.company_fund_needs_status == null ? "" : company.company_fund_needs_status.com_fund_needs_name);


        l.add(company.company_mile_stones.isEmpty() ? "" : company.company_mile_stones.get(0).toString());
        l.add(company.company_mile_stones.isEmpty() ? "" : StringUtils.join(company
                .company_mile_stones, '\n'));

        try {
            l.add(company.company_invest_events.isEmpty() ? "" : company.company_invest_events.get(0).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        l.add(company.company_invest_events.isEmpty() ? "" : StringUtils.join(company
                .company_invest_events, '\n'));


        return l;

    }

    private void printDir(File f, CSVPrinter csvPrinter) {
        if (f.isFile() && f.getName().startsWith("com_")) {
            try {
                Reader reader = new InputStreamReader(new FileInputStream(f), "utf-8");
                final Company company = objectMapper.readValue(reader, Company.class);
                csvPrinter.printRecord(toRecord(company));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (f.isDirectory()) {
            for (File sub : f.listFiles()) {
                printDir(sub, csvPrinter);
            }
        }
    }

    public void parse() throws IOException, InterruptedException {
        Writer fileWriter = new OutputStreamWriter(new FileOutputStream("company.csv"), "utf-8");
        final CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(header().toArray(new String[0]));
        final CSVPrinter csvPrinter = new CSVPrinter(fileWriter, csvFormat);


        printDir(new File("output"), csvPrinter);

        csvPrinter.close();
    }

}
