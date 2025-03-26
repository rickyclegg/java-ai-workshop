import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Example usage
        List<Map<String, Object>> customers = new ArrayList<>();

        Map<String, Object> customer1 = new HashMap<>();
        customer1.put("id", "C001");
        customer1.put("name", "John Doe");
        customer1.put("age", 35);
        customer1.put("region", "NORTH");
        customer1.put("spending", 1200.50);
        customer1.put("interactions", 25);
        customer1.put("lastActive", "2023-05-15");
        customers.add(customer1);

        Map<String, Object> customer2 = new HashMap<>();
        customer2.put("id", "C002");
        customer2.put("name", "Jane Smith");
        customer2.put("birthDate", "1980-08-22");
        customer2.put("region", "SOUTH");
        customer2.put("spending", 850.75);
        customer2.put("interactions", 15);
        customer2.put("lastActive", "2023-06-20");
        customers.add(customer2);

        Map<String, Object> customer3 = new HashMap<>();
        customer3.put("id", "C003");
        customer3.put("name", "Bob Johnson");
        customer3.put("age", 52);
        customer3.put("region", "NORTH");
        customer3.put("spending", 2500.00);
        customer3.put("interactions", 40);
        customer3.put("lastActive", "2023-06-10");
        customers.add(customer3);

        Map<String, String> options = new HashMap<>();
        options.put("sort", "true");
        options.put("group", "true");
        options.put("sortField", "spending");

        Map<String, Object> result = CustomerDataProcessor.processCustomerData(customers, "PREMIUM", 1000.0, options);
        System.out.println("Result: " + result);
    }
}
