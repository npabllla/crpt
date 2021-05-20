package ru.boblak;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

/** API class
 * @author Kirill Boblak
 * */
public class CrptApi {
    private static final String URL_FOR_CREATE =
            "https://ismp.crpt.ru/api/v3/lk/documents/create";

    private final RateLimiter rateLimiter;

    public CrptApi(long interval, int requestLimit) {
        this.rateLimiter = new RateLimiter(interval, requestLimit);
    }

    /**
     * @param document for creating
     * @param signature for document
     * */
    public void createDocument(Document document, String signature) {
        if (this.rateLimiter.availableForWork()) {
            document.setSignature(signature);
            sendData(transformObjectToJson(document),
                    URL_FOR_CREATE);
        }
    }

    private void sendData(String data, String urlForSend) {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(data))
                    .uri(URI.create(urlForSend))
                    .header("Content-Type", "application/json")
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> String transformObjectToJson(T objectToJson) {
        final Gson gson = new GsonBuilder().create();
        return gson.toJson(objectToJson);
    }

}

/** Rate limit class for API
 * @author Kirill Boblak
 * */
class RateLimiter {
    final int requestLimit;

    final long interval;

    double available;

    long lastTimeStamp;

    public RateLimiter(long interval, int requestLimit) {
        this.requestLimit = requestLimit;
        this.interval = interval;

        available = 0;
        lastTimeStamp = System.currentTimeMillis();
    }

    synchronized boolean availableForWork() {
        long now = System.currentTimeMillis();
        available += (now - lastTimeStamp) * 1.0 / interval * requestLimit;
        if (available > requestLimit) {
            available = requestLimit;
        }
        if (available < 1) {
            return false;
        } else {
            available--;
            lastTimeStamp = now;
            return true;
        }
    }
}

class Document {
    private final String documentFormat = "MANUAL";

    private ProductDocument productDocument;

    private String productGroup;

    private final String type = "LP_INTRODUCE_GOODS";

    private String signature;

    public void setSignature(String signature) {
        this.signature = signature;
    }

//    Getters and Setters
}

class ProductDocument {
    private String docId;

    private String docStatus;

    private String docType;

    private boolean importRequest;

    private String ownerInn;

    private String participantInn;

    private LocalDate productionDate;

    private String productionType;

    private List<Product> products;

    private LocalDate regDate;

    private String regNumber;

//    Getters and Setters
}

class Product {
    private String certificateDocument;

    private String certificateDocumentDate;

    private String certificateDocumentNumber;

    private String ownerInn;

    private String producerInn;

    private LocalDate productionDate;

    private String tnvedCode;

    private String uitCode;

    private String uituCode;

//    Getters and Setters
}