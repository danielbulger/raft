package com.danielbulger.raft;

import java.net.InetSocketAddress;
import java.util.Objects;

public abstract class Node {

	private final int id;
	private final InetSocketAddress address;

	protected Node(int id, InetSocketAddress address) {

		if (address == null) {
			throw new IllegalArgumentException();
		}

		this.id = id;
		this.address = address;
	}

	public int getId() {
		return id;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Node node = (Node) o;
		return id == node.id &&
			address.equals(node.address);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, address);
	}

	@Override
	public String toString() {
		return "Node{" +
			"id=" + id +
			", address=" + address +
			'}';
	}
}
