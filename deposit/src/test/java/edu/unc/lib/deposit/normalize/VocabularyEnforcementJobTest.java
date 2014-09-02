/**
 * Copyright 2008 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.unc.lib.deposit.normalize;

import static edu.unc.lib.dl.test.TestHelpers.setField;
import static edu.unc.lib.dl.util.ContentModelHelper.CDRProperty.invalidAffiliationTerm;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.unc.lib.dl.util.DepositConstants;
import edu.unc.lib.dl.xml.DepartmentOntologyUtil;

/**
 * @author bbpennel
 * @date Jun 26, 2014
 */
public class VocabularyEnforcementJobTest extends AbstractNormalizationJobTest {

	@Mock
	private DepartmentOntologyUtil deptUtil;

	private VocabularyEnforcementJob job;

	private static final String MAIN_RESOURCE = "info:fedora/uuid:resource";
	private static final String MAIN_UUID = "resource";

	@Before
	public void setup() throws Exception {
		job = new VocabularyEnforcementJob();
		job.setDepositUUID(depositUUID);
		job.setDepositDirectory(depositDir);
		setField(job, "depositsDirectory", depositsDirectory);
		setField(job, "jobStatusFactory", jobStatusFactory);
		setField(job, "depositStatusFactory", depositStatusFactory);
		setField(job, "deptUtil", deptUtil);

		job.getDescriptionDir().mkdir();

		Model model = ModelFactory.createDefaultModel();
		Bag depositBag = model.createBag(job.getDepositPID().getURI());
		Resource mainResource = model.createResource(MAIN_RESOURCE);
		depositBag.add(mainResource);

		File modelFile = new File(job.getDepositDirectory(), DepositConstants.MODEL_FILE);
		try (FileOutputStream fos = new FileOutputStream(modelFile)) {
			model.write(fos, "N-TRIPLE");
		}
	}

	@Test
	public void noMatchingDeptTest() throws Exception {
		Files.copy(Paths.get("src/test/resources/mods/singleAffiliationMods.xml"),
				job.getDescriptionDir().toPath().resolve(MAIN_UUID + ".xml"));

		job.run();

		verify(deptUtil).getAuthoritativeDepartment(anyString());

		Document modsDoc = getMODSDocument(MAIN_UUID);
		Element affiliation = element("/mods:mods/mods:name/mods:affiliation", modsDoc);
		assertEquals("Affiliation should not have been changed", "dept1", affiliation.getText());

		Model model = this.getModel(job);
		Resource mainResource = model.getResource(MAIN_RESOURCE);
		Property invalidTerm = model.createProperty(invalidAffiliationTerm.getURI().toString());
		Statement invalidStatement = model.getProperty(mainResource, invalidTerm);
		assertEquals("Department was not logged as being incorrect", "dept1", invalidStatement.getString());
	}

	@Test
	public void singleAffiliationTest() throws Exception {
		Files.copy(Paths.get("src/test/resources/mods/singleAffiliationMods.xml"),
				job.getDescriptionDir().toPath().resolve(MAIN_UUID + ".xml"));

		when(deptUtil.getAuthoritativeDepartment("dept1")).thenReturn(Arrays.asList(Arrays.asList("dept2")));

		job.run();

		verify(deptUtil).getAuthoritativeDepartment(anyString());

		Document modsDoc = getMODSDocument(MAIN_UUID);
		Element affiliation = element("/mods:mods/mods:name/mods:affiliation", modsDoc);
		assertEquals("Affiliation was not changed", "dept2", affiliation.getText());

		Model model = this.getModel(job);
		Resource mainResource = model.getResource(MAIN_RESOURCE);
		Property invalidTerm = model.createProperty(invalidAffiliationTerm.getURI().toString());
		Statement invalidStatement = model.getProperty(mainResource, invalidTerm);
		assertNull("Successful lookup should not create invalid term statement", invalidStatement);
	}

	@Test
	public void deptPathTest() throws Exception {
		Files.copy(Paths.get("src/test/resources/mods/singleAffiliationMods.xml"), job.getDescriptionDir().toPath()
				.resolve(MAIN_UUID + ".xml"));

		when(deptUtil.getAuthoritativeDepartment("dept1")).thenReturn(
				Arrays.asList(Arrays.asList("dept2", "dept3", "dept4", "dept5")));

		job.run();

		verify(deptUtil).getAuthoritativeDepartment(anyString());

		Document modsDoc = getMODSDocument(MAIN_UUID);
		List<String> affiliations = stringList(xpath("/mods:mods/mods:name/mods:affiliation", modsDoc));
		assertTrue("Lowest level department was not added", affiliations.contains("dept2"));
		assertTrue("Highest level department was not added", affiliations.contains("dept5"));

		assertFalse("Intermediate department should not be included in MODS", affiliations.contains("dept3"));
		assertEquals("Only two tiers of the department path should have been added", 2, affiliations.size());
	}

	@Test
	public void deptMultiplePathsTest() throws Exception {
		Files.copy(Paths.get("src/test/resources/mods/singleAffiliationMods.xml"), job.getDescriptionDir().toPath()
				.resolve(MAIN_UUID + ".xml"));

		when(deptUtil.getAuthoritativeDepartment("dept1")).thenReturn(
				Arrays.asList(Arrays.asList("dept2", "dept3"), Arrays.asList("dept2", "dept5")));

		job.run();

		verify(deptUtil).getAuthoritativeDepartment(anyString());

		Document modsDoc = getMODSDocument(MAIN_UUID);
		List<String> affiliations = stringList(xpath("/mods:mods/mods:name/mods:affiliation", modsDoc));
		assertTrue("Lowest level department was not added", affiliations.contains("dept2"));
		assertTrue("Highest level department from first path not added", affiliations.contains("dept3"));
		assertTrue("Highest level department from second path was not added", affiliations.contains("dept5"));

		assertEquals("Tiers from both paths should have been added", 3, affiliations.size());
	}

	@Test
	public void deptMultipleBaseTest() throws Exception {
		Files.copy(Paths.get("src/test/resources/mods/singleAffiliationMods.xml"), job.getDescriptionDir().toPath()
				.resolve(MAIN_UUID + ".xml"));

		when(deptUtil.getAuthoritativeDepartment("dept1")).thenReturn(
				Arrays.asList(Arrays.asList("dept5", "dept2"), Arrays.asList("dept3", "dept4", "dept2")));

		job.run();

		verify(deptUtil).getAuthoritativeDepartment(anyString());

		Document modsDoc = getMODSDocument(MAIN_UUID);
		List<String> affiliations = stringList(xpath("/mods:mods/mods:name/mods:affiliation", modsDoc));
		assertTrue("Shared top tier not added", affiliations.contains("dept2"));
		assertTrue("Base tier not added", affiliations.contains("dept3"));
		assertTrue("Base tier not added", affiliations.contains("dept5"));

		assertEquals("Incorrect number of affiliations", 3, affiliations.size());
	}

	@Test
	public void deptCorrectTest() throws Exception {
		Files.copy(Paths.get("src/test/resources/mods/singleAffiliationMods.xml"), job.getDescriptionDir().toPath()
				.resolve(MAIN_UUID + ".xml"));

		when(deptUtil.getAuthoritativeDepartment("dept1")).thenReturn(Arrays.asList(Arrays.asList("dept1")));

		job.run();

		verify(deptUtil).getAuthoritativeDepartment(anyString());

		Document modsDoc = getMODSDocument(MAIN_UUID);
		List<String> affiliations = stringList(xpath("/mods:mods/mods:name/mods:affiliation", modsDoc));
		assertTrue("Starting dept should still be present", affiliations.contains("dept1"));

		assertEquals("Only the original dept should be present", 1, affiliations.size());
	}

	@Test
	public void multipleNamesTest() throws Exception {
		Files.copy(Paths.get("src/test/resources/mods/multipleNameAffiliationsMods.xml"), job.getDescriptionDir()
				.toPath().resolve(MAIN_UUID + ".xml"));

		when(deptUtil.getAuthoritativeDepartment("dept1")).thenReturn(Arrays.asList(Arrays.asList("dept3", "dept1")));
		when(deptUtil.getAuthoritativeDepartment("dept2")).thenReturn(Arrays.asList(Arrays.asList("dept5", "dept4")));

		job.run();

		verify(deptUtil, times(3)).getAuthoritativeDepartment(anyString());

		Document modsDoc = getMODSDocument(MAIN_UUID);
		// System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(modsDoc));

		List<String> firstNameAffils = stringList(xpath("/mods:mods/mods:name[1]/mods:affiliation", modsDoc));
		assertTrue("Original department not present", firstNameAffils.contains("dept1"));
		assertTrue("Expanded dept not present", firstNameAffils.contains("dept3"));
		assertEquals("Incorrect number of affiliations on first name", 2, firstNameAffils.size());

		List<String> secondNameAffils = stringList(xpath("/mods:mods/mods:name[2]/mods:affiliation", modsDoc));
		assertEquals("No affiliations should be added to second name", 0, secondNameAffils.size());

		List<String> thirdNameAffils = stringList(xpath("/mods:mods/mods:name[3]/mods:affiliation", modsDoc));
		assertTrue("Original department not present", thirdNameAffils.contains("dept1"));
		assertTrue("Expanded dept not present", thirdNameAffils.contains("dept3"));
		assertTrue("Expanded dept not present", thirdNameAffils.contains("dept4"));
		assertTrue("Expanded dept not present", thirdNameAffils.contains("dept5"));
		assertEquals("Incorrect number of affiliations on third name", 4, thirdNameAffils.size());
	}

	@Test
	public void duplicateLookupTest() throws Exception {
		Files.copy(Paths.get("src/test/resources/mods/multipleNameAffiliationsMods.xml"), job.getDescriptionDir()
				.toPath().resolve(MAIN_UUID + ".xml"));

		when(deptUtil.getAuthoritativeDepartment("dept1")).thenReturn(Arrays.asList(Arrays.asList("dept3")));
		when(deptUtil.getAuthoritativeDepartment("dept2")).thenReturn(Arrays.asList(Arrays.asList("dept3")));

		job.run();

		Document modsDoc = getMODSDocument(MAIN_UUID);

		List<String> thirdNameAffils = stringList(xpath("/mods:mods/mods:name[3]/mods:affiliation", modsDoc));
		assertTrue("Mapped department not found", thirdNameAffils.contains("dept3"));
		assertEquals("Incorrect number of affiliations on third name", 1, thirdNameAffils.size());
	}

	@Test
	public void multipleInvalidTermsTest() throws Exception {
		Files.copy(Paths.get("src/test/resources/mods/multipleNameAffiliationsMods.xml"), job.getDescriptionDir()
				.toPath().resolve(MAIN_UUID + ".xml"));

		job.run();

		verify(deptUtil, times(3)).getAuthoritativeDepartment(anyString());

		Model model = this.getModel(job);
		Resource mainResource = model.getResource(MAIN_RESOURCE);
		Property invalidTerm = model.createProperty(invalidAffiliationTerm.getURI().toString());
		StmtIterator stmtIt = mainResource.listProperties(invalidTerm);
		List<String> invalidTerms = new ArrayList<>();
		while (stmtIt.hasNext()) {
			invalidTerms.add(stmtIt.next().getString());
		}

		assertTrue("Property indicating invalid affiliation was not added", invalidTerms.contains("dept1"));
		assertTrue("Property indicating invalid affiliation was not added", invalidTerms.contains("dept2"));
		assertEquals("Incorrect number of invalid affiliations logged", 2, invalidTerms.size());
	}

	@Test
	public void parentDuplicateTest() throws Exception {
		Files.copy(Paths.get("src/test/resources/mods/multipleNameAffiliationsMods.xml"), job.getDescriptionDir()
				.toPath().resolve(MAIN_UUID + ".xml"));

		when(deptUtil.getAuthoritativeDepartment("dept1")).thenReturn(Arrays.asList(Arrays.asList("dept1")));
		when(deptUtil.getAuthoritativeDepartment("dept2")).thenReturn(Arrays.asList(Arrays.asList("dept1", "dept2")));

		job.run();

		Document modsDoc = getMODSDocument(MAIN_UUID);

		List<String> thirdNameAffils = stringList(xpath("/mods:mods/mods:name[3]/mods:affiliation", modsDoc));
		assertTrue("Mapped department not found", thirdNameAffils.contains("dept1"));
		assertTrue("Mapped department not found", thirdNameAffils.contains("dept2"));
		assertEquals("Incorrect number of affiliations on third name", 2, thirdNameAffils.size());
	}

	private List<String> stringList(List<?> elements) {
		List<String> values = new ArrayList<>(elements.size());
		for (Object elObj : elements) {
			values.add(((Element) elObj).getText());
		}
		return values;
	}

	private Document getMODSDocument(String uuid) throws Exception {
		File modsFile = new File(job.getDescriptionDir(), uuid + ".xml");

		SAXBuilder sb = new SAXBuilder(false);
		return sb.build(modsFile);
	}
}