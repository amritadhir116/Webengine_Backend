package web.engine;

import static spark.Spark.get;

public class InstantSearch {
	public InstantSearch() {
		get("/hello", (req, res)->"Hello, bro");
	}
}
