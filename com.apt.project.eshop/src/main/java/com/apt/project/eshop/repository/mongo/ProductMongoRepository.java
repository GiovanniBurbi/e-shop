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

	private static final String PRICE_FIELD_NAME = "price";
	private static final String NAME_FIELD_NAME = "name";
	private static final String ID_FIELD_NAME = "_id";
	private static final String STORAGE_FIELD_NAME = "storage";
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
				.find(session, Filters.regex(NAME_FIELD_NAME, Pattern.compile(nameSearch, Pattern.CASE_INSENSITIVE)))
				.spliterator(), false)
				.map(this::fromDocumentToCatalogItem)
				.collect(Collectors.toList());
	}

	@Override
	public void removeFromStorage(CartItem item) throws RepositoryException {
		int quantityRequested = item.getQuantity();
		Bson filterNameProduct = Filters.eq(ID_FIELD_NAME, item.getProduct().getId());
		CatalogItem productInStorage = fromDocumentToCatalogItem(productCollection.find(session, filterNameProduct).first());
		int quantityInStorage = productInStorage.getStorage();
		if (quantityInStorage < quantityRequested)
			throw new RepositoryException("Insufficient stock", productInStorage);
		Bson update = Updates.inc(STORAGE_FIELD_NAME, -quantityRequested);
		productCollection.findOneAndUpdate(session, filterNameProduct, update);
	}

	private List<Document> fromCatalogItemsToDocuments(List<CatalogItem> items) {
		List<Document> documents = new ArrayList<>();
		for (CatalogItem item : items) {
			documents.add(new Document()
					.append(ID_FIELD_NAME, item.getProduct().getId())
					.append(NAME_FIELD_NAME, item.getProduct().getName())
					.append(PRICE_FIELD_NAME, item.getProduct().getPrice())
					.append(STORAGE_FIELD_NAME, item.getStorage())
					);
		}
		return documents;
	}
	
	private CatalogItem fromDocumentToCatalogItem(Document doc) {
		return new CatalogItem(new Product(""+doc.get(ID_FIELD_NAME), ""+doc.get(NAME_FIELD_NAME), doc.getDouble(PRICE_FIELD_NAME)), doc.getInteger(STORAGE_FIELD_NAME));
	}
}
