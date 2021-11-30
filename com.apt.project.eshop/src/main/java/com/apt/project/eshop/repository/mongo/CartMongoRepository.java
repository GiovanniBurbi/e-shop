package com.apt.project.eshop.repository.mongo;

import static com.mongodb.client.model.Aggregates.lookup;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.apt.project.eshop.model.CartItem;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.CartRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class CartMongoRepository implements CartRepository {

	private static final String PRICE_NAME = "price";
	private static final String NAME_FIELD = "name";
	private static final String REF_FIELD = "product";
	private static final String QUANTITY_FIELD = "quantity";
	private static final String ID_EXTERNAL_FIELD = "_id";
	private MongoCollection<Document> cartCollection;
	private ClientSession session;
	private String productCollectionName;

	public CartMongoRepository(MongoClient client, String databaseName, String collectionName, String productCollectionName, ClientSession session) {
		cartCollection = client.getDatabase(databaseName).getCollection(collectionName);
		this.productCollectionName = productCollectionName;
		this.session = session;
	}

	@Override
	public void addToCart(Product product) {
		Document existingCartProduct = cartCollection.find(session, Filters.eq(REF_FIELD, product.getId())).first();
		if (existingCartProduct != null)
			cartCollection.updateOne(session, Filters.eq(REF_FIELD, product.getId()), Updates.inc(QUANTITY_FIELD, 1));
		else 
			cartCollection.insertOne(session, new Document().append(REF_FIELD, product.getId()).append(QUANTITY_FIELD, 1));
	}

	@Override
	public List<CartItem> allCart() {
		// Fill documents of this collection with related products in productCollection
		List<Document> cartJoined = aggregateCollections();
		return cartJoined.stream()
				.map(d -> new CartItem(fromDocumentToProduct(d.get(REF_FIELD, Document.class)), d.getInteger(QUANTITY_FIELD)))
				.collect(Collectors.toList());
	}

	@Override
	public void removeFromCart(Product product) {
		cartCollection.findOneAndDelete(session, Filters.eq(REF_FIELD, product.getId()));
	}

	@Override
	public double cartTotalCost() {
		List<Document> cartJoined = aggregateCollections();
		List<CartItem> cartItems = cartJoined.stream()
				.map(d -> new CartItem(fromDocumentToProduct(d.get(REF_FIELD, Document.class)), d.getInteger(QUANTITY_FIELD)))
				.collect(Collectors.toList());
		double total = 0;
		for (CartItem item : cartItems) {
			total += item.getProduct().getPrice() * item.getQuantity();
		}
		return total;
	}
	
	private Product fromDocumentToProduct(Document d) {
		return new Product(""+d.get(ID_EXTERNAL_FIELD), ""+d.get(NAME_FIELD), d.getDouble(PRICE_NAME));
	}
	
	private List<Document> aggregateCollections() {
		// aggregate collections cart and products using field REF_FIELD of cartCollection that match field ID_EXTERNAL_FIELD of productCollection
		// and put the results in REF_FIELD. 
		//Then exclude field _id of documents and unwind field REF_FIELD
		Bson lookup = lookup(productCollectionName, REF_FIELD, ID_EXTERNAL_FIELD, REF_FIELD);
		Bson project = project(fields(include(REF_FIELD, QUANTITY_FIELD), excludeId()));
		Bson unwind = unwind("$" + REF_FIELD);
		return cartCollection
				.aggregate(session, asList(lookup, project, unwind))
				.into(new ArrayList<>());
	}
}
