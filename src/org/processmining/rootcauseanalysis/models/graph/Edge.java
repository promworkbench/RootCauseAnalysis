package org.processmining.rootcauseanalysis.models.graph;

import java.util.Objects;
import java.util.UUID;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;

public class Edge {

	// FIELDS

	private UUID id;
	private Node source;
	private Node target;
	private UnifiedMap<String, Object> properties;

	// CONSTRUCTORS

	public Edge() {
		id = UUID.randomUUID();
		properties = new UnifiedMap<String, Object>();
	}

	public Edge(String label) {
		this();
		properties.put(Properties.KEY_LABEL, label);
	}

	public Edge(Node source, Node target) {
		this();
		setSource(source);
		setTarget(target);
	}

	public Edge(Node source, Node target, String label) {
		this(label);
		setSource(source);
		setTarget(target);
	}

	// GETTERS AND SETTERS

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Node getSource() {
		return source;
	}

	public void setSource(Node source) {
		this.source = source;
	}

	public Node getTarget() {
		return target;
	}

	public void setTarget(Node target) {
		this.target = target;
	}

	public UnifiedMap<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(UnifiedMap<String, Object> properties) {
		this.properties = properties;
	}

	// METHODS

	public String getLabel() {
		return properties.containsKey(Properties.KEY_LABEL) ? properties.get(Properties.KEY_LABEL).toString() : "";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Edge))
			return false;

		Edge edge = (Edge) obj;

		return Objects.equals(id, edge.id) && Objects.equals(source, edge.source) && Objects.equals(target, edge.target)
				&& Objects.equals(properties, edge.properties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, source, target);
	}

}
