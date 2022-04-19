package solutions.capybara.fetchassess.controllers;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/fetch/v1")
public class FetchControllerV1 {

    private static final Logger log = LoggerFactory.getLogger(FetchControllerV1.class);

    private static final List<Transaction> TRANSACTIONS = new ArrayList<>();

    @PostMapping(path = "/transaction")
    public ResponseEntity<?> doTransactionPost(HttpServletResponse response,
                                               HttpServletRequest request)
            throws ServletException, IOException {

        try {
            ServletInputStream stream = request.getInputStream();

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = stream.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }

            String payload = result.toString("UTF-8");

            Object obj = new JSONParser().parse(payload);

            JSONObject jo = (JSONObject) obj;

            int responseCode = -1;
            StringBuilder bodyBuilder = new StringBuilder();

            String payer = (String) jo.getOrDefault("payer", null);
            if (payer == null) {
                responseCode = HttpStatus.BAD_REQUEST.value();
                bodyBuilder.append("payer missing.").append("\n");
            }

            Integer points = null;
            Object pointsString = jo.getOrDefault("points", null);
            if (pointsString == null) {
                responseCode = HttpStatus.BAD_REQUEST.value();
                bodyBuilder.append("points missing.").append("\n");
            } else {
                try {
                    points = Integer.valueOf(((Long) pointsString).intValue());
                } catch (ClassCastException | NumberFormatException e) {
                    log.info("cannot parse points: " + pointsString);
                    responseCode = HttpStatus.BAD_REQUEST.value();
                    bodyBuilder.append("Cannot parse points as Integer").append("\n");
                }
            }

            Date timestamp = null;
            Object timestampString = jo.getOrDefault("timestamp", null);
            if (timestampString == null) {
                responseCode = HttpStatus.BAD_REQUEST.value();
                bodyBuilder.append("timestamp").append("\n");
            } else {
                try {
                    Instant timestampInstant = Instant.parse((String) timestampString);
                    timestamp = Date.from(timestampInstant);
                    //LoggerFactory.getLogger(this.getClass()).info(timestampInstant.toString());
                } catch (ClassCastException e) {
                    log.info("cannot parse timestamp: " + timestampString);
                    responseCode = HttpStatus.BAD_REQUEST.value();
                    bodyBuilder.append("Cannot parse timestamp as Date").append("\n");
                }
            }

            if (responseCode > -1) {
                return ResponseEntity.status(HttpStatus.valueOf(responseCode)).body(bodyBuilder.toString());
            }

            Transaction newTransaction = new Transaction(payer, timestamp, points);

            newTransaction.setPayer(payer);
            newTransaction.setPoints(points);
            newTransaction.setTimestamp(timestamp);

            TRANSACTIONS.add(newTransaction);
        } catch (ParseException e) {
            log.error("Error", e);
        }

        return ResponseEntity.status(HttpStatus.OK).body("We good");
    }

    @PostMapping(path = "/spend")
    public void doSpendPost(HttpServletResponse response,
                            HttpServletRequest request)
            throws IOException {

        log.info("\n\nspend");

        response.reset();
        response.setContentType("application/json");
        try (PrintWriter printWriter = response.getWriter()) {

            ServletInputStream stream = request.getInputStream();

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = stream.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }

            String payload = result.toString("UTF-8");

            // parsing file "JSONExample.json"
            Object obj = new JSONParser().parse(payload);

            // typecasting obj to JSONObject
            JSONObject jo = (JSONObject) obj;

            int responseCode = -1;
            StringBuilder bodyBuilder = new StringBuilder();

            Integer points = null;
            Object pointsString = jo.getOrDefault("points", null);
            if (pointsString == null) {
                responseCode = HttpStatus.BAD_REQUEST.value();
                bodyBuilder.append("points missing.").append("\n");
            } else {
                try {
                    points = Integer.valueOf(((Long) pointsString).intValue());
                } catch (ClassCastException | NumberFormatException e) {
                    log.info("cannot parse points: " + pointsString);
                    responseCode = HttpStatus.BAD_REQUEST.value();
                    bodyBuilder.append("Cannot parse points as Integer").append("\n");
                }
            }

            if (responseCode > -1) {
                response.sendError(responseCode, bodyBuilder.toString());
            } else {

                if (points > getCurrentTotalPoints()) {
                    JSONObject newObject = new JSONObject();
                    newObject.put("error", "insufficient points");
                    printWriter.write(newObject.toString());
                } else {
                    List<Transaction> copyTransactions = TRANSACTIONS
                            .stream()
                            .sorted(Comparator.comparing(Transaction::getTimestamp))
                            .collect(Collectors.toList());

                    Map<String,Integer> currentBalances = getCurrentBalances();

                    Integer remainingToDeduct = points;
                    Map<String, Integer> deductedPoints = new HashMap<>();
                    List<Transaction> newTransactions = new ArrayList<>();
                    for (Transaction transaction : copyTransactions) {
                        log.info(transaction.toString());
                        String payer = transaction.getPayer();
                        if (!deductedPoints.containsKey(payer)) {
                            deductedPoints.put(payer, 0);
                        }

                        Integer toDeduct = remainingToDeduct;
                        if (transaction.getPoints() < remainingToDeduct) {
                            toDeduct = transaction.getPoints();
                        }

                        log.info("toDeduct: " + toDeduct);

                        deductedPoints.put(payer, deductedPoints.get(payer) - toDeduct);
//                        if ((deductedPoints.get(payer) - toDeduct) * -1 > getCurrentBalances().get(payer)) {
//                            continue;
//                        }
                        remainingToDeduct -= toDeduct;
                        log.info("remainingToDeduct: " + remainingToDeduct);

                        Transaction newTransaction = new Transaction(payer, transaction.timestamp, toDeduct * -1);
                        newTransactions.add(newTransaction);

                        if (remainingToDeduct == 0) {
                            break;
                        }
                    }


                    TRANSACTIONS.addAll(newTransactions);
                    JSONArray newArray = new JSONArray();
                    deductedPoints.forEach((e, f) -> {
                        JSONObject newObject = new JSONObject();
                        newObject.put("payer", e);
                        newObject.put("points", f);
                        newArray.put(newObject);
                    });
                    printWriter.write(newArray.toString());
                }
            }

        } catch (ParseException e) {
            log.error("Error", e);
        }

        log.info("end spend\n\n");
    }

    @PostMapping(path = "/cleartransactions")
    public void doClearPost(HttpServletResponse response,
                            HttpServletRequest request)
            throws ServletException, IOException {
        TRANSACTIONS.clear();
    }

    @GetMapping(path = "/balances")
    public void doBalancesGet(HttpServletResponse response,
                              HttpServletRequest request)
            throws ServletException, IOException {

        response.setContentType("application/json");

        JSONArray newArray = new JSONArray();

        getCurrentBalances().forEach((k, v) -> {
            JSONObject newObject = new JSONObject();
            newObject.put("payer", k);
            newObject.put("points", v);
            newArray.put(newObject);
        });

        response.getWriter().write(newArray.toString());
    }

    private Map<String, Integer> getCurrentBalances() {
        Map<String, Integer> output = new HashMap<>();

        TRANSACTIONS.stream()
                .collect(Collectors.groupingBy(Transaction::getPayer, Collectors.summingInt(Transaction::getPoints)))
                .forEach((payer, count) -> {
                    output.put(payer, count);
                });

        return output;
    }

    private Integer getCurrentTotalPoints() {
        return TRANSACTIONS.stream().mapToInt(Transaction::getPoints).sum();
    }

    private static class Transaction {

        private String payer;
        private Date timestamp;
        private Integer points;

        public Transaction(String payer, Date timestamp, Integer points) {
            this.payer = payer;
            this.timestamp = timestamp;
            this.points = points;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(Integer points) {
            this.points = points;
        }

        public String getPayer() {
            return payer;
        }

        public void setPayer(String payer) {
            this.payer = payer;
        }

        @Override
        public String toString() {
            return getPayer() + "/" + getPoints() + "/" + getTimestamp().toInstant().toString();
        }
    }
}
