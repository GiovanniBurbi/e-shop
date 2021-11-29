package com.apt.project.eshop.repository.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.apt.project.eshop.model.CartItem;
import com.apt.project.eshop.model.CatalogItem;
import com.apt.project.eshop.model.Product;
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
	public List<CatalogItem> findAll() {
		return StreamSupport.stream(productCollection
				.find(session).spliterator(), false)
				.map(this::fromDocumentToCatalogItem)
				.collect(Collectors.toList());
	}

	@Override
	public void loadCatalog(List<CatalogItem> items) {
		database.drop();
		List<Document> documents = fromCatalogItemsToDocuments(items);
		productCollection.insertMany(session, documents);
	}


	@Override
	public List<CatalogItem> findByName(String nameSearch) {
		return StreamSupport.stream(productCollection
				.find(session, Filters.regex("name", Pattern.compile(nameSearch, Pattern.CASE_INSENSITIVE)))
				.spliterator(), false)
				.map(this::fromDocumentToCatalogItem)
				.collect(Collectors.toList());
	}

	@Override
	public void removeFromStorage(CartItem item) throws RepositoryException {
		int quantityRequested = item.getQuantity();
		Bson filterNameProduct = Filters.eq("id", item.getProduct().getId());
		CatalogItem productInStorage = fromDocumentToCatalogItem(productCollection.find(session, filterNameProduct).first());
		int quantityInStorage = productInStorage.getStorage();
		if (quantityInStorage < quantityRequested)
			throw new RepositoryException("Insufficient stock", productInStorage);
		Bson update = Updates.inc("storage", -quantityRequested);
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
	
	private CatalogItem fromDocumentToCatalogItem(Document doc) {
		return new CatalogItem(new Product(""+doc.get("id"), ""+doc.get("name"), doc.getDouble("price")), doc.getInteger("storage"));
	}
}
