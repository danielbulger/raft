package com.danielbulger.raft.example;

import com.danielbulger.raft.net.RaftClient;
import com.danielbulger.raft.rpc.UpdateDataResponse;
import org.apache.thrift.async.AsyncMethodCallback;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class RaftCommandClient {

	public static void main(final String[] args) {

		RaftClient client = null;

		try (final Scanner scanner = new Scanner(System.in)) {

			String line;

			while (true) {
				try {
					System.out.println("Command: ");

					line = scanner.nextLine();

					if (line.toUpperCase().equals("QUIT")) {
						System.exit(1);
					}

					if (line.startsWith("connect")) {
						String[] parts = line.split(" ");

						if (parts.length != 3) {
							System.out.println("USAGE connect host ip");
							continue;
						}

						String host = parts[1];

						int port = Integer.parseInt(parts[2]);

						client = new RaftClient(new InetSocketAddress(host, port));
					} else if (line.equals("whoisleader")) {
						if (client == null) {
							System.out.println("Please connect to client first");
							continue;
						}
						client.whoIsLeader(new AsyncMethodCallback<>() {
							@Override
							public void onComplete(Integer response) {
								System.out.println("The leader is currently " + response);
							}

							@Override
							public void onError(Exception exception) {
								System.out.println("Please try again");
							}
						});
					} else if (line.startsWith("message")) {
						if (client == null) {
							System.out.println("Please connect to client first");
							continue;
						}

						final int index = line.indexOf(' ');

						final String msg = line.substring(index + 1);

						client.updateData(msg.getBytes(), new AsyncMethodCallback<>() {
							@Override
							public void onComplete(UpdateDataResponse response) {
								if (response.isSuccess()) {
									System.out.println("Replicated!");
								} else {
									if (response.getLeaderId() > 0) {
										System.out.println("Leader is currently " + response.getLeaderId());
									}
								}
							}

							@Override
							public void onError(Exception exception) {
								exception.printStackTrace();
							}
						});

					}

				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}

		}
	}
}
