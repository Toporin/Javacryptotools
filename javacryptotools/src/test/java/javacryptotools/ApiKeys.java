package javacryptotools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

// Loads API keys from api_keys.ini (see api_keys(sample).ini in the root project).
// The file path is set by the 'apikeys.file' system property (configured in build.gradle).
final class ApiKeys {

    private ApiKeys() {}

    static HashMap<String, String> load() {
        HashMap<String, String> apikeys = new HashMap<>();
        Path path = Paths.get(System.getProperty("apikeys.file", "../api_keys.ini"));
        if (!Files.exists(path)) {
            return apikeys;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith(";") || line.startsWith("#") || line.startsWith("[")) {
                    continue;
                }
                int sep = line.indexOf('=');
                if (sep <= 0) {
                    continue;
                }
                String key = line.substring(0, sep).trim();
                String value = line.substring(sep + 1).trim();
                // ignore placeholders copied from the template
                if (!value.isEmpty() && !value.startsWith("YOUR_")) {
                    apikeys.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read API keys from " + path, e);
        }
        return apikeys;
    }

    static boolean has(HashMap<String, String> apikeys, String name) {
        return apikeys.containsKey(name);
    }
}
