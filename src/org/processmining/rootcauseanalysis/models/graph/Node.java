package org.processmining.rootcauseanalysis.models.graph;

import java.util.Objects;
import java.util.UUID;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;

public class Node {

	// FIELDS

	private UUID id;
	private UnifiedMap<String, Object> properties;

	// CONSTRUCTORS

	public Node() {
		id = UUID.randomUUID();
		properties = new UnifiedMap<String, Object>();
	}

	public Node(String label) {
		this();
		properties.put(Properties.KEY_LABEL, label);
	}

	// GETTERS AND SETTERS

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UnifiedMap<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(UnifiedMap<String, Object> properties) {
		this.properties = properties;
	}

	// METHODS

	public Object getProperty(String property) {
		return properties.containsKey(property) ? properties.get(property) : null;
	}

	public Object setProperty(String key, Object value) {
		return properties.put(key, value);
	}

	public String getLabel() {
		return properties.containsKey(Properties.KEY_LABEL) ? properties.get(Properties.KEY_LABEL).toString() : "";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Node))
			return false;

		Node node = (Node) obj;

		return Objects.equals(id, node.id) && Objects.equals(properties, node.properties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public Node clone() {
		Node copy = new Node();
		copy.setProperties(properties.clone());
		return copy;
	}

}
