package com.cigia;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FindPair {

	public static void main(String[] args) {

		String filepath = args[0];
		int balance = Integer.parseInt(args[1]);
		boolean triple = false;

		if (args.length > 2) {
			triple = Boolean.parseBoolean(args[2]);
		}

		ArrayList<Item> items = new ArrayList<>();

		try (Stream<String> s = Files.lines(Paths.get(filepath))) {
			s.forEach(v -> Item.parseItem(v).ifPresent(items::add));

		} catch (FileNotFoundException e) {
			System.err.print("File not found at path: " + filepath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (triple) {
			System.out.println(findTriple(items, balance).toString());
		} else {
			System.out.println(findPair(items, balance).toString());
		}
	}

	public static Pair findPair(List<Item> items, int balance) {
		Pair result = new Pair();

		if (items.size() < 2)
			return result;

		int st = 0, ed = items.size() - 1;
		int minDiff = Integer.MAX_VALUE;

		while (st < ed) {
			int cost = items.get(st).price + items.get(ed).price;

			if (cost == balance)
				return new Pair(items.get(st), items.get(ed));

			if (cost < balance) {
				int diff = balance - cost;

				if (diff < minDiff) {
					minDiff = diff;
					result.a = items.get(st);
					result.b = items.get(ed);
				}
				st++;
			} else {
				ed--;
			}
		}

		return result;
	}

	public static Pair findTriple(List<Item> items, int balance) {
		List<Triple> sums = new ArrayList<>();

		for (int i = 0; i < items.size() - 1; i++) {
			for (int j = i + 1; j < items.size(); j++) {
				Triple triple = new Triple(items.get(i), items.get(j), i, j);
				if (triple.cost == balance)
					return triple;
				sums.add(triple);
			}
		}
		Collections.sort(sums);

		Triple result = new Triple();
		int minDiff = Integer.MAX_VALUE;

		for (int i = 0; i < items.size(); i++) {
			Item it = items.get(i);

			int st = 0, ed = sums.size() - 1;
			int target = balance - it.price;
			int mid = -1;

			while (st <= ed) {
				mid = st + (ed - st) / 2;
				Triple sum = sums.get(mid);

				if (sum.cost == target)
					break;

				if (sum.cost < target) {
					st = mid + 1;
				} else {
					ed = mid - 1;
				}
			}

			// Move cursor to the largest valid item
			while (mid > 0 && sums.get(mid).isUsed(i)) {
				mid--;
			}

			if (mid >= 0 && target > sums.get(mid).cost && minDiff > target - sums.get(mid).cost) {
				result = sums.get(mid);
				result.c = it;
				minDiff = target - sums.get(mid).cost;
			}
		}

		return result;
	}

	static class Pair implements Comparable<Pair> {
		Item a;
		Item b;
		int cost;

		Pair() {
		}

		Pair(Item a, Item b) {
			this.a = a;
			this.b = b;
			cost = a.price + b.price;
		}

		public boolean found() {
			return a != null && b != null;
		}

		@Override
		public String toString() {
			if (this.found())
				return a.toString() + ", " + b.toString();
			return "Not possible";
		}

		@Override
		public int compareTo(Pair o) {
			return this.cost - o.cost;
		}
	}

	static class Triple extends Pair {
		int i;
		int j;
		Item c;

		Triple() {
			super();
		}

		Triple(Item a, Item b, int i, int j) {
			super(a, b);
			this.i = i;
			this.j = j;
		}

		public boolean isUsed(int x) {
			return i == x || j == x;
		}

		@Override
		public String toString() {
			if (c != null)
				return String.join(", ", super.toString(), c.toString());
			return super.toString();
		}
	}

	static class Item {
		String name;
		int price;

		Item(String name, int price) {
			this.name = name;
			this.price = price;
		}

		@Override
		public String toString() {
			return name + " " + price;
		}

		public static Optional<Item> parseItem(String str) {
			Item item = null;
			String[] strs = str.split(",");

			if (strs.length >= 2) {
				String name = strs[0].trim();
				int price = Integer.parseInt(strs[1].trim());
				item = new Item(name, price);

			}
			return Optional.ofNullable(item);
		}
	}
}
