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
package edu.unc.lib.dl.cdr.services.imaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.unc.lib.dl.cdr.services.Enhancement;
import edu.unc.lib.dl.cdr.services.model.PIDMessage;
import edu.unc.lib.dl.cdr.services.util.JMSMessageUtil;
import edu.unc.lib.dl.fedora.FedoraException;
import edu.unc.lib.dl.fedora.ManagementClient;
import edu.unc.lib.dl.fedora.ManagementClient.Format;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.fedora.types.Datastream;
import edu.unc.lib.dl.util.ContentModelHelper;
import edu.unc.lib.dl.util.TripleStoreQueryService;

/**
 * Copyright 2010 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Gregory Jansen
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/service-context-it.xml" })
public class ThumbnailEnhancementServiceITCase {

	private static final Logger LOG = LoggerFactory.getLogger(ThumbnailEnhancementServiceITCase.class);

	@Resource
	private TripleStoreQueryService tripleStoreQueryService = null;

	@Resource
	private ManagementClient managementClient = null;

	@Resource
	private ThumbnailEnhancementService thumbnailEnhancementService = null;

	private Set<PID> samples = new HashSet<PID>();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private PIDMessage ingestSample(String filename, String dataFilename, String mimetype) throws Exception {
		File file = new File("src/test/resources", filename);
		Document doc = new SAXBuilder().build(new FileReader(file));
		PID pid = this.getManagementClient().ingest(doc, Format.FOXML_1_1, "ingesting test object");
		if (dataFilename != null) {
			File dataFile = new File("src/test/resources", dataFilename);
			String uploadURI = this.getManagementClient().upload(dataFile);
			this.getManagementClient().addManagedDatastream(pid, "DATA_FILE", false, "Thumbnail Test",
					Collections.<String>emptyList(), dataFilename, true, mimetype, uploadURI);
			PID dataFilePID = new PID(pid.getPid() + "/DATA_FILE");
			this.getManagementClient().addObjectRelationship(pid,
					ContentModelHelper.CDRProperty.sourceData.getURI().toString(), dataFilePID);
			this.getManagementClient().addLiteralStatement(pid,
					ContentModelHelper.CDRProperty.hasSourceMimeType.getURI().toString(), mimetype, null);
		}
		PIDMessage result = new PIDMessage(pid, JMSMessageUtil.servicesMessageNamespace, 
				JMSMessageUtil.ServicesActions.APPLY_SERVICE_STACK.getName());
		samples.add(pid);
		return result;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		try {
			for(PID p : samples) {
				this.getManagementClient().purgeObject(p, "removing test object", false);
			}
			samples.clear();
		} catch (FedoraException e) {
			e.printStackTrace();
			throw new Error("Cannot purge test object", e);
		}
	}

	@Test
	public void testFindCandidateObjects() throws Exception {
		PIDMessage pidPDF = ingestSample("thumbnail-PDF.xml", "sample.pdf", "application/pdf");
		//PIDMessage pidPDF2 = ingestSample("thumbnail-PDF2.xml", "sample.pdf", "application/pdf");
		PIDMessage pidTIFF = ingestSample("thumbnail-TIFF.xml", "sample.tiff", "image/tiff");
		PIDMessage pidDOC = ingestSample("thumbnail-DOC.xml", "sample.doc", "application/msword");
		PIDMessage pidCollNo = ingestSample("thumbnail-Coll-no.xml", null, null);

		// FIXME setup collection with surrogate
		PIDMessage pidCollYes = ingestSample("thumbnail-Coll-yes.xml", null, null);
		this.managementClient.addObjectRelationship(pidCollYes.getPID(), ContentModelHelper.CDRProperty.hasSurrogate.getURI().toString(), pidTIFF.getPID());

		List<PID> results = this.getThumbnailEnhancementService().findCandidateObjects(50);
		for (PID p : results) {
			LOG.debug("found candidate: " + p);
		}
		assertTrue("TIFF must be a candidate", results.contains(pidTIFF.getPID()));
		assertTrue("CollYes must be a candidate", results.contains(pidCollYes.getPID()));
		assertFalse("CollNo must not be a candidate", results.contains(pidCollNo.getPID()));
		assertFalse("DOC must not be a candidate", results.contains(pidDOC.getPID()));
		assertFalse("PDF must not be a candidate", results.contains(pidPDF.getPID()));
	}

	@Test
	public void testIsApplicable() throws Exception {
		PIDMessage pidPDF = ingestSample("thumbnail-PDF.xml", "sample.pdf", "application/pdf");
		//PIDMessage pidPDF2 = ingestSample("thumbnail-PDF2.xml", "sample.pdf", "application/pdf");
		PIDMessage pidTIFF = ingestSample("thumbnail-TIFF.xml", "sample.tiff", "image/tiff");
		PIDMessage pidDOC = ingestSample("thumbnail-DOC.xml", "sample.doc", "application/msword");

		// setup collection with surrogate
		PIDMessage pidCollYes = ingestSample("thumbnail-Coll-yes.xml", null, null);
		this.managementClient.addObjectRelationship(pidCollYes.getPID(), ContentModelHelper.CDRProperty.hasSurrogate.getURI().toString(), pidTIFF.getPID());

		PIDMessage pidCollNo = ingestSample("thumbnail-Coll-no.xml", null, null);
		// return false for a PID w/o sourcedata
		// return true for a PID w/o techData
		// return true for a PID w/techData older than sourceData
		// return false for a PID w/techData younger than sourceData
		assertTrue("The PID " + pidTIFF + " must be applicable.",
				this.getThumbnailEnhancementService().isApplicable(pidTIFF));
		assertFalse("The PID " + pidDOC + " must not be applicable.",
				this.getThumbnailEnhancementService().isApplicable(pidDOC));
		assertFalse("The PID " + pidPDF + " must not be applicable.",
				this.getThumbnailEnhancementService().isApplicable(pidPDF));
		assertTrue("The PID " + pidCollYes + " must be applicable.",
				this.getThumbnailEnhancementService().isApplicable(pidCollYes));
		assertFalse("The PID " + pidCollNo + " must not be applicable.", this.getThumbnailEnhancementService()
				.isApplicable(pidCollNo));
	}

	@Test
	public void testExtractionWorksAndNoLongerApplicable() throws Exception {
		PIDMessage pidTIFF = ingestSample("thumbnail-TIFF.xml", "sample.tiff", "image/tiff");
		Enhancement<Element> en = this.getThumbnailEnhancementService().getEnhancement(pidTIFF);
		try {
			en.call();
			Datastream thb = this.getManagementClient().getDatastream(pidTIFF.getPID(), "THUMB_LARGE");
			assertNotNull("Thumbnail datastream must not be null", thb);
		} catch (Exception e) {
			throw new Error(e);
		}
		assertFalse("The PID " + pidTIFF + " must not be applicable after service has run.", this
				.getThumbnailEnhancementService().isApplicable(pidTIFF));
		assertFalse("The PID " + pidTIFF + " must not be a candidate after service has run.", this
				.getThumbnailEnhancementService().findCandidateObjects(50).contains(pidTIFF.getPID()));

	}

	@Test
	public void testExtractionWorksAndNoLongerApplicableCollection() throws Exception {
		// ingest collection and tiff surrogate
		PIDMessage pidCollYes = ingestSample("thumbnail-Coll-yes.xml", null, null);
		PIDMessage pidTiff = ingestSample("thumbnail-TIFF.xml", "sample.tiff", "image/tiff");
		this.managementClient.addObjectRelationship(pidCollYes.getPID(), ContentModelHelper.CDRProperty.hasSurrogate.getURI().toString(), pidTiff.getPID());


		Enhancement<Element> en = this.getThumbnailEnhancementService().getEnhancement(pidCollYes);
		try {
			en.call();
		} catch (Exception e) {
			throw new Error(e);
		}
		assertFalse("The PID " + pidCollYes + " must not be applicable after service has run.", this
				.getThumbnailEnhancementService().isApplicable(pidCollYes));
		assertFalse("The PID " + pidCollYes + " must not be a candidate after service has run.", this
				.getThumbnailEnhancementService().findCandidateObjects(50).contains(pidCollYes.getPID()));

		Datastream ds = this.managementClient.getDatastream(pidCollYes.getPID(), ContentModelHelper.Datastream.THUMB_SMALL.getName());
		assertNotNull("There must be a small thumbnail on collection after service is run..", ds);
	}

	public TripleStoreQueryService getTripleStoreQueryService() {
		return tripleStoreQueryService;
	}

	public void setTripleStoreQueryService(TripleStoreQueryService tripleStoreQueryService) {
		this.tripleStoreQueryService = tripleStoreQueryService;
	}

	public ManagementClient getManagementClient() {
		return managementClient;
	}

	public void setManagementClient(ManagementClient managementClient) {
		this.managementClient = managementClient;
	}

	public ThumbnailEnhancementService getThumbnailEnhancementService() {
		return thumbnailEnhancementService;
	}

	public void setThumbnailEnhancementService(ThumbnailEnhancementService thumbnailEnhancementService) {
		this.thumbnailEnhancementService = thumbnailEnhancementService;
	}

}
