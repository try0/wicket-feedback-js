package jp.try0.wicket.feedback.js.samples;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link HomePage} tests.
 *
 * @author try0
 *
 */
public class HomePageTest {

	private WicketTester tester;

	@BeforeEach
	public void setUp() {
		tester = new WicketTester(new WicketApplication());
		tester.startPage(HomePage.class);
	}

	@AfterEach
	public void tearDown() {
		tester.destroy();
	}

	private void assertResponseContains(String expected) {
		String response = tester.getLastResponseAsString();
		assertTrue(response.contains(expected), response);
	}

	@Test
	public void rendersResourcesOfBothLibraries() {
		assertResponseContains("sweetalert2.min.js");
		assertResponseContains("notyf.min.js");
		assertResponseContains("new Notyf(");
	}

	@Test
	public void displaysInfoWithNotyf() {
		tester.clickLink("info");

		assertResponseContains("window.notyf.open({\"type\":\"info\",\"message\":\"info\"});");
	}

	@Test
	public void displaysErrorsWithSweetAlert2OneByOne() {
		tester.clickLink("errors");

		assertResponseContains("Promise.resolve()"
				+ ".then(function() { return Swal.fire({\"icon\":\"error\",\"text\":\"1st error\"}); })"
				+ ".then(function() { return Swal.fire({\"icon\":\"error\",\"text\":\"2nd error\"}); });");
	}

	@Test
	public void displaysTitleAndOptions() {
		tester.clickLink("options");

		assertResponseContains("Swal.fire({\"icon\":\"error\",\"title\":\"Title\","
				+ "\"text\":\"Displayed with a title and options\",\"confirmButtonText\":\"Got it\"});");
	}

}
