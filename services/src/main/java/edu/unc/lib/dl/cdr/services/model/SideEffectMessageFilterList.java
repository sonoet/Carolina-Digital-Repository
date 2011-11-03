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
package edu.unc.lib.dl.cdr.services.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data structure which stores a list of PIDMessages to discard they next time they appear.
 * The message blocks will time out after messageTimeoutPeriod milliseconds if the corresponding
 * message never appears.
 * @author bbpennel
 *
 */
public class SideEffectMessageFilterList extends ArrayList<PIDMessage> {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(SideEffectMessageFilterList.class);
	private long messageTimeoutPeriod;
	
	public long getMessageTimeoutPeriod() {
		return messageTimeoutPeriod;
	}

	public void setMessageTimeoutPeriod(long messageTimeoutPeriod) {
		this.messageTimeoutPeriod = messageTimeoutPeriod;
	}
	
	public synchronized boolean contains(PIDMessage msg){
		String action = msg.getAction();
		String datastream = msg.getDatastream();
		
		//Filter out messages that are automatically generated by service events that aren't needed.
		Iterator<PIDMessage> messageIt = this.iterator();
		long startTime = System.currentTimeMillis();
		while (messageIt.hasNext()){
			PIDMessage filteredMessage = messageIt.next();
			if (filteredMessage.getTimeCreated() + messageTimeoutPeriod >= startTime){
				LOG.debug("Side effect message " + msg.getPIDString() + " " + action + " timed out.");
				messageIt.remove();
			} else {
				if (filteredMessage.getPIDString().equals(msg.getPIDString())
						&& action.length() > 0 && action.equals(filteredMessage.getAction())
						&& ((filteredMessage.getDatastream().length() > 0 && datastream.length() > 0 
								&& datastream.equals(filteredMessage.getDatastream()))
						|| (filteredMessage.getRelation().length() > 0 && msg.getRelation().length() > 0 
								&& msg.getRelation().equals(filteredMessage.getRelation())))){
					if (LOG.isDebugEnabled())
						LOG.debug("Message " + msg.getPIDString() + " " + action + " was a side effect message.");
					
					return true;
				}
			}
		}
		return false;
	}
	
}