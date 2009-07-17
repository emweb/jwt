package eu.webtoolkit.jwt;

class TestController extends WtServlet {
	private static final long serialVersionUID = 1L;

	TestController(Configuration configuration) {
		setConfiguration(configuration);
	}
	
	@Override
	public WApplication createApplication(WEnvironment env) {
		return null;
	}

}
