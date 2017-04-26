package ru.redserver.assetsrenamer;

public enum Action {

	DECODE("Декодировать - переименовать для редактирования"),
	ENCODE("Кодировать - создать новый индекс");

	private final String title;

	Action(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return title;
	}

}
