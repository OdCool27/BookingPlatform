package http;

import com.sun.net.httpserver.HttpExchange;
import dto.ApiDtos;
import helpers.Json;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class HttpSupport {
    public static final int MAX_BODY = 65536;
    private HttpSupport() {}
    public static <T> T body(HttpExchange exchange,Class<T> type) throws IOException {
        // Accept JSON media types with optional parameters, such as application/json; charset=utf-8.
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if(contentType == null || !contentType.split(";")[0].trim().equalsIgnoreCase("application/json"))
            throw new HttpException(415,"Content-Type must be application/json");
        // Read one byte beyond the limit to detect oversize bodies without loading an unlimited stream.
        // This also works when the client does not provide a Content-Length header.
        byte[] bytes = exchange.getRequestBody().readNBytes(MAX_BODY + 1);
        if(bytes.length > MAX_BODY) throw new HttpException(413,"Request body exceeds 65536 bytes");
        try {
            // The caller supplies its DTO class. Jackson checks fields/types using the shared mapper.
            // JSON null is syntactically valid, but cannot serve as a request object.
            T value = Json.MAPPER.readValue(bytes,type);
            if(value == null) throw new HttpException(400,"A JSON object is required");
            return value;
        } catch(com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new HttpException(400,"Invalid JSON or request fields");
        }
    }
    public static void send(HttpExchange exchange,int status,Object response) throws IOException {
        // Set metadata before committing headers. Avoid caching potentially personal responses,
        // and tell clients to interpret the content as JSON rather than guessing its type.
        exchange.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control","no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options","nosniff");
        // HttpServer uses -1 for no response body, which is required for a 204 response.
        if(status == 204) { exchange.sendResponseHeaders(status,-1); return; }
        byte[] bytes = Json.MAPPER.writeValueAsBytes(response);
        // Content length is measured in encoded bytes, not Java characters. Closing the output
        // stream completes the response; the router closes the whole exchange in its finally block.
        exchange.sendResponseHeaders(status,bytes.length);
        try(OutputStream output = exchange.getResponseBody()) { output.write(bytes); }
    }
    public static void error(HttpExchange exchange,int status,String code,String message) throws IOException {
        // A 401 challenge tells the client which authentication scheme this API accepts.
        if(status == 401) exchange.getResponseHeaders().set("WWW-Authenticate","Basic realm=\"Osyntra\", charset=\"UTF-8\"");
        send(exchange,status,new ApiDtos.Error(code,message));
    }
    public static Map<String,String> query(HttpExchange exchange,Set<String> allowed) {
        Map<String,String> result = new HashMap<>();
        String raw = exchange.getRequestURI().getRawQuery();
        if(raw == null || raw.isEmpty()) return result;
        for(String part : raw.split("&")) {
            // Split once so an equals sign in a value is preserved, then decode URL-escaped text.
            String[] pair = part.split("=",2);
            String key = URLDecoder.decode(pair[0],StandardCharsets.UTF_8);
            String value = pair.length == 2 ? URLDecoder.decode(pair[1],StandardCharsets.UTF_8) : "";
            // Unknown or repeated keys are rejected instead of silently choosing one interpretation.
            if(!allowed.contains(key) || result.putIfAbsent(key,value) != null) throw new HttpException(400,"Invalid query parameters");
        }
        return result;
    }
}
