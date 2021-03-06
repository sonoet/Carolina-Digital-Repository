package edu.unc.lib.deposit.work;

import static edu.unc.lib.dl.util.DepositConstants.DESCRIPTION_DIR;
import static edu.unc.lib.dl.util.RedisWorkerConstants.DepositField.manifestURI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.util.DepositConstants;
import edu.unc.lib.dl.util.DepositStatusFactory;
import edu.unc.lib.dl.util.JobStatusFactory;
import edu.unc.lib.dl.util.PremisEventLogger;
import edu.unc.lib.dl.util.PremisEventLogger.Type;
import edu.unc.lib.dl.xml.JDOMNamespaceUtil;

/**
 * Constructed with deposit directory and deposit ID. Facilitates event logging
 * with standard success/failure states.
 *
 * @author count0
 *
 */
public abstract class AbstractDepositJob implements Runnable {
	private static final Logger log = LoggerFactory
			.getLogger(AbstractDepositJob.class);
	public static final String DEPOSIT_QUEUE = "Deposit";

	@Autowired
	private JobStatusFactory jobStatusFactory;

	@Autowired
	private DepositStatusFactory depositStatusFactory;

	// UUID for this deposit and its deposit record
	private String depositUUID;

	// UUID for this ingest job
	private String jobUUID;

	// Root directory where all deposits are stored
	@Autowired
	private File depositsDirectory;

	// Directory for this deposit
	private File depositDirectory;

	// Directory for local data files
	private File dataDirectory;

	private final PremisEventLogger eventLog = new PremisEventLogger(this
			.getClass().getName());
	// Directory containing PREMIS event files for individual objects in this
	// deposit
	private File eventsDirectory;

	@Autowired
	private Dataset dataset;

	public AbstractDepositJob() {
	}

	public AbstractDepositJob(String uuid, String depositUUID) {
		log.debug("Deposit job created: job:{} deposit:{}", uuid, depositUUID);
		this.jobUUID = uuid;
		this.depositUUID = depositUUID;
	}

	@PostConstruct
	public void init() {
		this.depositDirectory = new File(depositsDirectory, depositUUID);
		this.dataDirectory = new File(depositDirectory,
				DepositConstants.DATA_DIR);
		this.eventsDirectory = new File(depositDirectory,
				DepositConstants.EVENTS_DIR);
	}

	@Override
	public final void run() {
		try {
			runJob();
			if (dataset.isInTransaction()) {
				dataset.commit();
			}
		} catch (Throwable e) {
			if (dataset.isInTransaction()) {
				dataset.abort();
			}
			throw e;
		} finally {
			if (dataset.isInTransaction()) {
				dataset.end();
			}
		}
	}

	public abstract void runJob();

	public String getDepositUUID() {
		return depositUUID;
	}

	public void setDepositUUID(String depositUUID) {
		this.depositUUID = depositUUID;
	}

	public PID getDepositPID() {
		return new PID("uuid:" + this.depositUUID);
	}

	public String getJobUUID() {
		return jobUUID;
	}

	public void setJobUUID(String uuid) {
		this.jobUUID = uuid;
	}

	protected JobStatusFactory getJobStatusFactory() {
		return jobStatusFactory;
	}

	public void setJobStatusFactory(JobStatusFactory jobStatusFactory) {
		this.jobStatusFactory = jobStatusFactory;
	}

	protected DepositStatusFactory getDepositStatusFactory() {
		return depositStatusFactory;
	}

	public void setDepositStatusFactory(
			DepositStatusFactory depositStatusFactory) {
		this.depositStatusFactory = depositStatusFactory;
	}

	public Map<String, String> getDepositStatus() {
		Map<String, String> result = this.getDepositStatusFactory().get(
				depositUUID);
		return Collections.unmodifiableMap(result);
	}

	public File getDescriptionDir() {
		return new File(getDepositDirectory(), DESCRIPTION_DIR);
	}

	public File getDepositDirectory() {
		return depositDirectory;
	}

	public void setDepositDirectory(File depositDirectory) {
		this.depositDirectory = depositDirectory;
	}

	public File getDataDirectory() {
		return dataDirectory;
	}

	public PremisEventLogger getEventLog() {
		return eventLog;
	}

	public File getEventsDirectory() {
		return eventsDirectory;
	}

	/**
	 * Returns the file where the manifest for this deposit is stored. If no manifest was set, then null is returned
	 *
	 * @return
	 */
	public File getManifestFile() {
		String path = getDepositStatus().get(manifestURI.name());
		if (path == null)
			return null;
		return new File(path);
	}

	public void recordDepositEvent(Type type, String messageformat,
			Object... args) {
		String message = MessageFormat.format(messageformat, args);
		Element event = getEventLog().logEvent(type, message,
				this.getDepositPID());
		log.debug("event recorded: {}", event);
		appendDepositEvent(getDepositPID(), event);
	}

	public void failJob(Type type, String message, String details) {
		log.debug("failed deposit: {}", message);
		Element event = getEventLog().logEvent(type, message,
				this.getDepositPID());
		event = PremisEventLogger.addDetailedOutcome(event, "failed", details,
				null);
		appendDepositEvent(getDepositPID(), event);
		throw new JobFailedException(message);
	}

	public void failJob(Throwable throwable, Type type, String messageformat,
			Object... args) {
		String message = MessageFormat.format(messageformat, args);
		log.debug("failed deposit: {}", message, throwable);
		Element event = getEventLog().logException(message, throwable);
		event = PremisEventLogger.addLinkingAgentIdentifier(event,
				"SIP Processing Job", this.getClass().getName(), "Software");
		appendDepositEvent(getDepositPID(), event);
		throw new JobFailedException(message, throwable);
	}

	/**
	 * Returns the PREMIS events file for the given PID
	 *
	 * @param pid
	 * @return
	 */
	protected File getEventsFile(PID pid) {
		return createOrAppendToEventsFile(pid, null);
	}

	/**
	 * Appends an event to the PREMIS log for the given pid. If the log does not exist, it is created
	 *
	 * @param pid
	 * @param event
	 */
	protected void appendDepositEvent(PID pid, Element event) {
		createOrAppendToEventsFile(pid, event);
	}

	/**
	 * Appends an event to the PREMIS document for the given PID, creating the document if it does not already exist.
	 *
	 * @param pid
	 * @param event
	 * @return the premis document file
	 */
	private File createOrAppendToEventsFile(PID pid, Element event) {
		File file = new File(depositDirectory, DepositConstants.EVENTS_DIR + "/" + pid.getUUID() + ".xml");

		try {
			Document dom;
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
				dom = new Document();
				Element premis = new Element("premis",
						JDOMNamespaceUtil.PREMIS_V2_NS).addContent(PremisEventLogger.getObjectElement(pid));
				dom.setRootElement(premis);
			} else {
				// Not appending anything, so return before attempting to load existing file
				if (event == null)
					return file;

				dom = new SAXBuilder().build(file);
			}

			if (event != null)
				dom.getRootElement().addContent(event.detach());

			try (FileOutputStream out = new FileOutputStream(file, false)) {
				new XMLOutputter(Format.getPrettyFormat()).output(dom, out);
			}

			return file;
		} catch (JDOMException | IOException e1) {
			throw new Error("Unexpected problem with deposit events file", e1);
		}
	}

	public Model getWritableModel() {
		String uri = getDepositPID().getURI();
		this.dataset.begin(ReadWrite.WRITE);
		if (!this.dataset.containsNamedModel(uri)) {
			this.dataset.addNamedModel(uri, ModelFactory.createDefaultModel());
		}
		return this.dataset.getNamedModel(uri).begin();
	}

	public Model getReadOnlyModel() {
		String uri = getDepositPID().getURI();
		this.dataset.begin(ReadWrite.READ);
		return this.dataset.getNamedModel(uri).begin();
	}

	public void closeModel() {
		if (dataset.isInTransaction()) {
			dataset.commit();
			dataset.end();
		}
	}

	public void destroyModel() {
		String uri = getDepositPID().getURI();
		if (!dataset.isInTransaction()) {
			getWritableModel();
		}
		if (this.dataset.containsNamedModel(uri)) {
			this.dataset.removeNamedModel(uri);
		}
	}

	protected void setTotalClicks(int totalClicks) {
		getJobStatusFactory().setTotalCompletion(getJobUUID(), totalClicks);
	}

	protected void addClicks(int clicks) {
		getJobStatusFactory().incrCompletion(getJobUUID(), clicks);
	}

	public File getSubdir(String subpath) {
		return new File(getDepositDirectory(), subpath);
	}
}
