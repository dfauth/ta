package com.github.dfauth.ta.model.txn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.model.Side;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.dfauth.ta.model.txn.Constants.lookup;
import static java.util.function.Predicate.not;

public enum TxnType implements UnaryOperator<BigDecimal> {
    PAYMENT(TxnType::parsePaymentString), // payment
    DEP(TxnType::parseDepositString),  // deposit
    CREDIT(TxnType::parseCreditString),  // credit
    INT(TxnType::parseInterestString), // interest
    DIV(TxnType::parseDividendString), // dividend
    OTHER(TxnType::parseOtherString);

    private static Pattern ASXCODE = Pattern.compile("^ASX\\:([A-Z0-9]{3})$");

    public static String lookupCode(String asxcode) {
        return Optional.ofNullable(lookup(asxcode)).orElse(null);
    }

    public static boolean validateCode(String asxcode) {
        return Optional.ofNullable(asxcode).map(ASXCODE::matcher).filter(Matcher::matches).isPresent();
    }

    private static final BigDecimal MINUS_ONE = BigDecimal.valueOf(-1);
    private Function<Payment.PaymentFactory, Payment> f;

    TxnType(Function<Payment.PaymentFactory, Payment> f) {
        this.f = f;
    }

    public Payment toJson(Payment.PaymentFactory e) {
        return f.apply(e);
    }

    public boolean isDividend() {
        return this == DIV;
    }

    private static final String PREAMBLE = "PAYMENT BY AUTHORITY TO WESTPAC SECURITI ";
    private static final int PREAMBLE_LENGTH = PREAMBLE.length();
    private static Payment parseInterestString(Payment.PaymentFactory e) {
        // GROSS INT         155.74 INC BONUS           9.35 TAX 10.00%         15.00 NET INTERES
        return new Payment(0l, INT, e.date, e.detail, e.credit, e.balance, null, null, null);
    }

    private static Payment parseCreditString(Payment.PaymentFactory e) {
        if(e.detail.toUpperCase().startsWith("DEPOSIT ONLINE ")) {
            // DEPOSIT ONLINE 2081482 TFR Westpac Ch
            String tmp = e.detail.substring("DEPOSIT ONLINE ".length());
            String[] strings = tmp.split(" ");
            String contractNo = strings[0];
            return new Payment(0l, CREDIT, e.date, e.detail, e.credit, e.balance, null, null, contractNo);
        } else if(e.detail.startsWith("Deposit - Internet Online Banking")) {
            // Deposit - Internet Online Banking 2912267  Fnds Tfr 04-Dec
            return new Payment(0l, CREDIT, e.date, e.detail, e.credit, e.balance, null, null, null);
        } else {
            throw new IllegalArgumentException("Unknown or unsupportedf credit type: "+e.detail);
        }
    }

    private static Payment parseDepositString(Payment.PaymentFactory e) {
        // DEPOSIT WESTPAC SECURITI        S VUL 42584340-00
        String code;
        String contractNo;
        if(e.detail.toUpperCase().startsWith("DEPOSIT WESTPAC SECURITI")) {
            String tmp = e.detail.toUpperCase().substring("DEPOSIT WESTPAC SECURITI".length());
            String[] strings = Arrays.stream(tmp.trim().split(" ")).filter(not(""::equals)).toArray(String[]::new);
            Side side = Side.fromString(strings[0]);
            code = strings[1];
            contractNo = strings[strings.length-1];
            return new Payment(0l, DEP, e.date, e.detail, e.credit, e.balance, side, Optional.ofNullable(e.code).orElseGet(() -> lookupCode("ASX:"+code.toUpperCase())), contractNo);
        } else if(e.detail.toUpperCase().startsWith("DEPOSIT ")) {
            // DEPOSIT ALTIUM LIMITED        SOA24/0080705
            String tmp = e.detail.substring("DEPOSIT ".length());
            String[] strings = tmp.split(" ");
            code = strings[0];
            contractNo = strings[strings.length-1];
            return new Payment(0l, DEP, e.date, e.detail, e.credit, e.balance, null, Optional.ofNullable(e.code).orElseGet(() -> lookupCode("ASX:"+code.toUpperCase())), contractNo);
        } else {
            return new Payment(0, PAYMENT, e.date, e.detail, e.credit, e.balance, null, null, null);
        }
    }

    private static Payment parseOtherString(Payment.PaymentFactory e) {
        // DIRECT DEBIT DISHONOURED 012384
        return new Payment(0l, OTHER, e.date, e.detail, e.credit, e.balance, null, null, null);
    }

    private static Payment parseDividendString(Payment.PaymentFactory e) {
        if(e.detail.toUpperCase().startsWith("DEPOSIT DIVIDEND")) {
            // DEPOSIT DIVIDEND MQG ITM DIV 001269915175
            String tmp = e.detail.substring("DEPOSIT DIVIDEND ".length());
            String[] strings = tmp.split(" ");
            String code = strings[0];
            String contractNo = strings[strings.length-1];
            return new Payment(0l, DIV, e.date, e.detail, e.credit, e.balance, null, Optional.ofNullable(e.code).orElseGet(() -> lookupCode("ASX:"+code.toUpperCase())), contractNo);
        } else {
            // Deposit-Debenture/Note Interest Vgb Payment  Jan15/00800426
            String[] strings = Arrays.stream(e.detail.replace("/", " ").split(" ")).filter(not(""::equals)).toArray(String[]::new);
            String code = strings[3];
            String contractNo = strings[strings.length-1];
            return new Payment(0l, DIV, e.date, e.detail, e.credit, e.balance, null, Optional.ofNullable(e.code).orElseGet(() -> lookupCode("ASX:"+code.toUpperCase())), contractNo);
        }
    }

    public static Payment parsePaymentString(Payment.PaymentFactory e) {
        Payment result;
        // WITHDRAWAL ONLINE 1934011 TFR Westpac Cho renovation fund
        if(e.detail.startsWith("WITHDRAWAL ONLINE")) {
            result = new Payment(0, TxnType.PAYMENT, e.date,e.detail, e.debit, e.balance, null, null, null);
        } else {
            // PAYMENT BY AUTHORITY TO WESTPAC SECURITI B DUR 42855945-0
            String tmp = e.detail.substring(PREAMBLE_LENGTH);
            if(tmp.startsWith("Westpac Securitie")) {
                // some other payment type
                result = new Payment(0, TxnType.PAYMENT, e.date,e.detail, e.debit, e.balance, null, null, null);
            } else {
                String[] strings = Arrays.stream(tmp.split(" ")).filter(not(""::equals)).toArray(String[]::new);
                Side side = Side.fromString(strings[0]);
                String code = strings[1];
                String contractNo = strings[2].split("\\-")[0];
                result = new Payment(0l, PAYMENT, e.date, e.detail, e.debit, e.balance,side, Optional.ofNullable(e.code).orElseGet(() -> lookupCode("ASX:"+code.toUpperCase())), contractNo);
            }
        }
        return result;
    }

    public boolean isCredit() {
        return !isDebit();
    }

    public boolean isDebit() {
        return this == PAYMENT || this == OTHER;
    }

    public boolean isPayment() {
        return this == PAYMENT;
    }

    @Override
    public BigDecimal apply(BigDecimal bd) {
        return isCredit() ? bd : bd.multiply(MINUS_ONE);
    }
}
@Slf4j
class Constants {

    public static Map<String, String> codes;

    static {
        try {
            codes = new ObjectMapper().readValue(Codes.CODES, Map.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static String lookup(String key) {
        return codes.get(key);
    }
}

class Codes {
    static String CODES = "{\n" +
            "  \"ASX:29M\": \"ASX:29M\",\n" +
            "  \"ASX:360\": \"ASX:360\",\n" +
            "  \"ASX:3DP\": \"ASX:3DP\",\n" +
            "  \"ASX:3PL\": \"ASX:3PL\",\n" +
            "  \"ASX:4DS\": \"ASX:4DS\",\n" +
            "  \"ASX:4DX\": \"ASX:4DX\",\n" +
            "  \"ASX:5EA\": \"ASX:5EA\",\n" +
            "  \"ASX:A11\": \"ASX:A11\",\n" +
            "  \"ASX:A1M\": \"ASX:A1M\",\n" +
            "  \"ASX:A1N\": \"ASX:A1N\",\n" +
            "  \"ASX:A2M\": \"ASX:A2M\",\n" +
            "  \"ASX:A4N\": \"ASX:A4N\",\n" +
            "  \"ASX:AAC\": \"ASX:AAC\",\n" +
            "  \"ASX:AAI\": \"ASX:AAI\",\n" +
            "  \"ASX:ABA\": \"ASX:ABA\",\n" +
            "  \"ASX:ABB\": \"ASX:ABB\",\n" +
            "  \"ASX:ABC\": \"ASX:ABC\",\n" +
            "  \"ASX:ABG\": \"ASX:ABG\",\n" +
            "  \"ASX:ABP\": \"ASX:ABP\",\n" +
            "  \"ASX:ABV\": \"ASX:ABV\",\n" +
            "  \"ASX:ABY\": \"ASX:ABY\",\n" +
            "  \"ASX:ACE\": \"ASX:ACE\",\n" +
            "  \"ASX:ACF\": \"ASX:ACF\",\n" +
            "  \"ASX:ACL\": \"ASX:ACL\",\n" +
            "  \"ASX:ACU\": \"ASX:ACU\",\n" +
            "  \"ASX:ACW\": \"ASX:ACW\",\n" +
            "  \"ASX:AD8\": \"ASX:AD8\",\n" +
            "  \"ASX:ADA\": \"ASX:ADA\",\n" +
            "  \"ASX:ADACEL\": \"ASX:ADA\",\n" +
            "  \"ASX:ADH\": \"ASX:ADH\",\n" +
            "  \"ASX:ADT\": \"ASX:ADT\",\n" +
            "  \"ASX:AEE\": \"ASX:AEE\",\n" +
            "  \"ASX:AEF\": \"ASX:AEF\",\n" +
            "  \"ASX:AEV\": \"ASX:AEV\",\n" +
            "  \"ASX:AFG\": \"ASX:AFG\",\n" +
            "  \"ASX:AFI\": \"ASX:AFI\",\n" +
            "  \"ASX:AFP\": \"ASX:AFP\",\n" +
            "  \"ASX:AGE\": \"ASX:AGE\",\n" +
            "  \"ASX:AGG\": \"ASX:AGG\",\n" +
            "  \"ASX:AGI\": \"ASX:AGI\",\n" +
            "  \"ASX:AINSWORTH\": \"ASX:AGI\",\n" +
            "  \"ASX:AGL\": \"ASX:AGL\",\n" +
            "  \"ASX:AGY\": \"ASX:AGY\",\n" +
            "  \"ASX:AHC\": \"ASX:AHC\",\n" +
            "  \"ASX:AHL\": \"ASX:AHL\",\n" +
            "  \"ASX:AHX\": \"ASX:AHX\",\n" +
            "  \"ASX:AIA\": \"ASX:AIA\",\n" +
            "  \"ASX:AIM\": \"ASX:AIM\",\n" +
            "  \"ASX:AIS\": \"ASX:AIS\",\n" +
            "  \"ASX:AIZ\": \"ASX:AIZ\",\n" +
            "  \"ASX:AKE\": \"ASX:AKE\",\n" +
            "  \"ASX:AKP\": \"ASX:AKP\",\n" +
            "  \"ASX:ALC\": \"ASX:ALC\",\n" +
            "  \"ASX:ALD\": \"ASX:ALD\",\n" +
            "  \"ASX:ALG\": \"ASX:ALG\",\n" +
            "  \"ASX:ALI\": \"ASX:ALI\",\n" +
            "  \"ASX:ALK\": \"ASX:ALK\",\n" +
            "  \"ASX:ALL\": \"ASX:ALL\",\n" +
            "  \"ASX:ALQ\": \"ASX:ALQ\",\n" +
            "  \"ASX:ALU\": \"ASX:ALU\",\n" +
            "  \"ASX:ALTIUM\": \"ASX:ALU\",\n" +
            "  \"ASX:ALX\": \"ASX:ALX\",\n" +
            "  \"ASX:AMA\": \"ASX:AMA\",\n" +
            "  \"ASX:AMC\": \"ASX:AMC\",\n" +
            "  \"ASX:AMH\": \"ASX:AMH\",\n" +
            "  \"ASX:AMI\": \"ASX:AMI\",\n" +
            "  \"ASX:L\": \"ASX:AMI\",\n" +
            "  \"ASX:AMP\": \"ASX:AMP\",\n" +
            "  \"ASX:AND\": \"ASX:AND\",\n" +
            "  \"ASX:ANG\": \"ASX:ANG\",\n" +
            "  \"ASX:ANN\": \"ASX:ANN\",\n" +
            "  \"ASX:ANO\": \"ASX:ANO\",\n" +
            "  \"ASX:ANZ\": \"ASX:ANZ\",\n" +
            "  \"ASX:AOF\": \"ASX:AOF\",\n" +
            "  \"ASX:AOV\": \"ASX:AOV\",\n" +
            "  \"ASX:APA\": \"ASX:APA\",\n" +
            "  \"ASX:APE\": \"ASX:APE\",\n" +
            "  \"ASX:APM\": \"ASX:APM\",\n" +
            "  \"ASX:APX\": \"ASX:APX\",\n" +
            "  \"ASX:APZ\": \"ASX:APZ\",\n" +
            "  \"ASX:AQC\": \"ASX:AQC\",\n" +
            "  \"ASX:AQZ\": \"ASX:AQZ\",\n" +
            "  \"ASX:ARA\": \"ASX:ARA\",\n" +
            "  \"ASX:ARB\": \"ASX:ARB\",\n" +
            "  \"ASX:ARF\": \"ASX:ARF\",\n" +
            "  \"ASX:ARG\": \"ASX:ARG\",\n" +
            "  \"ASX:ART\": \"ASX:ART\",\n" +
            "  \"ASX:ARU\": \"ASX:ARU\",\n" +
            "  \"ASX:ARX\": \"ASX:ARX\",\n" +
            "  \"ASX:ASB\": \"ASX:ASB\",\n" +
            "  \"ASX:ASG\": \"ASX:ASG\",\n" +
            "  \"ASX:ASK\": \"ASX:ASK\",\n" +
            "  \"ASX:ASM\": \"ASX:ASM\",\n" +
            "  \"ASX:ASN\": \"ASX:ASN\",\n" +
            "  \"ASX:ASO\": \"ASX:ASO\",\n" +
            "  \"ASX:ASX\": \"ASX:ASX\",\n" +
            "  \"ASX:ATA\": \"ASX:ATA\",\n" +
            "  \"ASX:ATM\": \"ASX:ATM\",\n" +
            "  \"ASX:ATP\": \"ASX:ATP\",\n" +
            "  \"ASX:ATR\": \"ASX:ATR\",\n" +
            "  \"ASX:ATS\": \"ASX:ATS\",\n" +
            "  \"ASX:AUB\": \"ASX:AUB\",\n" +
            "  \"ASX:AUC\": \"ASX:AUC\",\n" +
            "  \"ASX:AUI\": \"ASX:AUI\",\n" +
            "  \"ASX:AUT\": \"ASX:AUT\",\n" +
            "  \"ASX:AVA\": \"ASX:AVA\",\n" +
            "  \"ASX:AVG\": \"ASX:AVG\",\n" +
            "  \"ASX:AVH\": \"ASX:AVH\",\n" +
            "  \"ASX:AVJ\": \"ASX:AVJ\",\n" +
            "  \"ASX:AVR\": \"ASX:AVR\",\n" +
            "  \"ASX:AVZ\": \"ASX:AVZ\",\n" +
            "  \"ASX:AWC\": \"ASX:AWC\",\n" +
            "  \"ASX:AX1\": \"ASX:AX1\",\n" +
            "  \"ASX:ACCENT\": \"ASX:AX1\",\n" +
            "  \"ASX:AZJ\": \"ASX:AZJ\",\n" +
            "  \"ASX:AZL\": \"ASX:AZL\",\n" +
            "  \"ASX:AZS\": \"ASX:AZS\",\n" +
            "  \"ASX:BAP\": \"ASX:BAP\",\n" +
            "  \"ASX:BBN\": \"ASX:BBN\",\n" +
            "  \"ASX:BC8\": \"ASX:BC8\",\n" +
            "  \"ASX:BCB\": \"ASX:BCB\",\n" +
            "  \"ASX:BCC\": \"ASX:BCC\",\n" +
            "  \"ASX:BCI\": \"ASX:BCI\",\n" +
            "  \"ASX:BEN\": \"ASX:BEN\",\n" +
            "  \"ASX:BEO\": \"ASX:BEO\",\n" +
            "  \"ASX:BET\": \"ASX:BET\",\n" +
            "  \"ASX:BEZ\": \"ASX:BEZ\",\n" +
            "  \"ASX:BFG\": \"ASX:BFG\",\n" +
            "  \"ASX:BFL\": \"ASX:BFL\",\n" +
            "  \"ASX:BGA\": \"ASX:BGA\",\n" +
            "  \"ASX:LTD\": \"ASX:BGA\",\n" +
            "  \"ASX:BGD\": \"ASX:BGD\",\n" +
            "  \"ASX:BGL\": \"ASX:BGL\",\n" +
            "  \"ASX:SLCSOA\": \"ASX:BGL\",\n" +
            "  \"ASX:BGP\": \"ASX:BGP\",\n" +
            "  \"ASX:BHP\": \"ASX:BHP\",\n" +
            "  \"ASX:BIO\": \"ASX:BIO\",\n" +
            "  \"ASX:BIS\": \"ASX:BIS\",\n" +
            "  \"ASX:BIT\": \"ASX:BIT\",\n" +
            "  \"ASX:BKI\": \"ASX:BKI\",\n" +
            "  \"ASX:BKL\": \"ASX:BKL\",\n" +
            "  \"ASX:BLACKMORES\": \"ASX:BKL\",\n" +
            "  \"ASX:BKT\": \"ASX:BKT\",\n" +
            "  \"ASX:BKW\": \"ASX:BKW\",\n" +
            "  \"ASX:BLD\": \"ASX:BLD\",\n" +
            "  \"ASX:BLU\": \"ASX:BLU\",\n" +
            "  \"ASX:BLX\": \"ASX:BLX\",\n" +
            "  \"ASX:BLY\": \"ASX:BLY\",\n" +
            "  \"ASX:BML\": \"ASX:BML\",\n" +
            "  \"ASX:BMN\": \"ASX:BMN\",\n" +
            "  \"ASX:BNR\": \"ASX:BNR\",\n" +
            "  \"ASX:BOC\": \"ASX:BOC\",\n" +
            "  \"ASX:BOE\": \"ASX:BOE\",\n" +
            "  \"ASX:BOQ\": \"ASX:BOQ\",\n" +
            "  \"ASX:BOT\": \"ASX:BOT\",\n" +
            "  \"ASX:BPH\": \"ASX:BPH\",\n" +
            "  \"ASX:BPT\": \"ASX:BPT\",\n" +
            "  \"ASX:BRB\": \"ASX:BRB\",\n" +
            "  \"ASX:BRE\": \"ASX:BRE\",\n" +
            "  \"ASX:BRG\": \"ASX:BRG\",\n" +
            "  \"ASX:BRI\": \"ASX:BRI\",\n" +
            "  \"ASX:BRK\": \"ASX:BRK\",\n" +
            "  \"ASX:BRL\": \"ASX:BRL\",\n" +
            "  \"ASX:BRN\": \"ASX:BRN\",\n" +
            "  \"ASX:BSE\": \"ASX:BSE\",\n" +
            "  \"ASX:BSL\": \"ASX:BSL\",\n" +
            "  \"ASX:BST\": \"ASX:BST\",\n" +
            "  \"ASX:BSX\": \"ASX:BSX\",\n" +
            "  \"ASX:BTH\": \"ASX:BTH\",\n" +
            "  \"ASX:BTR\": \"ASX:BTR\",\n" +
            "  \"ASX:BUB\": \"ASX:BUB\",\n" +
            "  \"ASX:BVS\": \"ASX:BVS\",\n" +
            "  \"ASX:BWP\": \"ASX:BWP\",\n" +
            "  \"ASX:BWX\": \"ASX:BWX\",\n" +
            "  \"ASX:PREM\": \"ASX:BWX\",\n" +
            "  \"ASX:BXB\": \"ASX:BXB\",\n" +
            "  \"ASX:BXN\": \"ASX:BXN\",\n" +
            "  \"ASX:C79\": \"ASX:C79\",\n" +
            "  \"ASX:CAF\": \"ASX:CAF\",\n" +
            "  \"ASX:CAI\": \"ASX:CAI\",\n" +
            "  \"ASX:CAJ\": \"ASX:CAJ\",\n" +
            "  \"ASX:CAPITOL\": \"ASX:CAJ\",\n" +
            "  \"ASX:CAR\": \"ASX:CAR\",\n" +
            "  \"ASX:CAT\": \"ASX:CAT\",\n" +
            "  \"ASX:CBA\": \"ASX:CBA\",\n" +
            "  \"ASX:CBO\": \"ASX:CBO\",\n" +
            "  \"ASX:CBR\": \"ASX:CBR\",\n" +
            "  \"ASX:CCL\": \"ASX:CCL\",\n" +
            "  \"ASX:CCP\": \"ASX:CCP\",\n" +
            "  \"ASX:CCR\": \"ASX:CCR\",\n" +
            "  \"ASX:CCV\": \"ASX:CCV\",\n" +
            "  \"ASX:CCX\": \"ASX:CCX\",\n" +
            "  \"ASX:CDA\": \"ASX:CDA\",\n" +
            "  \"ASX:CDM\": \"ASX:CDM\",\n" +
            "  \"ASX:CDP\": \"ASX:CDP\",\n" +
            "  \"ASX:CEL\": \"ASX:CEL\",\n" +
            "  \"ASX:CEN\": \"ASX:CEN\",\n" +
            "  \"ASX:CGC\": \"ASX:CGC\",\n" +
            "  \"ASX:CGF\": \"ASX:CGF\",\n" +
            "  \"ASX:CHALLENGER\": \"ASX:CGF\",\n" +
            "  \"ASX:CGS\": \"ASX:CGS\",\n" +
            "  \"ASX:CHC\": \"ASX:CHC\",\n" +
            "  \"ASX:CHL\": \"ASX:CHL\",\n" +
            "  \"ASX:CHN\": \"ASX:CHN\",\n" +
            "  \"ASX:CHR\": \"ASX:CHR\",\n" +
            "  \"ASX:CHW\": \"ASX:CHW\",\n" +
            "  \"ASX:CIA\": \"ASX:CIA\",\n" +
            "  \"ASX:CIN\": \"ASX:CIN\",\n" +
            "  \"ASX:CIP\": \"ASX:CIP\",\n" +
            "  \"ASX:CKA\": \"ASX:CKA\",\n" +
            "  \"ASX:CKF\": \"ASX:CKF\",\n" +
            "  \"ASX:CLG\": \"ASX:CLG\",\n" +
            "  \"ASX:CLV\": \"ASX:CLV\",\n" +
            "  \"ASX:CLW\": \"ASX:CLW\",\n" +
            "  \"ASX:CLX\": \"ASX:CLX\",\n" +
            "  \"ASX:CMM\": \"ASX:CMM\",\n" +
            "  \"ASX:CMW\": \"ASX:CMW\",\n" +
            "  \"ASX:CNB\": \"ASX:CNB\",\n" +
            "  \"ASX:CNI\": \"ASX:CNI\",\n" +
            "  \"ASX:CNU\": \"ASX:CNU\",\n" +
            "  \"ASX:CNW\": \"ASX:CNW\",\n" +
            "  \"ASX:COB\": \"ASX:COB\",\n" +
            "  \"ASX:COE\": \"ASX:COE\",\n" +
            "  \"ASX:COF\": \"ASX:COF\",\n" +
            "  \"ASX:COG\": \"ASX:COG\",\n" +
            "  \"ASX:COH\": \"ASX:COH\",\n" +
            "  \"ASX:COI\": \"ASX:COI\",\n" +
            "  \"ASX:COL\": \"ASX:COL\",\n" +
            "  \"ASX:COS\": \"ASX:COS\",\n" +
            "  \"ASX:CPU\": \"ASX:CPU\",\n" +
            "  \"ASX:CPV\": \"ASX:CPV\",\n" +
            "  \"ASX:CQE\": \"ASX:CQE\",\n" +
            "  \"ASX:CQR\": \"ASX:CQR\",\n" +
            "  \"ASX:CRD\": \"ASX:CRD\",\n" +
            "  \"ASX:CRN\": \"ASX:CRN\",\n" +
            "  \"ASX:CSL\": \"ASX:CSL\",\n" +
            "  \"ASX:CSR\": \"ASX:CSR\",\n" +
            "  \"ASX:CSS\": \"ASX:CSS\",\n" +
            "  \"ASX:CTD\": \"ASX:CTD\",\n" +
            "  \"ASX:CTM\": \"ASX:CTM\",\n" +
            "  \"ASX:CTT\": \"ASX:CTT\",\n" +
            "  \"ASX:CU6\": \"ASX:CU6\",\n" +
            "  \"ASX:CUE\": \"ASX:CUE\",\n" +
            "  \"ASX:CUV\": \"ASX:CUV\",\n" +
            "  \"ASX:CVL\": \"ASX:CVL\",\n" +
            "  \"ASX:CVN\": \"ASX:CVN\",\n" +
            "  \"ASX:CVV\": \"ASX:CVV\",\n" +
            "  \"ASX:CVW\": \"ASX:CVW\",\n" +
            "  \"ASX:CWP\": \"ASX:CWP\",\n" +
            "  \"ASX:CWY\": \"ASX:CWY\",\n" +
            "  \"ASX:CXL\": \"ASX:CXL\",\n" +
            "  \"ASX:CXM\": \"ASX:CXM\",\n" +
            "  \"ASX:CXO\": \"ASX:CXO\",\n" +
            "  \"ASX:CXZ\": \"ASX:CXZ\",\n" +
            "  \"ASX:CYC\": \"ASX:CYC\",\n" +
            "  \"ASX:CYG\": \"ASX:CYG\",\n" +
            "  \"ASX:CYL\": \"ASX:CYL\",\n" +
            "  \"ASX:CYM\": \"ASX:CYM\",\n" +
            "  \"ASX:DBI\": \"ASX:DBI\",\n" +
            "  \"ASX:DCC\": \"ASX:DCC\",\n" +
            "  \"ASX:DCN\": \"ASX:DCN\",\n" +
            "  \"ASX:DDH\": \"ASX:DDH\",\n" +
            "  \"ASX:DDR\": \"ASX:DDR\",\n" +
            "  \"ASX:DEG\": \"ASX:DEG\",\n" +
            "  \"ASX:DGL\": \"ASX:DGL\",\n" +
            "  \"ASX:DGT\": \"ASX:DGT\",\n" +
            "  \"ASX:DHG\": \"ASX:DHG\",\n" +
            "  \"ASX:DJW\": \"ASX:DJW\",\n" +
            "  \"ASX:DLI\": \"ASX:DLI\",\n" +
            "  \"ASX:DMP\": \"ASX:DMP\",\n" +
            "  \"ASX:DNA\": \"ASX:DNA\",\n" +
            "  \"ASX:DNK\": \"ASX:DNK\",\n" +
            "  \"ASX:DOC\": \"ASX:DOC\",\n" +
            "  \"ASX:DOW\": \"ASX:DOW\",\n" +
            "  \"ASX:DRE\": \"ASX:DRE\",\n" +
            "  \"ASX:DRO\": \"ASX:DRO\",\n" +
            "  \"ASX:DRR\": \"ASX:DRR\",\n" +
            "  \"ASX:DSE\": \"ASX:DSE\",\n" +
            "  \"ASX:DSK\": \"ASX:DSK\",\n" +
            "  \"ASX:DTL\": \"ASX:DTL\",\n" +
            "  \"ASX:DUG\": \"ASX:DUG\",\n" +
            "  \"ASX:DUI\": \"ASX:DUI\",\n" +
            "  \"ASX:DUR\": \"ASX:DUR\",\n" +
            "  \"ASX:DVP\": \"ASX:DVP\",\n" +
            "  \"ASX:DXB\": \"ASX:DXB\",\n" +
            "  \"ASX:DXC\": \"ASX:DXC\",\n" +
            "  \"ASX:DXI\": \"ASX:DXI\",\n" +
            "  \"ASX:DXS\": \"ASX:DXS\",\n" +
            "  \"ASX:DYL\": \"ASX:DYL\",\n" +
            "  \"ASX:E25\": \"ASX:E25\",\n" +
            "  \"ASX:EBO\": \"ASX:EBO\",\n" +
            "  \"ASX:EBR\": \"ASX:EBR\",\n" +
            "  \"ASX:ECF\": \"ASX:ECF\",\n" +
            "  \"ASX:EDV\": \"ASX:EDV\",\n" +
            "  \"ASX:EGG\": \"ASX:EGG\",\n" +
            "  \"ASX:EGH\": \"ASX:EGH\",\n" +
            "  \"ASX:EGL\": \"ASX:EGL\",\n" +
            "  \"ASX:EGR\": \"ASX:EGR\",\n" +
            "  \"ASX:EHE\": \"ASX:EHE\",\n" +
            "  \"ASX:EHL\": \"ASX:EHL\",\n" +
            "  \"ASX:EL8\": \"ASX:EL8\",\n" +
            "  \"ASX:ELD\": \"ASX:ELD\",\n" +
            "  \"ASX:ELT\": \"ASX:ELT\",\n" +
            "  \"ASX:EMB\": \"ASX:EMB\",\n" +
            "  \"ASX:EMH\": \"ASX:EMH\",\n" +
            "  \"ASX:EML\": \"ASX:EML\",\n" +
            "  \"ASX:EMR\": \"ASX:EMR\",\n" +
            "  \"ASX:EMV\": \"ASX:EMV\",\n" +
            "  \"ASX:ENN\": \"ASX:ENN\",\n" +
            "  \"ASX:ENV\": \"ASX:ENV\",\n" +
            "  \"ASX:EOL\": \"ASX:EOL\",\n" +
            "  \"ASX:EOS\": \"ASX:EOS\",\n" +
            "  \"ASX:EQR\": \"ASX:EQR\",\n" +
            "  \"ASX:EQT\": \"ASX:EQT\",\n" +
            "  \"ASX:ERA\": \"ASX:ERA\",\n" +
            "  \"ASX:ERD\": \"ASX:ERD\",\n" +
            "  \"ASX:ESK\": \"ASX:ESK\",\n" +
            "  \"ASX:EUR\": \"ASX:EUR\",\n" +
            "  \"ASX:EVN\": \"ASX:EVN\",\n" +
            "  \"ASX:EVO\": \"ASX:EVO\",\n" +
            "  \"ASX:EVS\": \"ASX:EVS\",\n" +
            "  \"ASX:EVT\": \"ASX:EVT\",\n" +
            "  \"ASX:EXP\": \"ASX:EXP\",\n" +
            "  \"ASX:EXR\": \"ASX:EXR\",\n" +
            "  \"ASX:EZZ\": \"ASX:EZZ\",\n" +
            "  \"ASX:FBU\": \"ASX:FBU\",\n" +
            "  \"ASX:FCL\": \"ASX:FCL\",\n" +
            "  \"ASX:FCT\": \"ASX:FCT\",\n" +
            "  \"ASX:FDV\": \"ASX:FDV\",\n" +
            "  \"ASX:FFM\": \"ASX:FFM\",\n" +
            "  \"ASX:FGG\": \"ASX:FGG\",\n" +
            "  \"ASX:FGX\": \"ASX:FGX\",\n" +
            "  \"ASX:FID\": \"ASX:FID\",\n" +
            "  \"ASX:FIG\": \"ASX:FIG\",\n" +
            "  \"ASX:CAP20\": \"ASX:FIG\",\n" +
            "  \"ASX:CAP21\": \"ASX:FIG\",\n" +
            "  \"ASX:FL1\": \"ASX:FL1\",\n" +
            "  \"ASX:FLC\": \"ASX:FLC\",\n" +
            "  \"ASX:FLT\": \"ASX:FLT\",\n" +
            "  \"ASX:FMG\": \"ASX:FMG\",\n" +
            "  \"ASX:FND\": \"ASX:FND\",\n" +
            "  \"ASX:FPH\": \"ASX:FPH\",\n" +
            "  \"ASX:FPR\": \"ASX:FPR\",\n" +
            "  \"ASX:FSF\": \"ASX:FSF\",\n" +
            "  \"ASX:FSG\": \"ASX:FSG\",\n" +
            "  \"ASX:FWD\": \"ASX:FWD\",\n" +
            "  \"ASX:FZO\": \"ASX:FZO\",\n" +
            "  \"ASX:G6M\": \"ASX:G6M\",\n" +
            "  \"ASX:GAL\": \"ASX:GAL\",\n" +
            "  \"ASX:GBR\": \"ASX:GBR\",\n" +
            "  \"ASX:GCI\": \"ASX:GCI\",\n" +
            "  \"ASX:GCY\": \"ASX:GCY\",\n" +
            "  \"ASX:GDC\": \"ASX:GDC\",\n" +
            "  \"ASX:GDF\": \"ASX:GDF\",\n" +
            "  \"ASX:GDG\": \"ASX:GDG\",\n" +
            "  \"ASX:GDI\": \"ASX:GDI\",\n" +
            "  \"ASX:GEM\": \"ASX:GEM\",\n" +
            "  \"ASX:G8\": \"ASX:GEM\",\n" +
            "  \"ASX:LIM\": \"ASX:GEM\",\n" +
            "  \"ASX:GEN\": \"ASX:GEN\",\n" +
            "  \"ASX:GHY\": \"ASX:GHY\",\n" +
            "  \"ASX:GL1\": \"ASX:GL1\",\n" +
            "  \"ASX:GLB\": \"ASX:GLB\",\n" +
            "  \"ASX:GLN\": \"ASX:GLN\",\n" +
            "  \"ASX:GMD\": \"ASX:GMD\",\n" +
            "  \"ASX:GMG\": \"ASX:GMG\",\n" +
            "  \"ASX:GNC\": \"ASX:GNC\",\n" +
            "  \"ASX:GNE\": \"ASX:GNE\",\n" +
            "  \"ASX:GNG\": \"ASX:GNG\",\n" +
            "  \"ASX:GNP\": \"ASX:GNP\",\n" +
            "  \"ASX:GNX\": \"ASX:GNX\",\n" +
            "  \"ASX:GOR\": \"ASX:GOR\",\n" +
            "  \"ASX:GOZ\": \"ASX:GOZ\",\n" +
            "  \"ASX:GPT\": \"ASX:GPT\",\n" +
            "  \"ASX:GQG\": \"ASX:GQG\",\n" +
            "  \"ASX:GRE\": \"ASX:GRE\",\n" +
            "  \"ASX:GRR\": \"ASX:GRR\",\n" +
            "  \"ASX:GSS\": \"ASX:GSS\",\n" +
            "  \"ASX:GT1\": \"ASX:GT1\",\n" +
            "  \"ASX:GTH\": \"ASX:GTH\",\n" +
            "  \"ASX:GTI\": \"ASX:GTI\",\n" +
            "  \"ASX:GTK\": \"ASX:GTK\",\n" +
            "  \"ASX:GTN\": \"ASX:GTN\",\n" +
            "  \"ASX:GUD\": \"ASX:GUD\",\n" +
            "  \"ASX:GWA\": \"ASX:GWA\",\n" +
            "  \"ASX:GYG\": \"ASX:GYG\",\n" +
            "  \"ASX:HAS\": \"ASX:HAS\",\n" +
            "  \"ASX:HCH\": \"ASX:HCH\",\n" +
            "  \"ASX:HCL\": \"ASX:HCL\",\n" +
            "  \"ASX:HCW\": \"ASX:HCW\",\n" +
            "  \"ASX:HDN\": \"ASX:HDN\",\n" +
            "  \"ASX:HFR\": \"ASX:HFR\",\n" +
            "  \"ASX:HGH\": \"ASX:HGH\",\n" +
            "  \"ASX:HHR\": \"ASX:HHR\",\n" +
            "  \"ASX:HIL\": \"ASX:HIL\",\n" +
            "  \"ASX:HILLS\": \"ASX:HIL\",\n" +
            "  \"ASX:HIT\": \"ASX:HIT\",\n" +
            "  \"ASX:HLA\": \"ASX:HLA\",\n" +
            "  \"ASX:HLI\": \"ASX:HLI\",\n" +
            "  \"ASX:HLO\": \"ASX:HLO\",\n" +
            "  \"ASX:HLS\": \"ASX:HLS\",\n" +
            "  \"ASX:HM1\": \"ASX:HM1\",\n" +
            "  \"ASX:HMC\": \"ASX:HMC\",\n" +
            "  \"ASX:HPG\": \"ASX:HPG\",\n" +
            "  \"ASX:HPI\": \"ASX:HPI\",\n" +
            "  \"ASX:HSN\": \"ASX:HSN\",\n" +
            "  \"ASX:HANSEN\": \"ASX:HSN\",\n" +
            "  \"ASX:HT1\": \"ASX:HT1\",\n" +
            "  \"ASX:HTA\": \"ASX:HTA\",\n" +
            "  \"ASX:HUB\": \"ASX:HUB\",\n" +
            "  \"ASX:HUM\": \"ASX:HUM\",\n" +
            "  \"ASX:HVN\": \"ASX:HVN\",\n" +
            "  \"ASX:HZN\": \"ASX:HZN\",\n" +
            "  \"ASX:IAG\": \"ASX:IAG\",\n" +
            "  \"ASX:IDX\": \"ASX:IDX\",\n" +
            "  \"ASX:IEL\": \"ASX:IEL\",\n" +
            "  \"ASX:IFL\": \"ASX:IFL\",\n" +
            "  \"ASX:IOOF\": \"ASX:IFL\",\n" +
            "  \"ASX:IFM\": \"ASX:IFM\",\n" +
            "  \"ASX:INFOMEDIA\": \"ASX:IFM\",\n" +
            "  \"ASX:IFT\": \"ASX:IFT\",\n" +
            "  \"ASX:IGL\": \"ASX:IGL\",\n" +
            "  \"ASX:IGO\": \"ASX:IGO\",\n" +
            "  \"ASX:IHL\": \"ASX:IHL\",\n" +
            "  \"ASX:IIQ\": \"ASX:IIQ\",\n" +
            "  \"ASX:IJP\": \"ASX:IJP\",\n" +
            "  \"ASX:IKE\": \"ASX:IKE\",\n" +
            "  \"ASX:ILU\": \"ASX:ILU\",\n" +
            "  \"ASX:IMB\": \"ASX:IMB\",\n" +
            "  \"ASX:IMD\": \"ASX:IMD\",\n" +
            "  \"ASX:IMM\": \"ASX:IMM\",\n" +
            "  \"ASX:IMU\": \"ASX:IMU\",\n" +
            "  \"ASX:INA\": \"ASX:INA\",\n" +
            "  \"ASX:IND\": \"ASX:IND\",\n" +
            "  \"ASX:INF\": \"ASX:INF\",\n" +
            "  \"ASX:ING\": \"ASX:ING\",\n" +
            "  \"ASX:INR\": \"ASX:INR\",\n" +
            "  \"ASX:IOD\": \"ASX:IOD\",\n" +
            "  \"ASX:IPD\": \"ASX:IPD\",\n" +
            "  \"ASX:IPG\": \"ASX:IPG\",\n" +
            "  \"ASX:IPH\": \"ASX:IPH\",\n" +
            "  \"ASX:IPL\": \"ASX:IPL\",\n" +
            "  \"ASX:IPP\": \"ASX:IPP\",\n" +
            "  \"ASX:IPROPERTY\": \"ASX:IPP\",\n" +
            "  \"ASX:IRE\": \"ASX:IRE\",\n" +
            "  \"ASX:IRI\": \"ASX:IRI\",\n" +
            "  \"ASX:INTERGRATED\": \"ASX:IRI\",\n" +
            "  \"ASX:INTEGRATED\": \"ASX:IRI\",\n" +
            "  \"ASX:ISU\": \"ASX:ISU\",\n" +
            "  \"ASX:ISELECT\": \"ASX:ISU\",\n" +
            "  \"ASX:ITQ\": \"ASX:ITQ\",\n" +
            "  \"ASX:SCH16\": \"ASX:ITQ\",\n" +
            "  \"ASX:WARRINGAH\": \"ASX:ITQ\",\n" +
            "  \"ASX:IVC\": \"ASX:IVC\",\n" +
            "  \"ASX:IVZ\": \"ASX:IVZ\",\n" +
            "  \"ASX:IXR\": \"ASX:IXR\",\n" +
            "  \"ASX:JAN\": \"ASX:JAN\",\n" +
            "  \"ASX:JBH\": \"ASX:JBH\",\n" +
            "  \"ASX:16CAP\": \"ASX:JBH\",\n" +
            "  \"ASX:JBY\": \"ASX:JBY\",\n" +
            "  \"ASX:JDO\": \"ASX:JDO\",\n" +
            "  \"ASX:JHG\": \"ASX:JHG\",\n" +
            "  \"ASX:JHX\": \"ASX:JHX\",\n" +
            "  \"ASX:JIN\": \"ASX:JIN\",\n" +
            "  \"ASX:JUMBO\": \"ASX:JIN\",\n" +
            "  \"ASX:JLG\": \"ASX:JLG\",\n" +
            "  \"ASX:JMS\": \"ASX:JMS\",\n" +
            "  \"ASX:JRV\": \"ASX:JRV\",\n" +
            "  \"ASX:KAR\": \"ASX:KAR\",\n" +
            "  \"ASX:KCN\": \"ASX:KCN\",\n" +
            "  \"ASX:KED\": \"ASX:KED\",\n" +
            "  \"ASX:KGN\": \"ASX:KGN\",\n" +
            "  \"ASX:KOGAN.COM\": \"ASX:KGN\",\n" +
            "  \"ASX:KKC\": \"ASX:KKC\",\n" +
            "  \"ASX:KLS\": \"ASX:KLS\",\n" +
            "  \"ASX:SEALINK\": \"ASX:KLS\",\n" +
            "  \"ASX:KMD\": \"ASX:KMD\",\n" +
            "  \"ASX:KME\": \"ASX:KME\",\n" +
            "  \"ASX:KOV\": \"ASX:KOV\",\n" +
            "  \"ASX:KPG\": \"ASX:KPG\",\n" +
            "  \"ASX:KSC\": \"ASX:KSC\",\n" +
            "  \"ASX:KSL\": \"ASX:KSL\",\n" +
            "  \"ASX:KTG\": \"ASX:KTG\",\n" +
            "  \"ASX:KYP\": \"ASX:KYP\",\n" +
            "  \"ASX:LAU\": \"ASX:LAU\",\n" +
            "  \"ASX:LBL\": \"ASX:LBL\",\n" +
            "  \"ASX:LEL\": \"ASX:LEL\",\n" +
            "  \"ASX:LFG\": \"ASX:LFG\",\n" +
            "  \"ASX:LFS\": \"ASX:LFS\",\n" +
            "  \"ASX:LGI\": \"ASX:LGI\",\n" +
            "  \"ASX:LGL\": \"ASX:LGL\",\n" +
            "  \"ASX:LGP\": \"ASX:LGP\",\n" +
            "  \"ASX:LIC\": \"ASX:LIC\",\n" +
            "  \"ASX:LIN\": \"ASX:LIN\",\n" +
            "  \"ASX:LKE\": \"ASX:LKE\",\n" +
            "  \"ASX:LLC\": \"ASX:LLC\",\n" +
            "  \"ASX:LLL\": \"ASX:LLL\",\n" +
            "  \"ASX:LM8\": \"ASX:LM8\",\n" +
            "  \"ASX:LME\": \"ASX:LME\",\n" +
            "  \"ASX:LMG\": \"ASX:LMG\",\n" +
            "  \"ASX:LNK\": \"ASX:LNK\",\n" +
            "  \"ASX:LNW\": \"ASX:LNW\",\n" +
            "  \"ASX:LOT\": \"ASX:LOT\",\n" +
            "  \"ASX:LOV\": \"ASX:LOV\",\n" +
            "  \"ASX:LPD\": \"ASX:LPD\",\n" +
            "  \"ASX:LPI\": \"ASX:LPI\",\n" +
            "  \"ASX:LRK\": \"ASX:LRK\",\n" +
            "  \"ASX:LRS\": \"ASX:LRS\",\n" +
            "  \"ASX:LRV\": \"ASX:LRV\",\n" +
            "  \"ASX:LSF\": \"ASX:LSF\",\n" +
            "  \"ASX:LTM\": \"ASX:LTM\",\n" +
            "  \"ASX:LTR\": \"ASX:LTR\",\n" +
            "  \"ASX:LYC\": \"ASX:LYC\",\n" +
            "  \"ASX:LYL\": \"ASX:LYL\",\n" +
            "  \"ASX:M4M\": \"ASX:M4M\",\n" +
            "  \"ASX:M7T\": \"ASX:M7T\",\n" +
            "  \"ASX:MAD\": \"ASX:MAD\",\n" +
            "  \"ASX:MAF\": \"ASX:MAF\",\n" +
            "  \"ASX:MAH\": \"ASX:MAH\",\n" +
            "  \"ASX:MAP\": \"ASX:MAP\",\n" +
            "  \"ASX:MAQ\": \"ASX:MAQ\",\n" +
            "  \"ASX:MAU\": \"ASX:MAU\",\n" +
            "  \"ASX:MAY\": \"ASX:MAY\",\n" +
            "  \"ASX:MCA\": \"ASX:MCA\",\n" +
            "  \"ASX:MCL\": \"ASX:MCL\",\n" +
            "  \"ASX:MCP\": \"ASX:MCP\",\n" +
            "  \"ASX:MCR\": \"ASX:MCR\",\n" +
            "  \"ASX:MCY\": \"ASX:MCY\",\n" +
            "  \"ASX:MDR\": \"ASX:MDR\",\n" +
            "  \"ASX:MEI\": \"ASX:MEI\",\n" +
            "  \"ASX:MEK\": \"ASX:MEK\",\n" +
            "  \"ASX:MEZ\": \"ASX:MEZ\",\n" +
            "  \"ASX:MFF\": \"ASX:MFF\",\n" +
            "  \"ASX:MFG\": \"ASX:MFG\",\n" +
            "  \"ASX:MAGELLAN\": \"ASX:MFG\",\n" +
            "  \"ASX:MGF\": \"ASX:MGF\",\n" +
            "  \"ASX:MGH\": \"ASX:MGH\",\n" +
            "  \"ASX:MGR\": \"ASX:MGR\",\n" +
            "  \"ASX:MGV\": \"ASX:MGV\",\n" +
            "  \"ASX:MGX\": \"ASX:MGX\",\n" +
            "  \"ASX:MHJ\": \"ASX:MHJ\",\n" +
            "  \"ASX:MIN\": \"ASX:MIN\",\n" +
            "  \"ASX:MIR\": \"ASX:MIR\",\n" +
            "  \"ASX:MKR\": \"ASX:MKR\",\n" +
            "  \"ASX:MLX\": \"ASX:MLX\",\n" +
            "  \"ASX:MME\": \"ASX:MME\",\n" +
            "  \"ASX:MMI\": \"ASX:MMI\",\n" +
            "  \"ASX:MMS\": \"ASX:MMS\",\n" +
            "  \"ASX:MND\": \"ASX:MND\",\n" +
            "  \"ASX:MNS\": \"ASX:MNS\",\n" +
            "  \"ASX:MOT\": \"ASX:MOT\",\n" +
            "  \"ASX:MOV\": \"ASX:MOV\",\n" +
            "  \"ASX:MOZ\": \"ASX:MOZ\",\n" +
            "  \"ASX:MP1\": \"ASX:MP1\",\n" +
            "  \"ASX:MPL\": \"ASX:MPL\",\n" +
            "  \"ASX:MQG\": \"ASX:MQG\",\n" +
            "  \"ASX:MRM\": \"ASX:MRM\",\n" +
            "  \"ASX:MSB\": \"ASX:MSB\",\n" +
            "  \"ASX:MTO\": \"ASX:MTO\",\n" +
            "  \"ASX:MTS\": \"ASX:MTS\",\n" +
            "  \"ASX:MVF\": \"ASX:MVF\",\n" +
            "  \"ASX:MVP\": \"ASX:MVP\",\n" +
            "  \"ASX:MXI\": \"ASX:MXI\",\n" +
            "  \"ASX:MXT\": \"ASX:MXT\",\n" +
            "  \"ASX:MYR\": \"ASX:MYR\",\n" +
            "  \"ASX:MYS\": \"ASX:MYS\",\n" +
            "  \"ASX:MYX\": \"ASX:MYX\",\n" +
            "  \"ASX:NAB\": \"ASX:NAB\",\n" +
            "  \"ASX:NAM\": \"ASX:NAM\",\n" +
            "  \"ASX:NAN\": \"ASX:NAN\",\n" +
            "  \"ASX:NBI\": \"ASX:NBI\",\n" +
            "  \"ASX:NCK\": \"ASX:NCK\",\n" +
            "  \"ASX:NCM\": \"ASX:NCM\",\n" +
            "  \"ASX:NCZ\": \"ASX:NCZ\",\n" +
            "  \"ASX:NEC\": \"ASX:NEC\",\n" +
            "  \"ASX:NEM\": \"ASX:NEM\",\n" +
            "  \"ASX:NEU\": \"ASX:NEU\",\n" +
            "  \"ASX:NGI\": \"ASX:NGI\",\n" +
            "  \"ASX:NHC\": \"ASX:NHC\",\n" +
            "  \"ASX:NHF\": \"ASX:NHF\",\n" +
            "  \"ASX:NIC\": \"ASX:NIC\",\n" +
            "  \"ASX:NIS\": \"ASX:NIS\",\n" +
            "  \"ASX:NMT\": \"ASX:NMT\",\n" +
            "  \"ASX:NOL\": \"ASX:NOL\",\n" +
            "  \"ASX:NOX\": \"ASX:NOX\",\n" +
            "  \"ASX:NPR\": \"ASX:NPR\",\n" +
            "  \"ASX:NSR\": \"ASX:NSR\",\n" +
            "  \"ASX:NST\": \"ASX:NST\",\n" +
            "  \"ASX:NTO\": \"ASX:NTO\",\n" +
            "  \"ASX:NTU\": \"ASX:NTU\",\n" +
            "  \"ASX:NUF\": \"ASX:NUF\",\n" +
            "  \"ASX:NVX\": \"ASX:NVX\",\n" +
            "  \"ASX:NWC\": \"ASX:NWC\",\n" +
            "  \"ASX:NWE\": \"ASX:NWE\",\n" +
            "  \"ASX:NWF\": \"ASX:NWF\",\n" +
            "  \"ASX:NWH\": \"ASX:NWH\",\n" +
            "  \"ASX:NWL\": \"ASX:NWL\",\n" +
            "  \"ASX:NWS\": \"ASX:NWS\",\n" +
            "  \"ASX:NXD\": \"ASX:NXD\",\n" +
            "  \"ASX:NXG\": \"ASX:NXG\",\n" +
            "  \"ASX:NXL\": \"ASX:NXL\",\n" +
            "  \"ASX:NXS\": \"ASX:NXS\",\n" +
            "  \"ASX:NXT\": \"ASX:NXT\",\n" +
            "  \"ASX:NZK\": \"ASX:NZK\",\n" +
            "  \"ASX:NZM\": \"ASX:NZM\",\n" +
            "  \"ASX:OBL\": \"ASX:OBL\",\n" +
            "  \"ASX:OBM\": \"ASX:OBM\",\n" +
            "  \"ASX:OCA\": \"ASX:OCA\",\n" +
            "  \"ASX:OCL\": \"ASX:OCL\",\n" +
            "  \"ASX:ODY\": \"ASX:ODY\",\n" +
            "  \"ASX:OFX\": \"ASX:OFX\",\n" +
            "  \"ASX:OZFOREX\": \"ASX:OFX\",\n" +
            "  \"ASX:OIL\": \"ASX:OIL\",\n" +
            "  \"ASX:OMH\": \"ASX:OMH\",\n" +
            "  \"ASX:OML\": \"ASX:OML\",\n" +
            "  \"ASX:OMN\": \"ASX:OMN\",\n" +
            "  \"ASX:AUD20\": \"ASX:OMN\",\n" +
            "  \"ASX:OPA\": \"ASX:OPA\",\n" +
            "  \"ASX:OPH\": \"ASX:OPH\",\n" +
            "  \"ASX:OPT\": \"ASX:OPT\",\n" +
            "  \"ASX:ORA\": \"ASX:ORA\",\n" +
            "  \"ASX:ORG\": \"ASX:ORG\",\n" +
            "  \"ASX:ORIGIN\": \"ASX:ORG\",\n" +
            "  \"ASX:PREMIUM\": \"ASX:ORG\",\n" +
            "  \"ASX:ORI\": \"ASX:ORI\",\n" +
            "  \"ASX:ORR\": \"ASX:ORR\",\n" +
            "  \"ASX:OZL\": \"ASX:OZL\",\n" +
            "  \"ASX:PAA\": \"ASX:PAA\",\n" +
            "  \"ASX:PAC\": \"ASX:PAC\",\n" +
            "  \"ASX:PAI\": \"ASX:PAI\",\n" +
            "  \"ASX:PAN\": \"ASX:PAN\",\n" +
            "  \"ASX:PAR\": \"ASX:PAR\",\n" +
            "  \"ASX:PBH\": \"ASX:PBH\",\n" +
            "  \"ASX:PBP\": \"ASX:PBP\",\n" +
            "  \"ASX:PCI\": \"ASX:PCI\",\n" +
            "  \"ASX:PDI\": \"ASX:PDI\",\n" +
            "  \"ASX:PDN\": \"ASX:PDN\",\n" +
            "  \"ASX:PE1\": \"ASX:PE1\",\n" +
            "  \"ASX:PEB\": \"ASX:PEB\",\n" +
            "  \"ASX:PEK\": \"ASX:PEK\",\n" +
            "  \"ASX:PEN\": \"ASX:PEN\",\n" +
            "  \"ASX:PEX\": \"ASX:PEX\",\n" +
            "  \"ASX:PFP\": \"ASX:PFP\",\n" +
            "  \"ASX:PGC\": \"ASX:PGC\",\n" +
            "  \"ASX:PGF\": \"ASX:PGF\",\n" +
            "  \"ASX:PGG\": \"ASX:PGG\",\n" +
            "  \"ASX:PGH\": \"ASX:PGH\",\n" +
            "  \"ASX:PGL\": \"ASX:PGL\",\n" +
            "  \"ASX:PIC\": \"ASX:PIC\",\n" +
            "  \"ASX:PIL\": \"ASX:PIL\",\n" +
            "  \"ASX:PIQ\": \"ASX:PIQ\",\n" +
            "  \"ASX:PL8\": \"ASX:PL8\",\n" +
            "  \"ASX:PLL\": \"ASX:PLL\",\n" +
            "  \"ASX:PLS\": \"ASX:PLS\",\n" +
            "  \"ASX:PLT\": \"ASX:PLT\",\n" +
            "  \"ASX:PLY\": \"ASX:PLY\",\n" +
            "  \"ASX:PMC\": \"ASX:PMC\",\n" +
            "  \"ASX:PME\": \"ASX:PME\",\n" +
            "  \"ASX:PMT\": \"ASX:PMT\",\n" +
            "  \"ASX:PMV\": \"ASX:PMV\",\n" +
            "  \"ASX:PNC\": \"ASX:PNC\",\n" +
            "  \"ASX:PNI\": \"ASX:PNI\",\n" +
            "  \"ASX:PNR\": \"ASX:PNR\",\n" +
            "  \"ASX:PNV\": \"ASX:PNV\",\n" +
            "  \"ASX:POS\": \"ASX:POS\",\n" +
            "  \"ASX:PPC\": \"ASX:PPC\",\n" +
            "  \"ASX:PPE\": \"ASX:PPE\",\n" +
            "  \"ASX:PPH\": \"ASX:PPH\",\n" +
            "  \"ASX:PPK\": \"ASX:PPK\",\n" +
            "  \"ASX:PPL\": \"ASX:PPL\",\n" +
            "  \"ASX:PPM\": \"ASX:PPM\",\n" +
            "  \"ASX:PPS\": \"ASX:PPS\",\n" +
            "  \"ASX:PPT\": \"ASX:PPT\",\n" +
            "  \"ASX:PRN\": \"ASX:PRN\",\n" +
            "  \"ASX:PRO\": \"ASX:PRO\",\n" +
            "  \"ASX:PRU\": \"ASX:PRU\",\n" +
            "  \"ASX:PSC\": \"ASX:PSC\",\n" +
            "  \"ASX:PSI\": \"ASX:PSI\",\n" +
            "  \"ASX:PSQ\": \"ASX:PSQ\",\n" +
            "  \"ASX:PTM\": \"ASX:PTM\",\n" +
            "  \"ASX:PWH\": \"ASX:PWH\",\n" +
            "  \"ASX:PWR\": \"ASX:PWR\",\n" +
            "  \"ASX:PXA\": \"ASX:PXA\",\n" +
            "  \"ASX:PYC\": \"ASX:PYC\",\n" +
            "  \"ASX:QAL\": \"ASX:QAL\",\n" +
            "  \"ASX:QAN\": \"ASX:QAN\",\n" +
            "  \"ASX:QBE\": \"ASX:QBE\",\n" +
            "  \"ASX:QOR\": \"ASX:QOR\",\n" +
            "  \"ASX:QRI\": \"ASX:QRI\",\n" +
            "  \"ASX:QUB\": \"ASX:QUB\",\n" +
            "  \"ASX:RAC\": \"ASX:RAC\",\n" +
            "  \"ASX:RBD\": \"ASX:RBD\",\n" +
            "  \"ASX:RBL\": \"ASX:RBL\",\n" +
            "  \"ASX:RBTZ\": \"ASX:RBTZ\",\n" +
            "  \"ASX:RCR\": \"ASX:RCR\",\n" +
            "  \"ASX:RCW\": \"ASX:RCW\",\n" +
            "  \"ASX:RDG\": \"ASX:RDG\",\n" +
            "  \"ASX:RDM\": \"ASX:RDM\",\n" +
            "  \"ASX:RDN\": \"ASX:RDN\",\n" +
            "  \"ASX:RDT\": \"ASX:RDT\",\n" +
            "  \"ASX:RDY\": \"ASX:RDY\",\n" +
            "  \"ASX:REA\": \"ASX:REA\",\n" +
            "  \"ASX:RED\": \"ASX:RED\",\n" +
            "  \"ASX:REG\": \"ASX:REG\",\n" +
            "  \"ASX:REH\": \"ASX:REH\",\n" +
            "  \"ASX:REP\": \"ASX:REP\",\n" +
            "  \"ASX:RF1\": \"ASX:RF1\",\n" +
            "  \"ASX:RFF\": \"ASX:RFF\",\n" +
            "  \"ASX:RFG\": \"ASX:RFG\",\n" +
            "  \"ASX:RETAIL\": \"ASX:RFG\",\n" +
            "  \"ASX:RFT\": \"ASX:RFT\",\n" +
            "  \"ASX:RG8\": \"ASX:RG8\",\n" +
            "  \"ASX:RGN\": \"ASX:RGN\",\n" +
            "  \"ASX:RHC\": \"ASX:RHC\",\n" +
            "  \"ASX:S00092445291\": \"ASX:RHC\",\n" +
            "  \"ASX:RHI\": \"ASX:RHI\",\n" +
            "  \"ASX:RIC\": \"ASX:RIC\",\n" +
            "  \"ASX:RIM\": \"ASX:RIM\",\n" +
            "  \"ASX:RIO\": \"ASX:RIO\",\n" +
            "  \"ASX:RMC\": \"ASX:RMC\",\n" +
            "  \"ASX:RMD\": \"ASX:RMD\",\n" +
            "  \"ASX:RESMED\": \"ASX:RMD\",\n" +
            "  \"ASX:RMS\": \"ASX:RMS\",\n" +
            "  \"ASX:RNU\": \"ASX:RNU\",\n" +
            "  \"ASX:RPL\": \"ASX:RPL\",\n" +
            "  \"ASX:RPM\": \"ASX:RPM\",\n" +
            "  \"ASX:RRL\": \"ASX:RRL\",\n" +
            "  \"ASX:RSG\": \"ASX:RSG\",\n" +
            "  \"ASX:RTH\": \"ASX:RTH\",\n" +
            "  \"ASX:RTR\": \"ASX:RTR\",\n" +
            "  \"ASX:RUL\": \"ASX:RUL\",\n" +
            "  \"ASX:RWC\": \"ASX:RWC\",\n" +
            "  \"ASX:RXL\": \"ASX:RXL\",\n" +
            "  \"ASX:RXM\": \"ASX:RXM\",\n" +
            "  \"ASX:S32\": \"ASX:S32\",\n" +
            "  \"ASX:SBM\": \"ASX:SBM\",\n" +
            "  \"ASX:ST\": \"ASX:SBM\",\n" +
            "  \"ASX:SCG\": \"ASX:SCG\",\n" +
            "  \"ASX:SDF\": \"ASX:SDF\",\n" +
            "  \"ASX:SDI\": \"ASX:SDI\",\n" +
            "  \"ASX:SDR\": \"ASX:SDR\",\n" +
            "  \"ASX:SEK\": \"ASX:SEK\",\n" +
            "  \"ASX:SFR\": \"ASX:SFR\",\n" +
            "  \"ASX:SFX\": \"ASX:SFX\",\n" +
            "  \"ASX:SGF\": \"ASX:SGF\",\n" +
            "  \"ASX:SGH\": \"ASX:SGH\",\n" +
            "  \"ASX:SGI\": \"ASX:SGI\",\n" +
            "  \"ASX:SGM\": \"ASX:SGM\",\n" +
            "  \"ASX:SGP\": \"ASX:SGP\",\n" +
            "  \"ASX:SGR\": \"ASX:SGR\",\n" +
            "  \"ASX:SHA\": \"ASX:SHA\",\n" +
            "  \"ASX:SHJ\": \"ASX:SHJ\",\n" +
            "  \"ASX:SHINE\": \"ASX:SHJ\",\n" +
            "  \"ASX:SHL\": \"ASX:SHL\",\n" +
            "  \"ASX:SHN\": \"ASX:SHN\",\n" +
            "  \"ASX:SHV\": \"ASX:SHV\",\n" +
            "  \"ASX:SELECT\": \"ASX:SHV\",\n" +
            "  \"ASX:SIG\": \"ASX:SIG\",\n" +
            "  \"ASX:SIQ\": \"ASX:SIQ\",\n" +
            "  \"ASX:SKC\": \"ASX:SKC\",\n" +
            "  \"ASX:SKF\": \"ASX:SKF\",\n" +
            "  \"ASX:SKO\": \"ASX:SKO\",\n" +
            "  \"ASX:SKS\": \"ASX:SKS\",\n" +
            "  \"ASX:SKT\": \"ASX:SKT\",\n" +
            "  \"ASX:SLA\": \"ASX:SLA\",\n" +
            "  \"ASX:SLC\": \"ASX:SLC\",\n" +
            "  \"ASX:SLH\": \"ASX:SLH\",\n" +
            "  \"ASX:SLR\": \"ASX:SLR\",\n" +
            "  \"ASX:SLX\": \"ASX:SLX\",\n" +
            "  \"ASX:SM1\": \"ASX:SM1\",\n" +
            "  \"ASX:SMI\": \"ASX:SMI\",\n" +
            "  \"ASX:SMP\": \"ASX:SMP\",\n" +
            "  \"ASX:SMR\": \"ASX:SMR\",\n" +
            "  \"ASX:SND\": \"ASX:SND\",\n" +
            "  \"ASX:SNL\": \"ASX:SNL\",\n" +
            "  \"ASX:SNZ\": \"ASX:SNZ\",\n" +
            "  \"ASX:SOL\": \"ASX:SOL\",\n" +
            "  \"ASX:SOM\": \"ASX:SOM\",\n" +
            "  \"ASX:SOP\": \"ASX:SOP\",\n" +
            "  \"ASX:SPK\": \"ASX:SPK\",\n" +
            "  \"ASX:SPL\": \"ASX:SPL\",\n" +
            "  \"ASX:SPR\": \"ASX:SPR\",\n" +
            "  \"ASX:SPX\": \"ASX:SPX\",\n" +
            "  \"ASX:SPZ\": \"ASX:SPZ\",\n" +
            "  \"ASX:SQ2\": \"ASX:SQ2\",\n" +
            "  \"ASX:SRG\": \"ASX:SRG\",\n" +
            "  \"ASX:SRL\": \"ASX:SRL\",\n" +
            "  \"ASX:SRV\": \"ASX:SRV\",\n" +
            "  \"ASX:SRX\": \"ASX:SRX\",\n" +
            "  \"ASX:SS1\": \"ASX:SS1\",\n" +
            "  \"ASX:SSG\": \"ASX:SSG\",\n" +
            "  \"ASX:SSM\": \"ASX:SSM\",\n" +
            "  \"ASX:SSR\": \"ASX:SSR\",\n" +
            "  \"ASX:SST\": \"ASX:SST\",\n" +
            "  \"ASX:STA\": \"ASX:STA\",\n" +
            "  \"ASX:STK\": \"ASX:STK\",\n" +
            "  \"ASX:STM\": \"ASX:STM\",\n" +
            "  \"ASX:STO\": \"ASX:STO\",\n" +
            "  \"ASX:STP\": \"ASX:STP\",\n" +
            "  \"ASX:STX\": \"ASX:STX\",\n" +
            "  \"ASX:SUL\": \"ASX:SUL\",\n" +
            "  \"ASX:SUPER\": \"ASX:SUL\",\n" +
            "  \"ASX:SUN\": \"ASX:SUN\",\n" +
            "  \"ASX:SUNCORP\": \"ASX:SUN\",\n" +
            "  \"ASX:SVL\": \"ASX:SVL\",\n" +
            "  \"ASX:SVM\": \"ASX:SVM\",\n" +
            "  \"ASX:SVR\": \"ASX:SVR\",\n" +
            "  \"ASX:SVW\": \"ASX:SVW\",\n" +
            "  \"ASX:SWM\": \"ASX:SWM\",\n" +
            "  \"ASX:SXE\": \"ASX:SXE\",\n" +
            "  \"ASX:SXG\": \"ASX:SXG\",\n" +
            "  \"ASX:SXL\": \"ASX:SXL\",\n" +
            "  \"ASX:SYA\": \"ASX:SYA\",\n" +
            "  \"ASX:SYM\": \"ASX:SYM\",\n" +
            "  \"ASX:SYR\": \"ASX:SYR\",\n" +
            "  \"ASX:SZL\": \"ASX:SZL\",\n" +
            "  \"ASX:TAH\": \"ASX:TAH\",\n" +
            "  \"ASX:TBN\": \"ASX:TBN\",\n" +
            "  \"ASX:TBR\": \"ASX:TBR\",\n" +
            "  \"ASX:TCF\": \"ASX:TCF\",\n" +
            "  \"ASX:TCG\": \"ASX:TCG\",\n" +
            "  \"ASX:TCL\": \"ASX:TCL\",\n" +
            "  \"ASX:TEA\": \"ASX:TEA\",\n" +
            "  \"ASX:TER\": \"ASX:TER\",\n" +
            "  \"ASX:TGA\": \"ASX:TGA\",\n" +
            "  \"ASX:THORN\": \"ASX:TGA\",\n" +
            "  \"ASX:TG6\": \"ASX:TG6\",\n" +
            "  \"ASX:TGM\": \"ASX:TGM\",\n" +
            "  \"ASX:TGP\": \"ASX:TGP\",\n" +
            "  \"ASX:TGR\": \"ASX:TGR\",\n" +
            "  \"ASX:TASSAL\": \"ASX:TGR\",\n" +
            "  \"ASX:THL\": \"ASX:THL\",\n" +
            "  \"ASX:TIE\": \"ASX:TIE\",\n" +
            "  \"ASX:TLC\": \"ASX:TLC\",\n" +
            "  \"ASX:TLG\": \"ASX:TLG\",\n" +
            "  \"ASX:TLS\": \"ASX:TLS\",\n" +
            "  \"ASX:TLX\": \"ASX:TLX\",\n" +
            "  \"ASX:TNE\": \"ASX:TNE\",\n" +
            "  \"ASX:TNT\": \"ASX:TNT\",\n" +
            "  \"ASX:TOT\": \"ASX:TOT\",\n" +
            "  \"ASX:TPC\": \"ASX:TPC\",\n" +
            "  \"ASX:TPG\": \"ASX:TPG\",\n" +
            "  \"ASX:TPW\": \"ASX:TPW\",\n" +
            "  \"ASX:TRA\": \"ASX:TRA\",\n" +
            "  \"ASX:TRJ\": \"ASX:TRJ\",\n" +
            "  \"ASX:TRS\": \"ASX:TRS\",\n" +
            "  \"ASX:TSK\": \"ASX:TSK\",\n" +
            "  \"ASX:TSO\": \"ASX:TSO\",\n" +
            "  \"ASX:TTM\": \"ASX:TTM\",\n" +
            "  \"ASX:TTT\": \"ASX:TTT\",\n" +
            "  \"ASX:TUA\": \"ASX:TUA\",\n" +
            "  \"ASX:TUL\": \"ASX:TUL\",\n" +
            "  \"ASX:TWE\": \"ASX:TWE\",\n" +
            "  \"ASX:TREASURY\": \"ASX:TWE\",\n" +
            "  \"ASX:TWR\": \"ASX:TWR\",\n" +
            "  \"ASX:TYR\": \"ASX:TYR\",\n" +
            "  \"ASX:UBN\": \"ASX:UBN\",\n" +
            "  \"ASX:UMG\": \"ASX:UMG\",\n" +
            "  \"ASX:UNI\": \"ASX:UNI\",\n" +
            "  \"ASX:UOS\": \"ASX:UOS\",\n" +
            "  \"ASX:URF\": \"ASX:URF\",\n" +
            "  \"ASX:URW\": \"ASX:URW\",\n" +
            "  \"ASX:VAU\": \"ASX:VAU\",\n" +
            "  \"ASX:VCX\": \"ASX:VCX\",\n" +
            "  \"ASX:VEA\": \"ASX:VEA\",\n" +
            "  \"ASX:VEE\": \"ASX:VEE\",\n" +
            "  \"ASX:VG1\": \"ASX:VG1\",\n" +
            "  \"ASX:VGL\": \"ASX:VGL\",\n" +
            "  \"ASX:VHM\": \"ASX:VHM\",\n" +
            "  \"ASX:VHT\": \"ASX:VHT\",\n" +
            "  \"ASX:VIT\": \"ASX:VIT\",\n" +
            "  \"ASX:VLS\": \"ASX:VLS\",\n" +
            "  \"ASX:VML\": \"ASX:VML\",\n" +
            "  \"ASX:VMM\": \"ASX:VMM\",\n" +
            "  \"ASX:VMT\": \"ASX:VMT\",\n" +
            "  \"ASX:VNL\": \"ASX:VNL\",\n" +
            "  \"ASX:VNT\": \"ASX:VNT\",\n" +
            "  \"ASX:VOC\": \"ASX:VOC\",\n" +
            "  \"ASX:VOCUS\": \"ASX:VOC\",\n" +
            "  \"ASX:VRL\": \"ASX:VRL\",\n" +
            "  \"ASX:VILLAGE\": \"ASX:VRL\",\n" +
            "  \"ASX:VSL\": \"ASX:VSL\",\n" +
            "  \"ASX:VUK\": \"ASX:VUK\",\n" +
            "  \"ASX:VUL\": \"ASX:VUL\",\n" +
            "  \"ASX:VVA\": \"ASX:VVA\",\n" +
            "  \"ASX:WA1\": \"ASX:WA1\",\n" +
            "  \"ASX:WAA\": \"ASX:WAA\",\n" +
            "  \"ASX:WAF\": \"ASX:WAF\",\n" +
            "  \"ASX:WAM\": \"ASX:WAM\",\n" +
            "  \"ASX:WBC\": \"ASX:WBC\",\n" +
            "  \"ASX:WBT\": \"ASX:WBT\",\n" +
            "  \"ASX:WC8\": \"ASX:WC8\",\n" +
            "  \"ASX:WCG\": \"ASX:WCG\",\n" +
            "  \"ASX:WDS\": \"ASX:WDS\",\n" +
            "  \"ASX:WEB\": \"ASX:WEB\",\n" +
            "  \"ASX:WEBJET\": \"ASX:WEB\",\n" +
            "  \"ASX:WES\": \"ASX:WES\",\n" +
            "  \"ASX:WFD\": \"ASX:WFD\",\n" +
            "  \"ASX:WESTFIELD\": \"ASX:WFD\",\n" +
            "  \"ASX:WGB\": \"ASX:WGB\",\n" +
            "  \"ASX:WGN\": \"ASX:WGN\",\n" +
            "  \"ASX:WGX\": \"ASX:WGX\",\n" +
            "  \"ASX:WHC\": \"ASX:WHC\",\n" +
            "  \"ASX:WHF\": \"ASX:WHF\",\n" +
            "  \"ASX:WJL\": \"ASX:WJL\",\n" +
            "  \"ASX:WLE\": \"ASX:WLE\",\n" +
            "  \"ASX:WMG\": \"ASX:WMG\",\n" +
            "  \"ASX:WMI\": \"ASX:WMI\",\n" +
            "  \"ASX:WML\": \"ASX:WML\",\n" +
            "  \"ASX:WOR\": \"ASX:WOR\",\n" +
            "  \"ASX:WOW\": \"ASX:WOW\",\n" +
            "  \"ASX:WPR\": \"ASX:WPR\",\n" +
            "  \"ASX:WR1\": \"ASX:WR1\",\n" +
            "  \"ASX:WTC\": \"ASX:WTC\",\n" +
            "  \"ASX:WTN\": \"ASX:WTN\",\n" +
            "  \"ASX:XRF\": \"ASX:XRF\",\n" +
            "  \"ASX:XRO\": \"ASX:XRO\",\n" +
            "  \"ASX:XTE\": \"ASX:XTE\",\n" +
            "  \"ASX:XYZ\": \"ASX:XYZ\",\n" +
            "  \"ASX:YAL\": \"ASX:YAL\",\n" +
            "  \"ASX:ZIM\": \"ASX:ZIM\",\n" +
            "  \"ASX:ZIP\": \"ASX:ZIP\",\n" +
            "  \"ASX:ZNC\": \"ASX:ZNC\",\n" +
            "  \"ASX:ZNO\": \"ASX:ZNO\",\n" +
            "  \"ASX:ZNT\": \"ASX:ZNT\",\n" +
            "  \"ASX:REF:\": \"ASX:ZNT\"\n" +
            "}";
}