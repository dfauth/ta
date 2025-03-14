package com.github.dfauth.util;

import com.github.dfauth.ta.model.txn.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static io.github.dfauth.trycatch.Try.tryWith;

@Slf4j
@Data
public class TabulaJavaTest  implements PDFContext<Payment> {

    private List<EventProcessor> eventProcessors = new ArrayList<>();
    private int year;
    private List<String> textBuffer = new ArrayList<>();
    private Locale en_AU = new Locale.Builder().setLanguageTag("en-AU").build();
    private Pattern AUD = Pattern.compile("^((([1-9]\\d{0,10}(,\\d{3})*)|(([1-9]\\d*)?\\d))(\\.\\d\\d))$");
//    private Pattern AUD = Pattern.compile("^(([1-9]\\d{0,10}(,\\d{3})*)|(([1-9]\\d*)?\\d))(\\.\\d\\d)?$");


    @Test
    public void testIt() throws IOException {

        List<Payment> payments = new ArrayList<>();

        try(PrintWriter pw = new PrintWriter(new FileOutputStream("out.csv"))) {
            pw.println("Bank Account,Date,Narrative,Debit Amount,Credit Amount,Balance,Categories,Serial");


            registerEventProcessor(e -> tryWith(() -> Integer.valueOf(e.trim())).toOptional().isPresent(), (text, ctx) -> {
                if(text.trim().length() == 4) {
                    ctx.setYear(Integer.parseInt(text.trim()));
                }
            });

            registerEventProcessor(e -> e.contains("STATEMENT OPENING BALANCE"), (text, ctx) -> {
                var arr = text.split(" ");
                // prior to nov 2016 this was a year: 2015
                ctx.setYear(tryWith(() -> Integer.parseInt(arr[0])).toOptional().orElseGet(() -> {
                    // after nov 2016 it was a date: 30/11/16
                    return LocalDate.parse(arr[0], DateTimeFormatter.ofPattern("dd/MM/yy")).getYear();
                }));
            });

            registerEventProcessor(e -> true,
                    (text, ctx) -> {
                        // try to get a ledger entry from the last 3 , 4 or 5 text events
                        IntStream.of(2,3,4,5,6).forEach(i -> {
                            Optional.ofNullable(ctx.getCells(i)).ifPresent(p -> {
                                // Bank Account	Date	Narrative	Debit Amount	Credit Amount	Balance	Categories	Serial
                                pw.println("32099621742," + p.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "," + p.getDetail() + "," + (p.getTxnType().isDebit() ? p.getValue() : "") + "," + (p.getTxnType().isCredit() ? p.getValue() : "") + "," + p.getBalance() + "," + p.getTxnType() + ",");
                                payments.add(p);
                            });
                        });
                    });

            Arrays.stream(new File("C:\\Users\\dfaut\\Downloads\\txns\\2015\\").listFiles())
                    .forEach(f -> {
                        log.info("processing file {}",f);
                        try (FileInputStream in = new FileInputStream(f)) {
                            try (PDDocument document = PDDocument.load(in)) {
                                var sea = new BasicExtractionAlgorithm();
                                PageIterator pi = new ObjectExtractor(document).extract();
                                while (pi.hasNext()) {
                                    // iterate over the pages of the document
                                    Page page = pi.next();
                                    List<Table> table = sea.extract(page);
                                    // iterate over the tables of the page
                                    for (Table tables : table) {
                                        List<List<RectangularTextContainer>> rows = tables.getRows();
                                        // iterate over the rows of the table
                                        for (List<RectangularTextContainer> cells : rows) {
                                            // print all column-cells of the row plus linefeed
                                            for (RectangularTextContainer content : cells) {
                                                // Note: Cell.getText() uses \r to concat text chunks
                                                String text = content.getText().replace("\r", " ");
                                                onEvent(text, this);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (FileNotFoundException e) {
                            log.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    });
        }

        // validate
        payments.stream().reduce((l,r) -> l.validate(r));
    }

    private void registerEventProcessor(Predicate<String> p, BiConsumer<String,PDFContext<Payment>> c2) {
        eventProcessors.add(new EventProcessor(p, c2));
    }

    private void onEvent(String text, PDFContext<Payment> ctx) {
        textBuffer.add(text);
        eventProcessors.stream()
                .filter(ep -> ep.test(text))
                .findFirst()
                .ifPresent(
                        ep -> ep.accept(text, ctx)
                );
    }

    @Override
    public String removeText(int n) {
        return removeText(n, s -> s.collect(Collectors.joining("|")));
    }

    public <T> T removeText(int n, Function<Stream<String>, T> f) {
        try {
            var t = f.apply(textBuffer.subList(textBuffer.size()-n, textBuffer.size()).stream());
            textBuffer.clear();
            return t;
        } catch(RuntimeException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public int getTextSize() {
        return textBuffer.size();
    }

    @Override
    public Payment getCells(int n) {
        return removeText(n, s -> parsePayment(s.collect(Collectors.joining(" ")), this));
    }

    private Payment parsePayment(String str, PDFContext<Payment> ctx) {

        var arr = str.trim().split(" ");

        // expecting at least 4 fields
        if(arr.length < 4) {
            throw new IllegalStateException("oops");
        }

        var builder = Payment.PaymentFactory.builder();

        AtomicInteger detailOffset = new AtomicInteger(2);

        // start with a date in the for 2 Dec
        tryCatch(() -> builder.date(LocalDate.parse(String.format("%s %s %d", arr[0], arr[1], ctx.getYear()), DateTimeFormatter.ofPattern("dd MMM yyyy"))), e -> {
            // if this fails try for format 30/11/16 as this was the format after this date
            var result = builder.date(LocalDate.parse(arr[0], DateTimeFormatter.ofPattern("dd/MM/yy")));
            detailOffset.set(1);
            return result;
        });

        // finish with 2 decimal values
        int lastIdx = arr.length-1;
        var balance = Optional.of(AUD.matcher(arr[lastIdx].trim())).filter(Matcher::find).map(m -> m.group(1).replace(",","")).map(BigDecimal::new).orElseThrow();
        builder.balance(balance);

        var value = Optional.of(AUD.matcher(arr[lastIdx-1].trim())).filter(Matcher::find).map(m -> m.group(1).replace(",","")).map(BigDecimal::new).orElseThrow();
//        var value = new BigDecimal(arr[lastIdx-1].trim().replace(",",""));
        builder.debit(value);
        builder.credit(value);

        // if we got this far, it's probably valid, the rest is the description
        builder.detail(IntStream.range(detailOffset.get(), lastIdx-1).mapToObj(i -> arr[i]).collect(Collectors.joining(" ")));

        var factory = builder.build();
        factory.guessTxnType();

        return factory.toPayment().sanityCheck();
    }

    @AllArgsConstructor
    private static class EventProcessor implements Predicate<String>, BiConsumer<String, PDFContext<Payment>> {

        private Predicate<String> p;
        private BiConsumer<String,PDFContext<Payment>> consumer;

        @Override
        public boolean test(String s) {
            return p.test(s);
        }

        @Override
        public void accept(String text, PDFContext<Payment> pdfContext) {
            consumer.accept(text, pdfContext);
        }
    }
    public static <T,R> Collector<String, List<T>, R> statefulCollector(Supplier<T> tSupplier, Function<List<T>,R> finisher, BiConsumer<T,String>... processors) {

        AtomicInteger cnt = new AtomicInteger(0);
        return new Collector<>() {
            @Override
            public Supplier<List<T>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<T>, String> accumulator() {
                AtomicReference<T> tmp = new AtomicReference<>();
                return (l, s) -> {
                    if (cnt.get() == 0) {
                        tmp.set(tSupplier.get());
                    }
                    processors[cnt.getAndIncrement() % processors.length].accept(tmp.get(), s);
                    if (cnt.get() % processors.length == 0) {
                        l.add(tmp.get());
                    }
                };
            }

            @Override
            public BinaryOperator<List<T>> combiner() {
                return oops();
            }

            @Override
            public Function<List<T>, R> finisher() {
                return finisher;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }


}

interface PDFContext<R> {

    void setYear(int yr);
    int getYear();

    String removeText(int n);

    int getTextSize();

    default boolean isTableData() {
        return getTextSize() == 5;
    }

    default R getCells() {
        return getCells(getTextSize());
    }

    R getCells(int n);
}

