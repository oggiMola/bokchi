package com.example.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.Customer;
import com.example.repository.CustomerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

@RestController
@EnableMongoRepositories(basePackages = "com.example.repository")
public class MongoDBController {

    @Autowired
    MongoDatabaseFactory mongoDatabaseFactory;

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private ObjectMapper mapper;

    // 방식 1. JPA 사용 및 okimoki 참조
    @GetMapping("/testJpa")
    public Flux<List<Customer>> mongodbTest() {
        // Document 전부 삭제
        repository.deleteAll().subscribe();

        // Customer Collection/Document 생성
        repository.save(new Customer("Nonno", "Shin")).subscribe();
        repository.save(new Customer("Test", "Shin")).subscribe();

        // Document의 하위의 하위 생성
        Customer tmp = new Customer("Bokchi", "Gae");
        repository.save(new Customer("MolaMola", tmp)).subscribe();

        MongoDatabase db = mongoDatabaseFactory.getMongoDatabase();

        // Document 조회
        System.out.println("Customers found with findAll():");
        System.out.println("--------------------------------");
        FindIterable<Document> myDocs = db.getCollection("customer").find();
        for (Document doc : myDocs) {
            System.out.println(doc.toJson());
        }

        // firstName 검색
        System.out.println("Customer found with findByFirstName('Nonno'):");
        System.out.println("--------------------------------");
        System.out.println(repository.findByFirstName("Nonno").subscribe());
        repository.findByFirstName("Nonno").subscribe(s -> {
            System.out.println(s);
        });

        // lastName 검색
        System.out.println("Customers found with findByLastName('Shin'):");
        System.out.println("--------------------------------");
        List<Customer> listCustomer = new ArrayList<>();

        repository.findByLastName("Shin").subscribe(s -> {
            System.out.println(s);
            listCustomer.add(s);
        });

        // lastName 업데이트
        repository.findByFirstName("Test").subscribe(s -> {
            s.setLastName("Merong");
            repository.save(s).subscribe();
            listCustomer.add(s);

        });

        return Flux.just(listCustomer);
    }

    // API 테스트
    @PostMapping("/testPost")
    public String mongodbPostTest(@RequestBody Map<String, Object> requestData) {
        // collection내 document 전부 삭제
        repository.deleteAll().subscribe();

        MongoDatabase db = mongoDatabaseFactory.getMongoDatabase();

        System.out.println(requestData);

        // collection에 document 생성
        requestData.forEach((key, value) -> {
            System.out.println("key : " + key);
            System.out.println("value : " + value);

            // * 방식1 : 하나의 document에 여러 field 생성
            // docu.append(key, value);

            // * 방식2 : 다수의 document 생성
            Document doc = new Document();
            doc.put(key, value);
            db.getCollection("customer").insertOne(doc); // Collection(customer)에 Document 생성
        });
        // db.getCollection("customer").insertOne(docu);

        // Collection내의 모든 Document 조회
        System.out.println("Customers found with findAll():");
        FindIterable<Document> myDocs = db.getCollection("customer").find();
        for (Document doc : myDocs) {
            String json = doc.toJson();
            System.out.println(json);
        }

        return "done_post";
    }

    // 방식 2. Collection 자체에서 조회/업데이트 (JPA 사용X)
    @GetMapping("/testJpaNone")
    public List<Map> mongodbGetTest() {
        // collection내 document 전부 삭제
        repository.deleteAll().subscribe();

        MongoDatabase db = mongoDatabaseFactory.getMongoDatabase();
        Document docu1 = new Document("name", "cccc");
        Document docu2 = new Document("name", "aaaa");
        Document docu3 = new Document("name", "dddd");
        Document docu4 = new Document("name", "bbbb");
        Document docu5 = new Document("food", "maratang");

        Document docu_1 = new Document("age", 10);
        Document docu_2 = new Document("age", 40);
        Document docu_3 = new Document("age", 20);
        Document docu_4 = new Document("age", 30);
        Document docu_5 = new Document("price", 30);
        Document docu_5_1 = new Document("meat", "lamb");

        docu1.append("info", docu_1);
        docu2.append("info", docu_2);
        docu3.append("info", docu_3);
        docu4.append("info", docu_4);
        docu5.append("info", docu_5);
        docu_5.append("info", docu_5_1);

        List<Document> list = new ArrayList<Document>();
        list.add(docu1);
        list.add(docu2);
        list.add(docu3);
        list.add(docu4);
        list.add(docu5);

        // Document 생성
        db.getCollection("customer").insertMany(list);

        MongoCollection collection = db.getCollection("customer");

        // 1. 정렬 (field명 name을 기준으로 오름차순(value : 1))
        FindIterable<Document> myDocs1 = collection.find().sort(new BasicDBObject("name", 1));

        MongoCursor<Document> iterator = myDocs1.iterator();
        System.out.println("================= Sorting (name) ===============");
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

        // 2. 조건 검색 (filter/하위 document)
        Bson filter1 = lt("info.age", 30);
        Bson projection = fields(excludeId());
        FindIterable<Document> myDocs2 = collection.find(filter1).projection(projection);

        System.out.println("================= Searching (age<=30) ===============");
        List<Map> maplist = new ArrayList<Map>();
        for (Document doc : myDocs2) {
            String json = doc.toJson();
            System.out.println(json);

            Map<String, Object> map;

            // 조건 검색 결과 list로 출력 (return값)
            try {
                map = mapper.readValue(json, Map.class); // json->map parsing
                maplist.add(map);
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        // 3. 조건 검색 (filter/하위의 하위 document)
        System.out.println("================ Searching (info.info) ================");
        Bson filter2 = eq("info.info.meat", "lamb");
        FindIterable<Document> myDocs3 = collection.find(filter2).projection(projection);

        for (Document doc : myDocs3) {
            String json = doc.toJson();
            System.out.println(json);
        }

        // 4. Document 업데이트
        System.out.println("================ Update (info.info) ================");
        collection.updateOne(eq("name", "aaaa"), new Document("$set", new Document("name",
                "newName")));

        // 5. 조건 검색 (Bson/하위 document)
        System.out.println("================ Searching (name = aaaa) ================");
        FindIterable<Document> myDocs4 = collection.find(new BasicDBObject("name", "newName"));

        MongoCursor<Document> iterator2 = myDocs4.iterator();
        while (iterator2.hasNext()) {
            System.out.println(iterator2.next());
        }

        return maplist;
    }

}
