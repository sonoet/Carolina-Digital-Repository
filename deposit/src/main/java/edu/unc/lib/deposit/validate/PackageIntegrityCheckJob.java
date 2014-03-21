package edu.unc.lib.deposit.validate;

import java.util.Map;

import edu.unc.lib.deposit.work.AbstractDepositJob;
import edu.unc.lib.dl.util.RedisWorkerConstants.DepositField;

public class PackageIntegrityCheckJob extends AbstractDepositJob implements Runnable {

	/**
	 * Verifies the integrity of the deposit file received from SWORD.
	 * @param uuid
	 * @param bagDirectory
	 * @param depositId
	 */
	public PackageIntegrityCheckJob(String uuid, String depositUUID) {
		super(uuid, depositUUID);
	}
	
	public PackageIntegrityCheckJob() {
		super();
	};

	public void run() {
		Map<String, String> status = getDepositStatus();
		if(status.containsKey(DepositField.depositMd5.name())) {
			String md5 = status.get(DepositField.depositMd5.name());
			String file = status.get(DepositField.fileName.name());
			// TODO check the MD5 sum
			// TODO fail on mismatch
		}
	}

}