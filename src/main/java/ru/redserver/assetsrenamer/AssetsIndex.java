package ru.redserver.assetsrenamer;

import java.util.Map;

public class AssetsIndex {

	public Map<String, Entry> objects;

	public static class Entry {

		public String hash;
		public long size;

		public Entry(String hash, long size) {
			this.hash = hash;
			this.size = size;
		}

	}

}
