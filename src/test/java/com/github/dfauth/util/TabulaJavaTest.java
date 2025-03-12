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
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.functional.Tuple2.tuple2;
import static io.github.dfauth.trycatch.Try.tryWith;

@Slf4j
@Data
public class TabulaJavaTest  implements PDFContext<Payment[]> {

    private List<EventProcessor> eventProcessors = new ArrayList<>();
    private int year;
    private List<String> textBuffer = new ArrayList<>();


    @Test
    public void testIt() throws IOException {

        try(PrintWriter pw = new PrintWriter(new FileOutputStream("out.csv"))) {
            pw.println("Bank Account,Date,Narrative,Debit Amount,Credit Amount,Balance,Categories,Serial");


            registerEventProcessor(e -> tryWith(() -> Integer.valueOf(e.trim())).toOptional().isPresent(), (text, ctx) -> {
                if(ctx.getTextSize() == 1) {
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

            registerEventProcessor(e -> e.contains("........................................................................................................................................................................................................................."),
                    (text, ctx) -> {
                        // end of cell
                        if (ctx.isTableData()) {
                            Arrays.stream(ctx.getCells()).forEach(p -> {
                                // Bank Account	Date	Narrative	Debit Amount	Credit Amount	Balance	Categories	Serial
                                pw.println("32099621742," + p.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "," + p.getDetail() + "," + (p.getTxnType().isDebit() ? p.getValue() : "") + "," + (p.getTxnType().isCredit() ? p.getValue() : "") + "," + p.getBalance() + "," + p.getTxnType() + ",");
                            });
                        } else {
                            // discard
                            ctx.removeText();
                        }
                    });

            registerEventProcessor(e -> e.contains("Debit Credit Balance"),
                    (text, ctx) -> {
                        // end of header
                        // discard
                        ctx.removeText();
                        textBuffer.add(""); // this is cheating
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
    }

    private void registerEventProcessor(Predicate<String> p, BiConsumer<String,PDFContext<Payment[]>> c2) {
        eventProcessors.add(new EventProcessor(p, c2));
    }

    private void onEvent(String text, PDFContext ctx) {
        eventProcessors.stream().filter(ep -> ep.test(text)).findFirst().ifPresentOrElse(ep -> ep.accept(text, ctx), () -> textBuffer.add(text));
    }

    @Override
    public String removeText() {
        return removeText(s -> s.collect(Collectors.joining("|")));
    }

    public <T> T removeText(Function<Stream<String>, T> f) {
        try {
            return f.apply(textBuffer.stream());
        } finally {
            textBuffer.clear();
        }
    }

    @Override
    public int getTextSize() {
        return textBuffer.size();
    }

    @Override
    public Payment[] getCells() {
        AtomicBoolean isMultiLineEntry = new AtomicBoolean();
        return removeText(s -> s.collect(statefulCollector(
                () -> tuple2(this, Payment.PaymentFactory.builder()),
                l -> l.stream().map(t2 -> t2._2().build().guessTxnType().toPayment()).toArray(Payment[]::new),
                (t2, _s) -> {
                    assert(_s.trim().isEmpty());
                },
                (t2, _s) -> {
                    var arr = _s.split(" ");
                    isMultiLineEntry.set(arr.length > 1);
                    if(isMultiLineEntry.get()) {
                        t2._2().date(LocalDate.of(t2._1().year, monthValueOf(arr[1]), Integer.parseInt(arr[0])));
                        t2._2().detail(Arrays.stream(Arrays.copyOfRange(arr, 2, arr.length)).collect(Collectors.joining(" ")));
                    }
                },
                (t2, _s) -> {
                    var arr = _s.split(" ");
                    if(arr.length > 1) {
                        t2._2().date(LocalDate.of(t2._1().year, monthValueOf(arr[1]), Integer.parseInt(arr[0])));
                        t2._2().detail(Arrays.stream(Arrays.copyOfRange(arr, 2, arr.length)).collect(Collectors.joining(" ")));
                    }
                },
                (t2, _s) -> {
                    if(isMultiLineEntry.get()) {
                        t2._2().detail(t2._2().build().detail+" "+_s);
                    } else {
                        var value = new BigDecimal(_s.trim().replace(",",""));
                        t2._2().credit(value);
                        t2._2().debit(value);
                    }
                },
                (t2, _s) -> {
                    var arr = _s.split(" ");
                    if(isMultiLineEntry.get()) {
                        var value = new BigDecimal(arr[0].replace(",",""));
                        var balance = new BigDecimal(arr[1].replace(",",""));
                        t2._2().credit(value);
                        t2._2().debit(value);
                        t2._2().balance(balance);
                    } else {
                        var balance = new BigDecimal(arr[0].replace(",",""));
                        t2._2().balance(balance);
                    }
                }

        )));
    }

    private int monthValueOf(String s) {
        return Arrays.stream(Month.values()).filter(m -> m.name().substring(0,3).equalsIgnoreCase(s)).findFirst().map(m -> m.ordinal()+1).orElseThrow();
    }

    @AllArgsConstructor
    private static class EventProcessor implements Predicate<String>, BiConsumer<String, PDFContext<Payment[]>> {

        private Predicate<String> p;
        private BiConsumer<String,PDFContext<Payment[]>> consumer;

        @Override
        public boolean test(String s) {
            return p.test(s);
        }

        @Override
        public void accept(String text, PDFContext<Payment[]> pdfContext) {
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

    String removeText();

    int getTextSize();

    default boolean isTableData() {
        return getTextSize() == 5;
    }

    R getCells();
}

