package com.apt.project.eshop.repository.mongo;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.CatalogItem;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.repository.RepositoryException;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class ProductMongoRepository implements ProductRepository {

	private MongoCollection<Document> productCollection;
	private MongoDatabase database;
	private ClientSession session;

	public ProductMongoRepository(MongoClient client, String databaseName, String collectionName,
			ClientSession session) {
		database = client.getDatabase(databaseName);
		productCollection = database.getCollection(collectionName);
		this.session = session;
	}

	@Override
	public List<Product> findAll() {
		return StreamSupport.stream(productCollection.find(session).spliterator(), false).map(this::fromDocumentToProduct).collect(Collectors.toList());
	}

	@Override
	public void loadCatalog(List<CatalogItem> items) {
		database.drop();
		List<Document> documents = fromCatalogItemsToDocuments(items);
		productCollection.insertMany(session, documents);
	}


	@Override
	public List<Product> findByName(String nameSearch) {
		return StreamSupport.stream(productCollection
				.find(session, Filters.regex("name", Pattern.compile(nameSearch, Pattern.CASE_INSENSITIVE)))
				.spliterator(), false)
				.map(this::fromDocumentToProduct)
				.collect(Collectors.toList());
	}

	@Override
	public void removeFromStorage(Product product) throws RepositoryException {
		Bson filterNameProduct = Filters.eq("name", product.getName());
		int quantityToReduce = product.getQuantity();
		Product productInStorage = productCollection.find(session, filterNameProduct).first();
		int quantityInStorage = productInStorage.getQuantity();
		if (quantityInStorage < quantityToReduce)
			throw new RepositoryException("Insufficient stock", productInStorage);
		Bson update = Updates.inc("quantity", -quantityToReduce);
		productCollection.findOneAndUpdate(session, filterNameProduct, update);
	}

	private List<Document> fromCatalogItemsToDocuments(List<CatalogItem> items) {
		List<Document> documents = new ArrayList<>();
		for (CatalogItem item : items) {
			documents.add(new Document()
					.append("id", item.getProduct().getId())
					.append("name", item.getProduct().getName())
					.append("price", item.getProduct().getPrice())
					.append("storage", item.getStorage())
					);
		}
		return documents;
	}
	
	private Product fromDocumentToProduct(Document d) {
		return new Product(""+d.get("id"), ""+d.get("name"), d.getDouble("price"));
	}
}
