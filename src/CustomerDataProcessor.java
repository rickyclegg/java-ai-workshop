import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class CustomerDataProcessor {

    /**
     * Processes customer data and returns processed results
     *
     * @param customers List of customer data as maps
     * @param processingMode The processing mode to apply
     * @param filterThreshold Value used for filtering
     * @param options Additional processing options
     * @return Processed customer data as a map
     */
    public static Map<String, Object> processCustomerData(
            List<Map<String, Object>> customers,
            String processingMode,
            double filterThreshold,
            Map<String, String> options) {

        Map<String, Object> result = new HashMap<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

        // Validate input
        if (customers == null || customers.isEmpty()) {
            result.put("status", "ERROR");
            result.put("message", "No customer data provided");
            return result;
        }

        // Apply default options if not provided
        Map<String, String> opts = options != null ? options : new HashMap<>();
        String sortField = opts.getOrDefault("sortField", "age");
        String groupField = opts.getOrDefault("groupField", "region");
        String dateFormat = opts.getOrDefault("dateFormat", "yyyy-MM-dd");

        // Filter customers based on threshold
        List<Map<String, Object>> filteredCustomers = new ArrayList<>();
        for (Map<String, Object> customer : customers) {
            if (customer == null) continue;

            // Apply different filtering logic based on processing mode
            if (processingMode.equals("PREMIUM")) {
                if (customer.containsKey("spending") &&
                        customer.get("spending") instanceof Number &&
                        ((Number) customer.get("spending")).doubleValue() >= filterThreshold) {
                    filteredCustomers.add(customer);
                }
            } else if (processingMode.equals("ACTIVE")) {
                if (customer.containsKey("lastActive") && customer.get("lastActive") instanceof String) {
                    try {
                        Date lastActive = fmt.parse((String) customer.get("lastActive"));
                        Date threshold = new Date(System.currentTimeMillis() - (long)(filterThreshold * 24 * 60 * 60 * 1000));
                        if (lastActive.after(threshold)) {
                            filteredCustomers.add(customer);
                        }
                    } catch (ParseException e) {
                        // Skip invalid dates
                    }
                }
            } else if (processingMode.equals("ENGAGED")) {
                if (customer.containsKey("interactions") &&
                        customer.get("interactions") instanceof Number &&
                        ((Number) customer.get("interactions")).doubleValue() >= filterThreshold) {
                    filteredCustomers.add(customer);
                }
            } else {
                // Default mode: include all customers
                filteredCustomers.add(customer);
            }
        }

        // Process the filtered customers
        result.put("totalCustomers", customers.size());
        result.put("filteredCount", filteredCustomers.size());

        // Calculate statistics
        double totalAge = 0;
        double totalSpending = 0;
        Set<String> regions = new HashSet<>();
        Map<String, Integer> regionCounts = new HashMap<>();

        for (Map<String, Object> customer : filteredCustomers) {
            // Process age
            if (customer.containsKey("age") && customer.get("age") instanceof Number) {
                totalAge += ((Number) customer.get("age")).doubleValue();
            } else if (customer.containsKey("birthDate") && customer.get("birthDate") instanceof String) {
                try {
                    Date birthDate = new SimpleDateFormat(dateFormat).parse((String) customer.get("birthDate"));
                    Calendar now = Calendar.getInstance();
                    Calendar birth = Calendar.getInstance();
                    birth.setTime(birthDate);
                    int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
                    totalAge += age;
                    // Add calculated age back to customer data
                    customer.put("age", age);
                } catch (ParseException e) {
                    // Use default age if parsing fails
                    totalAge += 30;
                    customer.put("age", 30);
                }
            }

            // Process spending
            if (customer.containsKey("spending") && customer.get("spending") instanceof Number) {
                totalSpending += ((Number) customer.get("spending")).doubleValue();
            } else if (customer.containsKey("transactions")) {
                // Calculate spending from transactions if available
                Object transactions = customer.get("transactions");
                if (transactions instanceof List) {
                    double customerSpending = 0;
                    for (Object transaction : (List<?>) transactions) {
                        if (transaction instanceof Map) {
                            Map<?, ?> t = (Map<?, ?>) transaction;
                            if (t.containsKey("amount") && t.get("amount") instanceof Number) {
                                customerSpending += ((Number) t.get("amount")).doubleValue();
                            }
                        }
                    }
                    totalSpending += customerSpending;
                    customer.put("spending", customerSpending);
                }
            }

            // Process region data
            String region = "UNKNOWN";
            if (customer.containsKey("region") && customer.get("region") instanceof String) {
                region = (String) customer.get("region");
            } else if (customer.containsKey("address") && customer.get("address") instanceof Map) {
                Map<?, ?> address = (Map<?, ?>) customer.get("address");
                if (address.containsKey("region") && address.get("region") instanceof String) {
                    region = (String) address.get("region");
                    customer.put("region", region);
                }
            }

            regions.add(region);
            regionCounts.put(region, regionCounts.getOrDefault(region, 0) + 1);
        }

        // Sort customers if needed
        if (opts.containsKey("sort") && opts.get("sort").equalsIgnoreCase("true")) {
            final String field = sortField;
            Collections.sort(filteredCustomers, (c1, c2) -> {
                Object v1 = c1.getOrDefault(field, null);
                Object v2 = c2.getOrDefault(field, null);

                if (v1 == null && v2 == null) return 0;
                if (v1 == null) return -1;
                if (v2 == null) return 1;

                if (v1 instanceof Number && v2 instanceof Number) {
                    return Double.compare(
                            ((Number) v1).doubleValue(),
                            ((Number) v2).doubleValue()
                    );
                }

                return v1.toString().compareTo(v2.toString());
            });
        }

        // Group customers if needed
        Map<String, List<Map<String, Object>>> groupedCustomers = new HashMap<>();
        if (opts.containsKey("group") && opts.get("group").equalsIgnoreCase("true")) {
            for (Map<String, Object> customer : filteredCustomers) {
                String groupKey = "UNKNOWN";
                if (customer.containsKey(groupField)) {
                    Object value = customer.get(groupField);
                    groupKey = value != null ? value.toString() : "UNKNOWN";
                }

                if (!groupedCustomers.containsKey(groupKey)) {
                    groupedCustomers.put(groupKey, new ArrayList<>());
                }
                groupedCustomers.get(groupKey).add(customer);
            }
            result.put("groupedCustomers", groupedCustomers);
        }

        // Calculate averages
        double avgAge = filteredCustomers.isEmpty() ? 0 : totalAge / filteredCustomers.size();
        double avgSpending = filteredCustomers.isEmpty() ? 0 : totalSpending / filteredCustomers.size();

        // Add results to output
        result.put("status", "SUCCESS");
        result.put("customers", filteredCustomers);
        result.put("averageAge", avgAge);
        result.put("totalSpending", totalSpending);
        result.put("averageSpending", avgSpending);
        result.put("regions", regions);
        result.put("regionDistribution", regionCounts);
        result.put("processingMode", processingMode);
        result.put("filterThreshold", filterThreshold);

        // Apply some bizarre transformations based on processing mode
        if (processingMode.equals("PREMIUM")) {
            // Calculate loyalty score for premium customers
            List<Map<String, Object>> loyaltyData = new ArrayList<>();
            for (Map<String, Object> customer : filteredCustomers) {
                Map<String, Object> loyalty = new HashMap<>();
                loyalty.put("customerId", customer.getOrDefault("id", "unknown"));

                double spending = 0;
                if (customer.containsKey("spending") && customer.get("spending") instanceof Number) {
                    spending = ((Number) customer.get("spending")).doubleValue();
                }

                int interactions = 0;
                if (customer.containsKey("interactions") && customer.get("interactions") instanceof Number) {
                    interactions = ((Number) customer.get("interactions")).intValue();
                }

                // Bizarre loyalty formula
                double loyaltyScore = (spending / 100) * 1.5 + interactions * 0.7;
                if (customer.containsKey("age") && customer.get("age") instanceof Number) {
                    int age = ((Number) customer.get("age")).intValue();
                    if (age > 60) loyaltyScore *= 1.2;
                }

                loyalty.put("score", loyaltyScore);
                loyalty.put("tier", loyaltyScore > 50 ? "GOLD" : loyaltyScore > 25 ? "SILVER" : "BRONZE");
                loyaltyData.add(loyalty);
            }
            result.put("loyaltyData", loyaltyData);
        }

        return result;
    }
}
