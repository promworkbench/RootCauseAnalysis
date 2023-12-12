package org.processmining.rootcauseanalysis.models.graph;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

public class Graph {

	// FIELDS

	private UUID id;
	private UnifiedSet<Node> nodes;
	private UnifiedSet<Edge> edges;
	private UnifiedMap<String, Object> properties;

	// CONSTRUCTORS

	public Graph() {
		id = UUID.randomUUID();
		nodes = new UnifiedSet<Node>();
		edges = new UnifiedSet<Edge>();
		setProperties(new UnifiedMap<String, Object>());
	}

	public Graph(String label) {
		this();
		properties.put(Properties.KEY_LABEL, label);
	}

	// GETTERS AND SETTERS

	public UUID getId() {
		return id;
	}

	public Node addNode(Node node) {
		nodes.add(node);
		return node;
	}

	public Edge addEdge(Edge edge) {
		edges.add(edge);
		return edge;
	}

	public Set<Node> getNodes() {
		return nodes;
	}

	public void setNodes(Set<Node> nodes) {
		this.nodes = new UnifiedSet<Node>(nodes);
	}

	public Set<Edge> getEdges() {
		return edges;
	}

	public void setEdges(Set<Edge> edges) {
		this.edges = new UnifiedSet<Edge>(edges);
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

	/**
	 * Returns all edges in this graph for which the specified node is either
	 * the source of the target node, depending on the specified flags.
	 * 
	 * @param node
	 *            The node that should be part of the edge.
	 * @param source
	 *            When set to true, edges in which the specified node is the
	 *            source node are included.
	 * @param target
	 *            When set to true, edges in which the specified node is the
	 *            target node are included.
	 * @return The set of requested edges in which the specified node is
	 *         present.
	 */
	public Set<Edge> getEdges(Node node, boolean source, boolean target) {
		Set<Edge> edgesForNode = Sets.mutable.empty();
		for (Edge edge : edges) {
			if (source && Objects.equals(edge.getSource(), node)) {
				edgesForNode.add(edge);
				continue;
			}
			if (target && Objects.equals(edge.getTarget(), node)) {
				edgesForNode.add(edge);
				continue;
			}
		}
		return edgesForNode;
	}

	/**
	 * Returns all edges in this graph for which the specified node is either
	 * the source or the target node.
	 * 
	 * @param node
	 *            The node that should be part of the edge.
	 * @return The set of edges in which the specified node is either the source
	 *         or the target node.
	 */
	public Set<Edge> getEdges(Node node) {
		return getEdges(node, true, true);
	}

	/**
	 * Checks whether there exists an ancestry relationship between two nodes
	 * (unidirectional). An ancestry relations exists when there is a path in
	 * the graph from the parent node to the child node. This implementation
	 * performs a depth-first search. *
	 * 
	 * @param child
	 *            The child node.
	 * @param parent
	 *            The parent node.
	 * @return Whether or not there exists an ancestry relation.
	 */
	public boolean ancestor(Node child, Node parent) {
		for (Edge edge : getEdges(child, false, true)) {
			if (edge.getSource().equals(parent) && edge.getTarget().equals(child))
				return true;
			if (ancestor(edge.getSource(), parent))
				return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Graph))
			return false;

		Graph graph = (Graph) obj;

		return Objects.equals(id, graph.id) && Objects.equals(nodes, graph.nodes) && Objects.equals(edges, graph.edges);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, nodes, edges);
	}

}
