package dao;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import model.Request;
import org.bson.Document;
import java.util.Properties;

public class RequestDao {

    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCredential credential;
    private MongoCollection<Document> collection;

    public RequestDao(Properties prop) {
        mongoClient = new MongoClient(prop.getProperty("host"), Integer.valueOf(prop.getProperty("port")) );
        db = mongoClient.getDatabase(prop.getProperty("dbname"));
        System.out.println(db.toString());
        credential = MongoCredential.createMongoCRCredential(prop.getProperty("login"),
                prop.getProperty("dbname"), prop.getProperty("password").toCharArray());
        collection = db.getCollection(prop.getProperty("table"));
        collection.createIndex(new BasicDBObject("dspId", 1), new IndexOptions().unique(true));
    }

    public void save(Request request) {
        Document document = new Document();
        document.append("dspId", request.getDspId());
        document.append("userId", request.getUserId());
        document.append("externalUserId", request.getExternalUserId());
        if (!checkIdentifiersPair(document)) {
            if (existDspId(request.getDspId())) {
                collection.replaceOne(Filters.eq("dspId", document.get("dspId")), document);
            } else {
                collection.insertOne(document);
            }
        }
    }

    private boolean existDspId(String dspId) {
        return collection.find(new Document().append("dspId", dspId)).first() != null;
    }

    private boolean checkIdentifiersPair(Document targetDoc) {
        Document doc = new Document();
        doc.append("userId", targetDoc.getString("userId"));
        doc.append("externalUserId", targetDoc.getString("externalUserId"));
        //return false;
        return collection.find(doc).first() != null;
    }

}
