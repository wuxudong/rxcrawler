package com.mrkid.ecommerce.itjuzi;

import java.util.ArrayList;
import java.util.List;

/**
 * User: xudong
 * Date: 7/24/16
 * Time: 2:30 PM
 */
public class Company {
    public String com_id;
    public String com_name;
    public String com_registered_name;
    public String com_des;
    public String com_url;
    public String com_born_year;
    public String com_born_month;
    public String com_prov;
    public String com_city;

    public List<CompanyScope> company_scope = new ArrayList<>();
    public List<CompanyScope> company_sub_scope;

    public CompanyStatus company_status;
    public CompanyStage company_stage;
    public CompanyFundStatus company_fund_status;

    public List<CompanyMileStone> company_mile_stones = new ArrayList<>();
    public List<CompanyInvestEvent> company_invest_events = new ArrayList<>();
    public CompanyFundNeedsStatus company_fund_needs_status;

    public static class CompanyScope {
        public String cat_name;
    }

    public static class CompanyStage {
        public String com_stage_name;
    }

    public static class CompanyStatus {
        public String com_status_name;
    }

    public static class CompanyMileStone {
        public String com_mil_year;
        public String com_mil_month;
        public String com_mil_detail;

        @Override
        public String toString() {
            return com_mil_year + '-' + com_mil_month + ':' + com_mil_detail;
        }
    }

    public static class CompanyInvestEvent {
        public String invse_year;
        public String invse_month;
        public String invse_des;
        public InvseSimilarMoney invse_similar_money;

        public static class InvseSimilarMoney {
            public String invse_similar_money_name;
        }

        @Override
        public String toString() {
            return invse_year + '-' + invse_month + " " +
                    (invse_similar_money == null ? " " : invse_similar_money.invse_similar_money_name) +
                    ":" + invse_des;
        }

    }

    public static class CompanyFundStatus {
        public String com_fund_status_name;
    }

    public static class CompanyFundNeedsStatus {
        public String com_fund_needs_name;
    }


}
