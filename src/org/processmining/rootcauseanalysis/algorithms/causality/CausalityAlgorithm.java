package org.processmining.rootcauseanalysis.algorithms.causality;

import java.util.Objects;

public abstract class CausalityAlgorithm implements ICausalityAlgorithm {

	// FIELDS

	private String description;

	// CONSTRUCTORS

	public CausalityAlgorithm(String description) {
		setDescription(description);
	}

	// GETTERS AND SETTERS

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// METHODS

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CausalityAlgorithm))
			return false;

		CausalityAlgorithm causality = (CausalityAlgorithm) obj;

		return super.equals(causality) && Objects.equals(description, causality.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), description);
	}

}
